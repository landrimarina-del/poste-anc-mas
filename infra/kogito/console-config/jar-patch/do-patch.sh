#!/bin/sh
set -e
apk add zip -q 2>/dev/null
cd /work
cp management-console-orig.jar management-console-patched.jar
mkdir -p /tmp/patch/META-INF/resources
cp app.bundle.patched.js /tmp/patch/META-INF/resources/app.bundle.js
cd /tmp/patch
zip -u /work/management-console-patched.jar META-INF/resources/app.bundle.js
echo "PATCH DONE"
unzip -l /work/management-console-patched.jar | grep bundle
