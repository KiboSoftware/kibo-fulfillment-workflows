#!/bin/bash

echo "MAVEN_OPTS=$MAVEN_OPTS PUBLISH=$PUBLISH"

if [ "$PUBLISH" = "true" ]; then
  mvn -B -e -s maven_settings.xml deploy -fn -P nexus-deploy
else
  echo "Skipping deployment as PUBLISH is not set to true."
fi
