from typing import Dict


class SingleTestData(object):
    def __init__(self, name, testruns, testrail_case_id, logs_paths, grop_name, secured):
        self.testrail_case_id = testrail_case_id
        self.name = name
        self.testruns = testruns
        self.logs_paths = logs_paths
        self.group_name = grop_name
        self.secured = secured

    class TestRunData(object):
        def __init__(self, steps, jobs, error, first_commands: Dict[str, int], xfail, run):
            self.steps = steps
            self.jobs = jobs
            self.error = error
            self.first_commands = first_commands
            self.xfail = xfail
            self.run = run

    def create_new_testrun(self):
        self.testruns.append(SingleTestData.TestRunData(
            steps=list(), jobs=dict(), error=None, first_commands=dict(), xfail='', run=True
        ))


class TestSuiteData(object):
    def __init__(self):
        self.apk_name = None
        self.current_test = None
        self.tests = list()

    def set_current_test(self, test_name, testrail_case_id, secured):
        existing_test = next((test for test in self.tests if test.name == test_name), None)
        if existing_test:
            self.current_test = existing_test
        else:
            test = SingleTestData(test_name, list(), testrail_case_id, list(), None, secured)
            self.tests.append(test)
            self.current_test = test
