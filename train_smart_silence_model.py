
from pathlib import Path
import json
import sys

import numpy as np
import tensorflow as tf


# ============================================================
# SMART SILENCE - BASELINE TENSORFLOW LITE MODEL TRAINING
# ============================================================
#
# IMPORTANT:
# This first version uses synthetic, rule-generated labels.
# It validates the end-to-end ML integration, but it is not
# trained on real user history and is not production-validated.
#
# Input order MUST match ContextTrustMlFeatures.kt:
#  0 inside_saved_location
#  1 location_trust
#  2 activity_still
#  3 activity_walking
#  4 activity_running
#  5 activity_cycling
#  6 activity_driving
#  7 time_of_day
#  8 device_unlocked
#  9 behaviour_trust
# 10 rapid_repeated_action
# 11 failed_authentication_level
#
# Output: one float from 0.0 (low risk) to 1.0 (high risk)
# ============================================================


SEED = 42
SAMPLE_COUNT = 24000
FEATURE_COUNT = 12

MODEL_NAME = "smart_silence_trust_model.tflite"
METADATA_NAME = "smart_silence_trust_model_metadata.json"

FEATURE_NAMES = [
    "inside_saved_location",
    "location_trust",
    "activity_still",
    "activity_walking",
    "activity_running",
    "activity_cycling",
    "activity_driving",
    "time_of_day",
    "device_unlocked",
    "behaviour_trust",
    "rapid_repeated_action",
    "failed_authentication_level",
]


def generate_synthetic_data(sample_count: int, seed: int):
    """Generate reproducible, synthetic context examples."""

    rng = np.random.default_rng(seed)

    x = np.zeros(
        (sample_count, FEATURE_COUNT),
        dtype=np.float32,
    )

    # Location context.
    x[:, 0] = rng.integers(0, 2, size=sample_count)
    x[:, 1] = rng.uniform(0.0, 1.0, size=sample_count)

    # Activity one-hot features. An all-zero activity vector
    # represents UNKNOWN, matching the Kotlin preprocessing.
    activity_ids = rng.choice(
        np.array([0, 1, 2, 3, 4, 5]),
        size=sample_count,
        p=[0.28, 0.24, 0.10, 0.08, 0.18, 0.12],
    )

    for row, activity_id in enumerate(activity_ids):
        if activity_id < 5:
            x[row, 2 + activity_id] = 1.0
        # activity_id == 5 means UNKNOWN: all activity flags 0.

    # Hour of day normalized by 23, matching Kotlin.
    hours = rng.integers(0, 24, size=sample_count)
    x[:, 7] = hours.astype(np.float32) / 23.0

    # Device and behaviour context.
    x[:, 8] = rng.integers(0, 2, size=sample_count)
    x[:, 9] = rng.uniform(0.0, 1.0, size=sample_count)
    x[:, 10] = rng.integers(0, 2, size=sample_count)

    # Failed-authentication level: 0 to 5, normalized by 5.
    failed_attempt_levels = rng.integers(
        0, 6, size=sample_count
    )
    x[:, 11] = failed_attempt_levels.astype(np.float32) / 5.0

    # Synthetic risk labels. These are training labels for a
    # prototype, not observed ground truth from real users.
    outside_location = 1.0 - x[:, 0]
    low_location_trust = 1.0 - x[:, 1]
    unknown_activity = (
        np.sum(x[:, 2:7], axis=1) == 0.0
    ).astype(np.float32)

    late_hours = (
        ((hours >= 0) & (hours <= 5))
        | (hours == 23)
    ).astype(np.float32)

    early_hours = (
        (hours >= 6) & (hours <= 8)
    ).astype(np.float32)

    evening_hours = (
        (hours >= 19) & (hours <= 22)
    ).astype(np.float32)

    locked_device = 1.0 - x[:, 8]
    unusual_behaviour = 1.0 - x[:, 9]
    repeated_actions = x[:, 10]
    failed_authentication = x[:, 11]

    risk = (
        0.20 * outside_location
        + 0.15 * low_location_trust
        + 0.10 * unknown_activity
        + 0.12 * late_hours
        + 0.04 * early_hours
        + 0.03 * evening_hours
        + 0.08 * locked_device
        + 0.15 * unusual_behaviour
        + 0.12 * repeated_actions
        + 0.18 * failed_authentication
    )

    # Add small noise so the model learns a smooth mapping
    # instead of only memorizing a perfectly fixed formula.
    risk += rng.normal(
        loc=0.0,
        scale=0.015,
        size=sample_count,
    )

    y = np.clip(risk, 0.0, 1.0).astype(np.float32)

    return x, y.reshape(-1, 1)


