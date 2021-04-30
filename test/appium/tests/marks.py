import pytest

testrail_case_id = pytest.mark.testrail_case_id
testrail_id = pytest.mark.testrail_id  # atomic tests
critical = pytest.mark.critical
high = pytest.mark.high
medium = pytest.mark.medium
low = pytest.mark.low

flaky = pytest.mark.flaky
transaction = pytest.mark.transaction
upgrade = pytest.mark.upgrade
skip = pytest.mark.skip