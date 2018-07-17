import pytest

from tests import marks, common_password, get_current_time
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.all
@marks.account
class TestCreateAccount(SingleDeviceTestCase):

    @marks.testrail_id(758)
    @marks.smoke_1
    def test_create_account(self):
        if not self.test_fairy_warning_is_shown:
            self.errors.append('TestFairy warning is not shown')
        sign_in = SignInView(self.driver)
        if not sign_in.i_have_account_button.is_element_displayed():
            self.errors.append("'I have an account' button is not displayed")
        sign_in.create_account_button.click()
        sign_in.password_input.set_value(common_password)
        sign_in.next_button.click()
        sign_in.confirm_password_input.set_value(common_password)
        sign_in.next_button.click()

        sign_in.element_by_text_part('Display name').wait_for_element(30)
        sign_in.name_input.send_keys('user_%s' % get_current_time())

        sign_in.next_button.click()

    @marks.testrail_id(1433)
    def test_switch_users_and_add_new_account(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        public_key = sign_in.get_public_key()
        profile = sign_in.get_profile_view()
        profile.logout()
        if sign_in.ok_button.is_element_displayed():
            sign_in.ok_button.click()
        sign_in.other_accounts_button.click()
        sign_in.create_user()
        if sign_in.get_public_key() == public_key:
            pytest.fail('New account was not created')

    @marks.testrail_id(3692)
    @marks.skip
    def test_home_view(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        for text in ['Welcome to Status',
                     ('Here you can securely chat with people, or browse and interact with DApps. '
                      'Tap the “Plus” icon to begin.')]:
            if not home.element_by_text(text).is_element_displayed():
                self.errors.append("'%s' text is not shown" % text)
        home.profile_button.click()
        home.home_button.click()
        text = ('There are no recent Chats or DApps here yet. '
                'Tap the “Plus” button to see the list of Dapps or discover people to chat with.')
        if not home.element_by_text(text).is_element_displayed():
            self.errors.append("'%s' text is not shown" % text)
        self.verify_no_errors()

    @marks.testrail_id(844)
    def test_create_account_short_and_mismatch_password(self):
        sign_in = SignInView(self.driver)
        sign_in.create_account_button.click()
        sign_in.password_input.set_value('12345')

        mismatch_error = "Password confirmation doesn't match password"

        sign_in.next_button.click()
        if sign_in.confirm_password_input.is_element_displayed():
            self.errors.append('Next button is clickable when password is less then 6 symbols')

        sign_in.password_input.set_value('123456')
        sign_in.next_button.click()
        sign_in.confirm_password_input.set_value('1234567')
        sign_in.next_button.click()

        if not sign_in.find_text_part(mismatch_error):
            self.errors.append("'%s' is not shown")
        self.verify_no_errors()

    @marks.testrail_id(3767)
    def test_password_in_logcat_creating_account(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        sign_in.check_no_values_in_logcat(password=common_password)
