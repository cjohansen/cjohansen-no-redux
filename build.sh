#!/bin/bash

clojure -X:dev:build

rsync -av --exclude='images/' resources/public/ target/
