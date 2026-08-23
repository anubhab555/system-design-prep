#!/bin/sh

# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
export APP_HOME

if [ -z "$JAVA_HOME" ]; then
  if command -v java >/dev/null 2>&1; then
    JAVA_HOME=$(dirname "$(dirname "$(command -v java)")") 2>/dev/null
  fi
fi

if [ -z "$JAVA_HOME" ]; then
  echo "Error: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
  echo ""
  echo "Please set the JAVA_HOME variable in your environment to match the"
  echo "location of your Java installation." >&2
  exit 1
fi

JAVA="$JAVA_HOME/bin/java"
if [ ! -x "$JAVA" ]; then
  echo "Error: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
  echo ""
  echo "Please set the JAVA_HOME variable in your environment to match the"
  echo "location of your Java installation." >&2
  exit 1
fi

DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"
JVM_OPTS="${JVM_OPTS:-$DEFAULT_JVM_OPTS}"
JVM_OPTS="$JVM_OPTS -Dorg.gradle.appname=$APP_BASE_NAME"

exec "$JAVA" $JVM_OPTS -classpath "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
