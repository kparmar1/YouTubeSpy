#!/bin/bash
docker run -v ~/.youtubespy:/root/.youtubespy -v "$(pwd)":/data youtubespy "$@"