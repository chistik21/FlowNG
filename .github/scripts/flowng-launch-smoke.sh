#!/usr/bin/env bash
set -euo pipefail

trap 'adb logcat -d -t 200 | tail -100' ERR
apk=$(find debug-apks -name 'FlowNG_*_x86_64.apk' -print -quit)
test -n "$apk"
adb install -r "$apk"
adb shell am start -W -n com.v2ray.ang/com.v2ray.ang.ui.main.MainActivity
sleep 8
pid=$(adb shell pidof com.v2ray.ang | tr -d '\r')
test -n "$pid" || { echo 'FlowNG exited after launch'; exit 1; }
echo "FlowNG UI process survived launch (PID $pid)"
