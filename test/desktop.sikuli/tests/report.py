import json
import os
from subprocess import check_output

TEST_REPORT_DIR = "%s/../report" % os.path.dirname(os.path.abspath(__file__))


def get_test_report_file_path(test_name):
    file_name = "%s.json" % test_name
    if not os.path.exists(TEST_REPORT_DIR):
        os.makedirs(TEST_REPORT_DIR)
    return os.path.join(TEST_REPORT_DIR, file_name)


def get_testrail_case_id(obj):
    if 'testrail_id' in obj.keywords._markers:
        return obj.keywords._markers['testrail_id'].args[0]


def save_test_result(test, report):
    test_name = test.name
    file_path = get_test_report_file_path(test_name)
    if report.failed:
        check_output(['screencapture', '%s/%s.png' % (TEST_REPORT_DIR, test_name)])
    with open('%s/%s.log' % (TEST_REPORT_DIR, test_name), 'r') as log:
        steps = [i for i in log]
    test_dict = {
        'testrail_case_id': get_testrail_case_id(test),
        'name': test_name,
        'steps': steps,
        'error': str(report.longrepr.reprcrash.message),
        'screenshot': test_name + '.png'}
    json.dump(test_dict, open(file_path, 'w'))
