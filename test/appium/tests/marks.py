import pytest

testrail_case_id = pytest.mark.testrail_case_id
testrail_id = pytest.mark.testrail_id  # atomic tests
critical = pytest.mark.critical
medium = pytest.mark.medium
# new ui
nightly = pytest.mark.nightly
smoke = pytest.mark.smoke

flaky = pytest.mark.flaky
upgrade = pytest.mark.upgrade
skip = pytest.mark.skip
xfail = pytest.mark.xfail
secured = pytest.mark.secured
