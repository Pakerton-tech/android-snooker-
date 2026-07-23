#!/bin/bash
# Build release AAB for Google Play
# Run from project root: ./release/build-release.sh

set -e

cd "$(dirname "$0")/.."
PROJECT_ROOT=$(pwd)

echo "=========================================="
echo " Snooker Scorekeeper - Release Build"
echo "=========================================="
echo ""
echo "Project: $PROJECT_ROOT"

# Ensure JDK 17 is in PATH
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"

# Verify JDK
echo "JDK: $(java -version 2>&1 | head -1)"
echo ""

# Clean
echo "→ Cleaning previous builds..."
./gradlew clean

# Build release AAB (Android App Bundle)
echo ""
echo "→ Building release AAB..."
./gradlew :app:bundleRelease

# Copy output
echo ""
echo "→ Copying AAB to release/ directory..."
cp app/build/outputs/bundle/release/app-release.aab release/snooker-scorekeeper-v1.0.0.aab 2>/dev/null || true

echo ""
echo "=========================================="
echo " Build Complete!"
echo "=========================================="
echo ""
echo "Output:"
ls -lh release/*.aab 2>/dev/null || echo "  (AAB not found - check build output)"
ls -lh app/build/outputs/bundle/release/ 2>/dev/null
echo ""
echo "To upload to Google Play Console:"
echo "  1. Go to https://play.google.com/console"
echo "  2. Create new app → Snooker Scorekeeper"
echo "  3. Production → Create new release"
echo "  4. Upload the AAB file"
echo "  5. Fill in store listing details"
