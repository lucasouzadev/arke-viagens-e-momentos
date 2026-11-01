#!/usr/bin/env bash
set -euo pipefail

output="$PWD/build/screenshots/ios"
mkdir -p "$output"
xcrun simctl list devices available --json > "$output/devices.json"
python3 - "$output/devices.json" > "$output/selected.tsv" <<'PY'
import json, sys
data = json.load(open(sys.argv[1]))
phones = [d for ds in data['devices'].values() for d in ds
          if d.get('isAvailable') and d['name'].startswith('iPhone')]
if not phones:
    raise SystemExit('No available iPhone Simulator')
standard = next((d for d in phones if d['name'] == 'iPhone 16'), phones[0])
compact = next((d for d in phones if 'SE' in d['name']),
               next((d for d in phones if d['udid'] != standard['udid']), None))
print('standard\t' + standard['udid'] + '\t' + standard['name'])
if compact:
    print('compact\t' + compact['udid'] + '\t' + compact['name'])
PY

while IFS=$'\t' read -r size device model; do
  printf '%s: %s\n' "$size" "$model"
  xcrun simctl boot "$device" || true
  xcrun simctl bootstatus "$device" -b
  xcrun simctl status_bar "$device" override --time '9:41' --batteryState charged --batteryLevel 100
  xcrun simctl install "$device" build/ios/Build/Products/Debug-iphonesimulator/Arke.app
  if [ "$size" = standard ]; then
    xcodebuild -project ios/Arke.xcodeproj -scheme Arke -configuration Debug \
      -destination "platform=iOS Simulator,id=$device" -parallel-testing-enabled NO \
      -derivedDataPath build/ios -resultBundlePath build/HeaderUI.xcresult \
      CODE_SIGNING_ALLOWED=NO test
  fi
  for theme in light dark; do
    xcrun simctl ui "$device" appearance "$theme"
    xcrun simctl launch --terminate-running-process "$device" dev.arke.app.dev
    # Map tiles are asynchronous; preserve real loading/offline results in the capture.
    sleep 12
    xcrun simctl io "$device" screenshot "$output/ios-${size}-${theme}.png"
  done
  xcrun simctl shutdown "$device"
done < "$output/selected.tsv"
