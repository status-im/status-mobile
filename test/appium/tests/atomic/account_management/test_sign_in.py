import pytest

from tests import marks, common_password, unique_password
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.all
@marks.sign_in
class TestSignIn(SingleDeviceTestCase):

    @marks.testrail_id(5312)
    @marks.critical
    def test_login_with_new_account(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        profile = sign_in.profile_button.click()
        default_username = profile.default_username_text.text
        self.driver.close_app()
        self.driver.launch_app()
        sign_in.accept_agreements()
        if not sign_in.element_by_text(default_username).is_element_displayed():
            self.driver.fail('Username is not shown while login')
        sign_in.sign_in()
        if not sign_in.home_button.is_element_displayed():
            self.driver.fail('User is not logged in')

    @marks.testrail_id(5463)
    @marks.medium
    def test_login_with_incorrect_password(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        profile = sign_in.profile_button.click()
        profile.logout()
        if sign_in.ok_button.is_element_displayed():
            sign_in.ok_button.click()
        sign_in.password_input.set_value(common_password + '1')
        sign_in.sign_in_button.click()
        sign_in.find_full_text("Wrong password")

    @marks.logcat
    @marks.testrail_id(5415)
    @marks.critical
    def test_password_in_logcat_sign_in(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user(password=unique_password)
        profile = sign_in.profile_button.click()
        profile.logout()
        sign_in.sign_in()
        values_in_logcat = sign_in.find_values_in_logcat(password=unique_password)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)


@marks.all
@marks.sign_in
class TestSignInOffline(MultipleDeviceTestCase):

    @marks.testrail_id(5327)
    @marks.medium
    def test_offline_login(self):
        self.create_drivers(1)
        sign_in = SignInView(self.drivers[0])
        sign_in.create_user()
        sign_in.toggle_airplane_mode()
        sign_in.accept_agreements()
        home = sign_in.sign_in()
        home.home_button.wait_for_visibility_of_element()
        connection_text = sign_in.connection_status.text
        if connection_text != 'Offline':
            pytest.fail("Connection status text '%s' doesn't match expected 'Offline'" % connection_text)
