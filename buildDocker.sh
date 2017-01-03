#!/bin/bash
pushd ${PWD}/al2016
docker build -t amcp/tupl-titan-server:latest .
docker login
docker push amcp/tupl-titan-server
popd
