from typing import Dict


class SingleTestData(object):
    def __init__(self, name, testruns, testrail_case_id, geth_paths, grop_name):
        self.testrail_case_id = testrail_case_id
        self.name = name
        self.testruns = testruns
        self.geth_paths = geth_paths
        self.group_name = grop_name

    class TestRunData(object):
        def __init__(self, steps, jobs, error, first_commands: Dict[str, int], xfail):
            self.steps = steps
            self.jobs = jobs
            self.error = error
            self.first_commands = first_commands
            self.xfail = xfail

    def create_new_testrun(self):
        self.testruns.append(SingleTestData.TestRunData(list(), dict(), None, dict(), xfail=''))


class TestSuiteData(object):
    def __init__(self):
        self.apk_name = None
        self.current_test = None
        self.tests = list()

    def set_current_test(self, test_name, testrail_case_id):
        existing_test = next((test for test in self.tests if test.name == test_name), None)
        if existing_test:
            self.current_test = existing_test
        else:
            test = SingleTestData(test_name, list(), testrail_case_id, list(), None)
            self.tests.append(test)
            self.current_test = test
