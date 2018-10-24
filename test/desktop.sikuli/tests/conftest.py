import pytest
from subprocess import check_output

from tests.report import save_test_result, TEST_REPORT_DIR


def pytest_addoption(parser):
    parser.addoption('--os',
                     action='store',
                     default='linux')
    parser.addoption("--nightly",
                     action="store",
                     default=True)
    parser.addoption('--dmg',
                     action='store',
                     default=None,
                     help='Url or local path to dmg')


def pytest_configure(config):
    import logging
    logging.basicConfig(level=logging.INFO)


@pytest.mark.hookwrapper
def pytest_runtest_makereport(item, call):
    import logging
    # logging.basicConfig(filename='%s/%s.log' % (TEST_REPORT_DIR, item.name), filemode='w', level=logging.INFO)
    outcome = yield
    report = outcome.get_result()
    if report.when == 'call':
        pass
        # save_test_result(item, report)


def after_all():
    if pytest.config.getoption('os') == 'linux':
        pass
        # check_output(['rm', '-rf', 'nightly.AppImage'])
    else:
        check_output(['rm', '-rf', 'nightly.dmg'])


@pytest.fixture(scope="session", autouse=True)
def before_all(request):
    request.addfinalizer(finalizer=after_all)
