# Gemma OCR Android Starter

Android starter app for extracting OCR JSON from an image using on-device Gemma 3n with MediaPipe Tasks.

## Local setup

`local.properties` must include:

```properties
sdk.dir=C\:\\Android\\sdk
HF_TOKEN=hf_your_token_here
MODEL_URL=https://huggingface.co/<org>/<repo>/resolve/main/<model-file>.task
MODEL_MAX_TOKENS=512
```

Notes:
- `HF_TOKEN` and `MODEL_URL` are read into `BuildConfig` at build time.
- `MODEL_MAX_TOKENS` is optional (default `512`) with a safety floor of `512`.
- `MODEL_MAX_TOKENS` is total context budget (image + prompt + output). Lower values can trigger `OUT_OF_RANGE` and JNI aborts.
- Use a MediaPipe-compatible multimodal Gemma 3n `.task` model artifact.
- The app downloads and caches the model on first run to app-private storage.

## Build and test

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug assembleDebugAndroidTest
```

## App flow

1. Setup screen validates config and downloads/caches model.
2. User picks image from gallery.
3. Extraction runs and returns JSON schema:
   - `full_text: string`
   - `lines: string[]`
   - `key_values: [{ key: string, value: string }]`
4. If output is invalid JSON, user can trigger manual JSON repair.
