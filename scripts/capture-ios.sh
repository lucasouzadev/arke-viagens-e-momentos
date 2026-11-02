#!/usr/bin/env bash
set -euo pipefail

output="$PWD/build/screenshots/ios"
mkdir -p "$output"
if [ -n "${ARKE_SIMULATOR_VERSION:-}" ]; then
  runtime=$(xcrun simctl list runtimes --json | python3 -c 'import json,os,sys; runtimes=[r for r in json.load(sys.stdin)["runtimes"] if r.get("isAvailable") and ".SimRuntime.iOS-" in r.get("identifier", "") and r.get("version") == os.environ["ARKE_SIMULATOR_VERSION"]]; assert runtimes, "Requested iOS runtime unavailable"; print(runtimes[-1]["identifier"])')
  export ARKE_CAPTURE_RUNTIME="$runtime"
  xcrun simctl create "iPhone 14 QA" com.apple.CoreSimulator.SimDeviceType.iPhone-14 "$runtime"
  xcrun simctl create "iPhone SE (3rd generation) QA" com.apple.CoreSimulator.SimDeviceType.iPhone-SE-3rd-generation "$runtime"
fi
xcrun simctl list devices available --json > "$output/devices.json"
python3 - "$output/devices.json" > "$output/selected.tsv" <<'PY'
import json, sys, os
data = json.load(open(sys.argv[1]))
selected_runtime = os.environ.get('ARKE_CAPTURE_RUNTIME')
device_sets = [ds for runtime, ds in data['devices'].items() if not selected_runtime or runtime == selected_runtime]
phones = [d for ds in device_sets for d in ds
          if d.get('isAvailable') and d['name'].startswith('iPhone')]
if not phones:
    raise SystemExit('No available iPhone Simulator')
standard = next((d for d in phones if d['name'] == 'iPhone 14 QA'), phones[0])
compact = next((d for d in phones if 'SE' in d['name']),
               next((d for d in phones if d['udid'] != standard['udid']), None))
print('standard\t' + standard['udid'] + '\t' + standard['name'])
if compact:
    print('compact\t' + compact['udid'] + '\t' + compact['name'])
PY

while IFS=$'\t' read -r size device model; do
  printf '%s: %s\n' "$size" "$model"
  xcrun simctl boot "$device" || true
  python3 - "$device" <<'PY'
import subprocess, sys
try:
    subprocess.run(['xcrun', 'simctl', 'bootstatus', sys.argv[1], '-b'], check=True, timeout=360)
except subprocess.TimeoutExpired:
    raise SystemExit('Simulator boot exceeded 360 seconds')
PY
  xcrun simctl status_bar "$device" override --time '9:41' --batteryState charged --batteryLevel 100
  xcrun simctl install "$device" build/ios/Build/Products/Debug-iphonesimulator/Arke.app
  if [ "$size" = standard ]; then
    # Fictitious QA position; never a person's location.
    xcrun simctl location "$device" set 0,0
    xcrun simctl io "$device" recordVideo --codec=h264 "$output/ios-qa-interactions.mov" &
    recorder=$!
    trap 'kill -INT "$recorder" 2>/dev/null || true; wait "$recorder" 2>/dev/null || true' EXIT
    if ! xcodebuild -project ios/Arke.xcodeproj -scheme Arke -configuration Debug \
      -destination "platform=iOS Simulator,id=$device" -parallel-testing-enabled NO \
      -test-timeouts-enabled YES -default-test-execution-time-allowance 180 -maximum-test-execution-time-allowance 180 \
      -derivedDataPath build/ios -resultBundlePath build/HeaderUI.xcresult \
      CODE_SIGNING_ALLOWED=NO test; then
      # Preserve the actual failure state rather than relaunching into the initial UI.
      xcrun simctl io "$device" screenshot "$output/ios-ui-test-failure.png"
      # XCTest can terminate the app before simctl captures. Export its exact
      # in-test diagnostic attachments as original PNGs as well.
      if xcrun xcresulttool export attachments --path build/HeaderUI.xcresult --output-path "$output/failure-attachments"; then
        find "$output/failure-attachments" -name '*.png' -exec cp {} "$output/" \;
      fi
      exit 1
    fi
    kill -INT "$recorder" 2>/dev/null || true
    wait "$recorder" 2>/dev/null || true
    trap - EXIT
  fi
  for theme in light dark; do
    xcrun simctl ui "$device" appearance "$theme"
    xcrun simctl launch --terminate-running-process "$device" dev.arke.app.dev
    # Map tiles are asynchronous; preserve real loading/offline results in the capture.
    sleep 12
    xcrun simctl io "$device" screenshot "$output/ios-${size}-${theme}.png"
    # Debug-only entry opens the real empty editor; it does not create or save data.
    xcrun simctl launch --terminate-running-process "$device" dev.arke.app.dev --capture-place-editor
    sleep 5
    xcrun simctl io "$device" screenshot "$output/ios-editor-${size}-${theme}.png"
    # Synthetic read-only examples in memory; no disk seeding or cleanup.
    xcrun simctl launch --terminate-running-process "$device" dev.arke.app.dev --capture-place-details
    sleep 5
    xcrun simctl io "$device" screenshot "$output/ios-details-${size}-${theme}.png"
    xcrun simctl launch --terminate-running-process "$device" dev.arke.app.dev --capture-place-details --capture-short-detail
    sleep 3
    xcrun simctl io "$device" screenshot "$output/ios-details-short-${size}-${theme}.png"
    xcrun simctl launch --terminate-running-process "$device" dev.arke.app.dev --capture-place-edit
    sleep 3
    xcrun simctl io "$device" screenshot "$output/ios-edit-${size}-${theme}.png"
  done
  xcrun simctl shutdown "$device"
done < "$output/selected.tsv"
