#!/usr/bin/env bash
set -euo pipefail

output="$PWD/build/screenshots/android"
mkdir -p "$output"
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
bash android/gradlew -p android --no-daemon :app:connectedDebugAndroidTest
# The instrumentation runner cleans up installed packages after the test suite.
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
for size in standard compact; do
  if [ "$size" = compact ]; then
    adb shell wm size 720x1280
    adb shell wm density 320
  else
    adb shell wm size reset
    adb shell wm density reset
  fi
  for theme in light dark; do
    if [ "$theme" = dark ]; then
      adb shell cmd uimode night yes
    else
      adb shell cmd uimode night no
    fi
    adb shell am force-stop dev.arke.app.dev
    adb shell am start -W -n dev.arke.app.dev/dev.arke.app.MainActivity
    sleep 5
    adb shell pidof dev.arke.app.dev
    adb shell uiautomator dump /sdcard/arke-window.xml
    adb pull /sdcard/arke-window.xml "$output/android-${size}-${theme}.xml"
    adb exec-out screencap -p > "$output/android-${size}-${theme}.png"
    # Preserve the raw PNG, but reject captures covered by system error dialogs.
    python3 - "$output/android-${size}-${theme}.xml" <<'PYXML'
import sys, xml.etree.ElementTree as ET
nodes = list(ET.parse(sys.argv[1]).iter('node'))
if not any(n.get('package') == 'dev.arke.app.dev' for n in nodes):
    raise SystemExit('Arke is not visible in capture')
if any(n.get('package') == 'android' for n in nodes):
    raise SystemExit('Unexpected system dialog covers Arke; inspect diagnostic PNG')
PYXML
    # Open + using the observed accessibility node, then capture the real empty editor.
    python3 - "$output/android-${size}-${theme}.xml" > "$output/add-target.txt" <<'PYTARGET'
import re, sys, xml.etree.ElementTree as ET
nodes = [n for n in ET.parse(sys.argv[1]).iter('node') if n.get('content-desc') == 'Adicionar lugar']
if len(nodes) != 1:
    raise SystemExit('Expected one accessible add-place control')
x1, y1, x2, y2 = map(int, re.findall(r'\d+', nodes[0].get('bounds', '')))
print((x1 + x2) // 2, (y1 + y2) // 2)
PYTARGET
    read -r tap_x tap_y < "$output/add-target.txt"
    adb shell input tap "$tap_x" "$tap_y"
    sleep 3
    adb shell uiautomator dump /sdcard/arke-editor.xml
    adb pull /sdcard/arke-editor.xml "$output/android-editor-${size}-${theme}.xml"
    adb exec-out screencap -p > "$output/android-editor-${size}-${theme}.png"
    python3 - "$output/android-editor-${size}-${theme}.xml" <<'PYEDITOR'
import sys, xml.etree.ElementTree as ET
nodes = list(ET.parse(sys.argv[1]).iter('node'))
if any(n.get('package') == 'android' for n in nodes):
    raise SystemExit('Unexpected system dialog covers editor')
if not any('Nome do lugar' in n.get('text', '') for n in nodes):
    raise SystemExit('Place editor is not visible')
PYEDITOR
  done
done
adb shell wm size reset
adb shell wm density reset
