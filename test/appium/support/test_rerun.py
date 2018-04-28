RERUN_ERRORS = [
    'Original error: Error: ESOCKETTIMEDOUT',
    "The server didn't respond in time.",
    'An unknown server-side error occurred while processing the command.',
    'Could not proxy command to remote server. Original error: Error: socket hang up'
]


def should_rerun_test(test_error):
    for rerun_error in RERUN_ERRORS:
        if rerun_error in test_error:
            return True
    return False
