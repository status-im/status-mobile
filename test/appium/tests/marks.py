import pytest

pr = pytest.mark.pr
testrail_case_id = pytest.mark.testrail_case_id
testrail_id = pytest.mark.testrail_id  # atomic tests
critical = pytest.mark.critical
high = pytest.mark.high
medium = pytest.mark.medium
low = pytest.mark.low

account = pytest.mark.account
upgrade = pytest.mark.upgrade
api = pytest.mark.api
all = pytest.mark.all
chat = pytest.mark.chat
chat_management = pytest.mark.chat_management
dapps = pytest.mark.dapps
message_reliability = pytest.mark.message_reliability
transaction = pytest.mark.transaction
wallet = pytest.mark.wallet
sign_in = pytest.mark.sign_in
skip = pytest.mark.skip
logcat = pytest.mark.logcat


battery_consumption = pytest.mark.battery_consumption
