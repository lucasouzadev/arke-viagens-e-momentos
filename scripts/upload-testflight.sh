#!/usr/bin/env bash
set -euo pipefail
umask 077
test "$DISTRIBUTION_DIR" = "$RUNNER_TEMP/arke-testflight"
profile_uuid=$(cat "$DISTRIBUTION_DIR/profile-uuid")
identity=$(cat "$DISTRIBUTION_DIR/identity")
xcodebuild -project ios/Arke.xcodeproj -scheme Arke -configuration Release \
  -destination 'generic/platform=iOS' -archivePath "$DISTRIBUTION_DIR/Arke.xcarchive" \
  CURRENT_PROJECT_VERSION="$BUILD_NUMBER" CODE_SIGN_STYLE=Manual \
  DEVELOPMENT_TEAM=ZRVX9VFFJW CODE_SIGN_IDENTITY="$identity" \
  PROVISIONING_PROFILE_SPECIFIER="$profile_uuid" \
  OTHER_CODE_SIGN_FLAGS="--keychain $DISTRIBUTION_DIR/distribution.keychain-db" archive
python3 - <<'PY'
import os, pathlib, plistlib
info = plistlib.loads((pathlib.Path(os.environ['DISTRIBUTION_DIR']) / 'Arke.xcarchive/Products/Applications/Arke.app/Info.plist').read_bytes())
expected = {'UIInterfaceOrientationPortrait', 'UIInterfaceOrientationPortraitUpsideDown', 'UIInterfaceOrientationLandscapeLeft', 'UIInterfaceOrientationLandscapeRight'}
assert expected <= set(info.get('UISupportedInterfaceOrientations~ipad', info.get('UISupportedInterfaceOrientations', []))), 'Archived iPad orientation declarations are incomplete'
assert info['CFBundleIdentifier'] == 'com.travel.arke'
assert info['CFBundleVersion'] == os.environ['BUILD_NUMBER']
print('Archived bundle, build number and iPad orientations verified.')
PY
xcodebuild -exportArchive -archivePath "$DISTRIBUTION_DIR/Arke.xcarchive" \
  -exportOptionsPlist "$DISTRIBUTION_DIR/ExportOptions.plist" -exportPath "$DISTRIBUTION_DIR/export"
ipa="$DISTRIBUTION_DIR/export/Arke.ipa"
test -f "$ipa"
codesign --verify --deep --strict "$DISTRIBUTION_DIR/Arke.xcarchive/Products/Applications/Arke.app"
# altool searches ./private_keys for the API key. Keep it inside the private temp directory.
cd "$DISTRIBUTION_DIR"
for operation in validate upload; do
  # Some altool versions exit zero even after a server-side rejection.
  # Require an explicit success message and reject any reported error.
  if ! xcrun altool "--${operation}-app" --type ios --file "$ipa" \
    --apiKey "$ASC_KEY_ID" --apiIssuer "$ASC_ISSUER_ID" 2>&1 | tee "$DISTRIBUTION_DIR/$operation.log"; then
    echo "::error::Apple $operation command failed."
    exit 1
  fi
  python3 "$GITHUB_WORKSPACE/scripts/verify-apple-upload.py" "$operation" "$DISTRIBUTION_DIR/$operation.log"
done
echo "### TestFlight upload accepted" >> "$GITHUB_STEP_SUMMARY"
echo "Version 0.0.1 ($BUILD_NUMBER). Apple processing and tester availability must be checked in App Store Connect." >> "$GITHUB_STEP_SUMMARY"
