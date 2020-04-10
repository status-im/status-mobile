from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.all
@marks.account
class TestCreateAccount(SingleDeviceTestCase):

    @marks.testrail_id(5689)
    @marks.critical
    def test_add_new_keycard_account(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user(keycard=True)

        sign_in.just_fyi('Check that after creating keycard account balance is 0, not ...')
        wallet_view = sign_in.wallet_button.click()
        wallet_view.set_up_wallet()
        if wallet_view.status_account_total_usd_value.text != '0':
            self.errors.append("Account USD value is not 0, it is %s" % wallet_view.status_account_total_usd_value.text)
        public_key = sign_in.get_public_key_and_username()
        profile = sign_in.get_profile_view()
        default_username = profile.default_username_text.text
        profile.logout()

        sign_in.just_fyi('Check that can login with keycard account')
        sign_in.multi_account_on_login_button.wait_for_visibility_of_element(5)
        sign_in.multi_account_on_login_button.click()
        from views.keycard_view import KeycardView
        keycard_view = KeycardView(self.driver)
        # TODO: disabled due to 10272
        # for text in (public_key[-5:],default_username):
        if not keycard_view.element_by_text_part(default_username).is_element_displayed():
            self.errors.append("%s is not found on keycard login screen!" % default_username)
        keycard_view.enter_default_pin()
        keycard_view.connect_card_button.click()
        # TODO: disabled as login is not made in e2e builds
        # if not sign_in.home_button.is_element_displayed():
        #     self.driver.fail('Keycard user is not logged in')

        self.errors.verify_no_errors()

