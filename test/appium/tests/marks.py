import pytest

testrail_case_id = pytest.mark.testrail_case_id
testrail_id = pytest.mark.testrail_id  # atomic tests
critical = pytest.mark.critical
medium = pytest.mark.medium

flaky = pytest.mark.flaky
upgrade = pytest.mark.upgrade
skip = pytest.mark.skip
xfail = pytest.mark.xfail
