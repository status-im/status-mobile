# E2E tests requirements

### Environment variables
- WEB3_INFURA_PROJECT_ID
- ETHERSCAN_API_KEY

1) If running on LambdaTest emulators:
- LAMBDA_TEST_USERNAME
- LAMBDA_TEST_ACCESS_KEY
2) If running with Testrail (`--testrail_report=True` param):
- TESTRAIL_PASS
- TESTRAIL_USER

## Other
Userdata is stored in `tests.users` module (`test/appium/tests/users.py`, which is not committed to repo).

So to launch e2e you need to copy `users.py` to `test/appium/tests/` to prevent `ImportError`

If you are external contributor, you can contact us at [error-reports@status.im](mailto:error-reports@status.im) to get `users.py` 

More info on local setup for e2e can be found [here](https://notes.status.im/setup-e2e)


