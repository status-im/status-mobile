#!/bin/sh

TESTFAIRY_ENDPOINT="https://app.testfairy.com/upload/dsym/"

ZIP=zip
CURL=curl
STAT=stat
DATE=date

log() {
	NOW=$($DATE +"%Y-%m-%d %H:%M:%S")
	echo "${NOW} ${1}"
}

help() {
	echo "Usage: ${0} [-f] TESTFAIRY_API_KEY [-p DSYM_PATH] [-u TESTFAIRY_ENDPOINT]"
	exit 1
}

DAEMON=1
if [ "${1}" == "-f" ]; then
	DAEMON=0
	shift
elif [ "${1}" == "-d" ]; then
	# backward compatible when -f was the default
	shift
fi


API_KEY="${1}"
if [ ! "${API_KEY}" ]; then
	echo "Fatal: No Upload API key provided."
	help
fi

DSYM_PATH=${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}

shift
while [[ $# -gt 1 ]]
do
key="$1"
case $key in
    -u)
    TESTFAIRY_ENDPOINT="${2}"
    shift
    ;;
    -p)
    DSYM_PATH="${2}"
    shift
    ;;
    *)
        help
    ;;
esac
shift
done

if [ "${DSYM_PATH}" == "" ] || [ "${DSYM_PATH}" == "/" ] || [ ! -d "${DSYM_PATH}" ]; then
	echo "Fatal: No .dSYM folder found at path [${DSYM_PATH}]."
	help
fi

if [[ ${TESTFAIRY_ENDPOINT} == "" ]]; then
	echo "Fatal: No upload endpoint given."
	help
fi

NOW=$($DATE +%s)
TMP_FILENAME="/tmp/${NOW}-${DWARF_DSYM_FILE_NAME}.zip"

# Compress the .dSYM folder into a zip file
log "Compressing .dSYM folder ${DSYM_PATH}"
$ZIP -qrp9 "${TMP_FILENAME}" "${DSYM_PATH}"
FILE_SIZE=$($STAT -f "%z" "${TMP_FILENAME}")

foreground_upload() {
	# Upload zipped .dSYM file to TestFairy's servers
	STARTED=$($DATE +"%s")
	$CURL -s -F api_key="${API_KEY}" -F dsym=@"${1}" -o /dev/null "${TESTFAIRY_ENDPOINT}"
	ENDED=$($DATE +"%s")
	DIFF=$(expr ${ENDED} - ${STARTED})
	log "Symbols uploaded in ${DIFF} seconds"

	# Clean up behind
	rm -f ${TMP_FILENAME}
}

background_upload() {
	sh -c "$CURL -F api_key=\"${API_KEY}\" -F dsym=@\"${1}\" -s -o /dev/null \"${TESTFAIRY_ENDPOINT}\"; rm -f ${TMP_FILENAME};" /dev/null 2>&1 &
}

if [ "$DAEMON" == "0" ]; then
	log "Uploading ${FILE_SIZE} bytes to dsym server in foreground"
	foreground_upload "${TMP_FILENAME}"
else
	log "Uploading ${FILE_SIZE} bytes to dsym server in background"
	background_upload "${TMP_FILENAME}"
fi

log "TestFairy .dSYM upload script ends"

