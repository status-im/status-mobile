from tests import tests_data
import time


def pytest_addoption(parser):
    parser.addoption("--build", action="store", default='build_' + time.strftime('%Y_%m_%d_%H_%M'),
                     help="Specify build name")
    parser.addoption('--apk', action='store', default=None, help='Please provide url or local path to apk')


def pytest_runtest_setup(item):
    tests_data.name = item.name
