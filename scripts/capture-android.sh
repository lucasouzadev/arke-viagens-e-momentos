#!/usr/bin/env bash
set -euo pipefail

output="$PWD/build/screenshots/android"
mkdir -p "$output"
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
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
  done
done
adb shell wm size reset
adb shell wm density reset