def main():
    np.random.seed(SEED)
    tf.keras.utils.set_random_seed(SEED)

    project_root = Path(__file__).resolve().parent
    assets_dir = (
        project_root
        / "app"
        / "src"
        / "main"
        / "assets"
    )

    assets_dir.mkdir(parents=True, exist_ok=True)

    print("Generating synthetic training examples...")
    x, y = generate_synthetic_data(SAMPLE_COUNT, SEED)

    indices = np.random.permutation(len(x))
    split_index = int(len(indices) * 0.80)

    train_indices = indices[:split_index]
    validation_indices = indices[split_index:]

    x_train = x[train_indices]
    y_train = y[train_indices]
    x_validation = x[validation_indices]
    y_validation = y[validation_indices]

    print("Building the neural network...")

    model = tf.keras.Sequential(
        [
            tf.keras.layers.Input(
                shape=(FEATURE_COUNT,),
                dtype=tf.float32,
                name="context_features",
            ),
            tf.keras.layers.Dense(
                24,
                activation="relu",
            ),
            tf.keras.layers.Dense(
                12,
                activation="relu",
            ),
            tf.keras.layers.Dense(
                1,
                activation="sigmoid",
                name="normalized_risk",
            ),
        ],
        name="smart_silence_context_risk",
    )

    model.compile(
        optimizer=tf.keras.optimizers.Adam(
            learning_rate=0.001
        ),
        loss="mse",
        metrics=["mae"],
    )

    callbacks = [
        tf.keras.callbacks.EarlyStopping(
            monitor="val_loss",
            patience=6,
            restore_best_weights=True,
        )
    ]

    print("Training model...")
    history = model.fit(
        x_train,
        y_train,
        validation_data=(x_validation, y_validation),
        epochs=60,
        batch_size=128,
        callbacks=callbacks,
        verbose=2,
    )

    evaluation = model.evaluate(
        x_validation,
        y_validation,
        verbose=0,
        return_dict=True,
    )

    print("Validation results:", evaluation)

    print("Converting model to TensorFlow Lite...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = []
    tflite_model = converter.convert()

    model_path = assets_dir / MODEL_NAME
    model_path.write_bytes(tflite_model)

    # Validate the exact input/output shapes used by the
    # existing Kotlin TensorFlowLiteInferenceEngine.
    interpreter = tf.lite.Interpreter(
        model_content=tflite_model
    )
    interpreter.allocate_tensors()

    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    input_shape = input_details[0]["shape"].tolist()
    output_shape = output_details[0]["shape"].tolist()

    if input_shape != [1, FEATURE_COUNT]:
        raise RuntimeError(
            f"Unexpected model input shape: {input_shape}"
        )

    if int(np.prod(output_shape)) != 1:
        raise RuntimeError(
            f"Unexpected model output shape: {output_shape}"
        )

    # Run one smoke-test prediction.
    sample = np.zeros((1, FEATURE_COUNT), dtype=np.float32)
    interpreter.set_tensor(
        input_details[0]["index"],
        sample,
    )
    interpreter.invoke()

    sample_output = interpreter.get_tensor(
        output_details[0]["index"]
    )
    sample_risk = float(sample_output.reshape(-1)[0])

    if not np.isfinite(sample_risk) or not 0.0 <= sample_risk <= 1.0:
        raise RuntimeError(
            f"Invalid smoke-test output: {sample_risk}"
        )

    metadata = {
        "model": MODEL_NAME,
        "training_type": "synthetic_prototype",
        "training_examples": SAMPLE_COUNT,
        "feature_count": FEATURE_COUNT,
        "feature_names_in_order": FEATURE_NAMES,
        "input_shape": input_shape,
        "output_shape": output_shape,
        "output_meaning": "normalized risk from 0.0 to 1.0",
        "validation_mse": float(evaluation["loss"]),
        "validation_mae": float(evaluation["mae"]),
        "smoke_test_risk": sample_risk,
        "seed": SEED,
        "note": (
            "Synthetic training labels only. "
            "Not validated on real user behaviour."
        ),
    }

    metadata_path = assets_dir / METADATA_NAME
    metadata_path.write_text(
        json.dumps(metadata, indent=2),
        encoding="utf-8",
    )

    print()
    print("MODEL CREATED AND SHAPE-VALIDATED")
    print(f"Model: {model_path}")
    print(f"Size: {model_path.stat().st_size:,} bytes")
    print(f"Input shape: {input_shape}")
    print(f"Output shape: {output_shape}")
    print(f"Smoke-test risk: {sample_risk:.4f}")
    print(f"Metadata: {metadata_path}")
    print()
    print(
        "Next: build the Android app and test model loading "
        "on a device. This script does not prove real-world "
        "security accuracy."
    )


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"MODEL TRAINING FAILED: {exc}", file=sys.stderr)
        raise
