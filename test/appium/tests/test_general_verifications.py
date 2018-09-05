from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


class TestLinksVerifications(SingleDeviceTestCase):

    @marks.testrail_id(5453)
    @marks.critical
    def test_privacy_policy_is_accessible(self):
        signin_view = SignInView(self.driver)
        no_link_found_error_msg = 'Could not find privacy policy link at'
        no_link_open_error_msg = 'Could not open our privacy policy from'

        if not signin_view.privacy_policy_link.is_element_displayed():
            self.driver.fail(f'{no_link_found_error_msg} Sign in view!')

        base_web_view = signin_view.privacy_policy_link.click()
        if not base_web_view.policy_summary.is_element_displayed():
            self.errors.append(f'{no_link_open_error_msg} Sign in view!')

        base_web_view.click_system_back_button()
        signin_view = SignInView(self.driver)
        home_view = signin_view.create_user()
        profile = home_view.profile_button.click()
        about_view = profile.about_button.click()
        base_web_view = about_view.privacy_policy_button.click()

        if not base_web_view.policy_summary.is_element_displayed():
            self.errors.append(f'{no_link_open_error_msg} Profile about view!')

        base_web_view.click_system_back_button()
        if about_view.privacy_policy_button.is_element_displayed():
            base_web_view.click_system_back_button()
        signin_view = profile.logout()
        if signin_view.ok_button.is_element_displayed():
            signin_view.ok_button.click()
        signin_view.other_accounts_button.click()

        if not signin_view.privacy_policy_link.is_element_displayed():
            self.driver.fail(f'{no_link_found_error_msg} Sign in view!')

        base_web_view = signin_view.privacy_policy_link.click()
        if not base_web_view.policy_summary.is_element_displayed():
            self.errors.append(f'{no_link_open_error_msg} Sign in view!')

        self.verify_no_errors()
