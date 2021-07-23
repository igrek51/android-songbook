#!/bin/bash
set -e
protoc -I=. --java_out=protos --kotlin_out=protos shared_song.proto
