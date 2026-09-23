# MacroDroid Workshop

Staging workbench for MacroDroid automation ideas, presets, and webhooks.

## Debug APK Build Workflow

This repository includes an automated GitHub Actions workflow and a local build script for generating the Debug APK.

### Automated GitHub Actions Workflow
- **File**: `.github/workflows/build-debug-apk.yml`
- **Triggers**:
  - Pushes to `main`, `master`, or `develop` branches.
  - Pull requests targeting `main` or `master`.
  - Manual trigger via GitHub `workflow_dispatch`.
- **Output**: Uploads `app-debug.apk` as a downloadable workflow artifact (retained for 30 days).

### Local Build Commands
To compile and assemble the Debug APK locally:

Using Gradle directly:
```bash
gradle assembleDebug
```

Using the provided script:
```bash
chmod +x scripts/build-debug-apk.sh
./scripts/build-debug-apk.sh
```

Output location: `app/build/outputs/apk/debug/app-debug.apk`

