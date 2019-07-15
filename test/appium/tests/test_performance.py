from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView
from datetime import datetime
import time


class TestPerformance(SingleDeviceTestCase):

    def get_timestamps_by_event(self, *args):
        # earlier event will be overwritten by latest in case of multiple events in a logcat!

        timestamps_by_event = dict()
        logcat = self.driver.get_log('logcat')
        for event in args:
            for line in logcat:
                if event in line['message']:
                    timestamps_by_event[event] = line['timestamp']
        return timestamps_by_event

    @marks.testrail_id(6216)
    @marks.high
    @marks.performance
    @marks.skip
    def test_time_to_load_sign_in_screen(self):

        app_started = ':init/app-started'
        login_shown = ':on-will-focus :login'
        password_submitted = ':accounts.login.ui/password-input-submitted'
        login_success = ':accounts.login.callback/login-success'

        sign_in = SignInView(self.driver)
        sign_in.create_user()
        profile = sign_in.profile_button.click()
        profile.logout()
        home = sign_in.sign_in()
        home.plus_button.click()
        self.driver.info("Close app")
        self.driver.close_app()
        self.driver.info("Launch app")
        self.driver.launch_app()
        time.sleep(5)

        timestamps_by_event = self.get_timestamps_by_event(app_started, login_shown, password_submitted, login_success)
        for event in app_started, login_shown, password_submitted, login_success:
            self.driver.info("event: '%s' | timestamp: '%s' | time: '%s'" % (event, timestamps_by_event[event],
                             datetime.utcfromtimestamp(timestamps_by_event[event] / 1000)))

        time_to_login= (timestamps_by_event[login_success] - timestamps_by_event[password_submitted]) / 1000
        self.driver.info("Time to login is '%s'" % time_to_login)

        time_to_start_app = (timestamps_by_event[login_shown] - timestamps_by_event[app_started]) / 1000
        self.driver.info("Time to start the app is '%s'" % time_to_start_app)

        baseline_start_app = 0.8
        baseline_login = 1.2

        if time_to_start_app > baseline_start_app:
            self.errors.append(
                "time between starting the app and login screen is '%s' seconds, while baseline is '%s'!"
                % (time_to_start_app, baseline_start_app))
        if time_to_login > baseline_login:
            self.errors.append(
                "time between submitting a password and successful login is '%s' seconds, while baseline is '%s'!"
                % (time_to_login, baseline_login))
        self.verify_no_errors()
