#!/bin/sh
set -e
apk add zip -q 2>/dev/null
cd /work
cp task-console-orig.jar task-console-patched.jar
mkdir -p /tmp/patch/META-INF/resources
cp task-bundle.patched.js /tmp/patch/META-INF/resources/app.bundle.js
cd /tmp/patch
zip -u /work/task-console-patched.jar META-INF/resources/app.bundle.js
echo "TASK PATCH DONE"
unzip -l /work/task-console-patched.jar | grep bundle
