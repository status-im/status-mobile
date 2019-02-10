import pytest
import re
from tests.report import save_test_result, TEST_REPORT_DIR_CONTAINER
import logging
from tests import test_data


@pytest.mark.hookwrapper
def pytest_runtest_makereport(item, call):
    logging.basicConfig(filename='%s/%s.log' % (TEST_REPORT_DIR_CONTAINER, item.name),
                        filemode='w', level=logging.INFO)
    outcome = yield
    report = outcome.get_result()
    if report.when == 'call':
        save_test_result(item, report)


def pytest_collection(session):
    items = session.perform_collect()
    if session.config.getoption('--collect-only'):
        for i, item in enumerate(items):
            name = item.__dict__['name']
            cls = re.findall('\.(Test.*) object', str(item.__dict__['_obj']))[0]
            module = re.findall('(test_.*.py)', str(item.__dict__['fspath']))[0]
            test_data.tests_to_run.append('%s::%s::%s' % (module, cls, name))
    return items
