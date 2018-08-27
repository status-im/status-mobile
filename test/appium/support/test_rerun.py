RERUN_ERRORS = [
    "can't receive further commands",
    'Original error: Error: ESOCKETTIMEDOUT',
    "The server didn't respond in time.",
    'An unknown server-side error occurred while processing the command.',
    'Could not proxy command to remote server. Original error: Error: socket hang up',
    'The server returned an invalid or incomplete response.',
    '502 Bad Gateway',
    'Unexpected server error',
    '504 Gateway Time-out',
    'Internal Server Error',
    'Invalid message: ERROR Internal Server Error',
    'ERROR The test with session id'
    "Message: 'CreateAccountButton' is not found on screen",
    "503 Service Unavailable",
    "object has no attribute",
    "[Errno 104] Connection reset by peer",
    "Sauce could not start your job",
    "HTTP Error 303",
    "http.client.RemoteDisconnected: Remote end closed connection without response",
    "[Errno 110] Connection timed out"
]


def should_rerun_test(test_error):
    for rerun_error in RERUN_ERRORS:
        if rerun_error in test_error:
            return True
    return False
