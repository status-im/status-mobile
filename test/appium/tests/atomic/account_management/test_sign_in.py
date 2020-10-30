import pytest

from tests import marks, common_password, unique_password
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from views.sign_in_view import SignInView


class TestSignIn(SingleDeviceTestCase):

    @marks.testrail_id(5312)
    @marks.critical
    def test_login_with_new_account_logcat(self):
        sign_in = SignInView(self.driver)
        password = unique_password
        sign_in.create_user(password=password)
        profile = sign_in.profile_button.click()
        default_username = profile.default_username_text.text
        profile.logout()

        sign_in.just_fyi('Check that cannot login with incorrect password')
        if sign_in.ok_button.is_element_displayed():
            sign_in.ok_button.click()
        sign_in.multi_account_on_login_button.click()
        sign_in.password_input.set_value(common_password + '1')
        sign_in.sign_in_button.click()
        sign_in.find_full_text("Wrong password")

        sign_in.just_fyi('Checking username and login')
        if not sign_in.element_by_text(default_username).is_element_displayed():
            self.driver.fail('Username is not shown while login')
        sign_in.password_input.set_value(password)
        sign_in.sign_in_button.click()

        sign_in.just_fyi('Checking logcat that no password during creating new user and login')
        if not sign_in.home_button.is_element_displayed(10):
            self.driver.fail('User is not logged in')
        values_in_logcat = sign_in.find_values_in_logcat(password=password)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)