#!/bin/bash
# This script will build the project.

buildTag="$TRAVIS_TAG"

export GRADLE_OPTS=-Xmx1024m

./gradlew -PreleaseMode=full -PbintrayUser="${bintrayUser}" -PbintrayKey="${bintrayKey}" -PsonatypeUsername="${sonatypeUsername}" -PsonatypePassword="${sonatypePassword}" build --stacktrace
