#!/usr/bin/env bash

# Fail on first error
set -e

TEST_JS=target/test/test.js
COVERAGE_REPORT=coverage-report/lcov.info

rm -rf "${COVERAGE_REPORT}"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

if [ ! -f "$TEST_JS" ]; then
	echo -e "${YELLOW}test suite not found, compiling and running...${NC}"
	make test
else
	echo -e "${YELLOW}test suite found.${NC}"
fi

echo -e "${YELLOW}running test suite with coverage...${NC}"

nyc node target/test/test.js

echo -e "${GREEN}DONE.${NC}"

if [ ! -f "$COVERAGE_REPORT" ]; then
	echo -e "${RED}coverage report not generated! check nyc configuration file .nycrc${NC}"
	exit 1
fi

## COVERALLS ENV ##

# COVERALLS_REPO_TOKEN (the secret repo token from coveralls.io)

# optionals ENV
# COVERALLS_SERVICE_NAME (the name of your build system, "jenkins"
# COVERALLS_SERVICE_JOB_ID

if [ -z "$COVERALLS_REPO_TOKEN" ]; then
	echo -e "${RED}COVERALLS_REPO_TOKEN not set!${NC}"
	exit 1
fi

echo -e "${YELLOW}uploading coverage to coveralls...${NC}"

cat coverage-report/lcov.info | coveralls

echo -e "${GREEN}DONE.${NC}"