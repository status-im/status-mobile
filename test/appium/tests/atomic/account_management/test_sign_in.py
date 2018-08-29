import pytest

from tests import marks, common_password, unique_password
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.all
@marks.sign_in
class TestSignIn(SingleDeviceTestCase):

    @marks.testrail_id(1381)
    @marks.smoke_1
    def test_login_with_new_account(self):
        sign_in = SignInView(self.driver)
        username = 'test_user'
        sign_in.create_user(username=username)
        self.driver.close_app()
        self.driver.launch_app()
        sign_in.accept_agreements()
        if not sign_in.element_by_text(username).is_element_displayed():
            self.errors.append('Username is not shown while login')
        sign_in.sign_in()
        if not sign_in.home_button.is_element_displayed():
            self.errors.append('User is not logged in')
        self.verify_no_errors()

    @marks.testrail_id(2169)
    def test_login_with_incorrect_password(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        profile = sign_in.profile_button.click()
        profile.logout()
        if sign_in.ok_button.is_element_displayed():
            sign_in.ok_button.click()
        sign_in.password_input.set_value(common_password + '1')
        sign_in.sign_in_button.click()
        sign_in.find_full_text('Wrong password')

    @marks.logcat
    @marks.testrail_id(3768)
    def test_password_in_logcat_sign_in(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user(password=unique_password)
        profile = sign_in.profile_button.click()
        profile.logout()
        sign_in.sign_in()
        sign_in.check_no_values_in_logcat(password=unique_password)


@marks.all
@marks.sign_in
class TestSignInOffline(MultipleDeviceTestCase):

    @marks.testrail_case_id(3740)
    @marks.testrail_id(1432)
    def test_offline_login(self):
        self.create_drivers(1, offline_mode=True)
        sign_in = SignInView(self.drivers[0])
        sign_in.create_user()

        sign_in.driver.close_app()
        sign_in.driver.set_network_connection(1)  # airplane mode

        sign_in.driver.launch_app()
        sign_in.accept_agreements()
        home = sign_in.sign_in()
        home.home_button.wait_for_visibility_of_element()
        connection_text = home.connection_status.text
        if connection_text != 'Offline':
            pytest.fail("Connection status text '%s' doesn't match expected 'Offline'" % connection_text)
