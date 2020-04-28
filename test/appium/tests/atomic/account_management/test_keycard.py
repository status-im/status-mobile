from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView
from views.keycard_view import KeycardView
from tests.users import basic_user


@marks.all
@marks.account
class TestCreateAccount(SingleDeviceTestCase):

    @marks.testrail_id(5689)
    @marks.critical
    def test_add_new_keycard_account_and_login(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user(keycard=True)

        sign_in.just_fyi('Check that after creating keycard account balance is 0, not ...')
        wallet_view = sign_in.wallet_button.click()
        wallet_view.set_up_wallet()
        if wallet_view.status_account_total_usd_value.text != '0':
            self.errors.append("Account USD value is not 0, it is %s" % wallet_view.status_account_total_usd_value.text)
        public_key, default_username = sign_in.get_public_key_and_username(return_username=True)
        profile = sign_in.get_profile_view()
        profile.logout()

        sign_in.just_fyi('Check that can login with keycard account')
        sign_in.multi_account_on_login_button.wait_for_visibility_of_element(5)
        sign_in.multi_account_on_login_button.click()
        keycard_view = KeycardView(self.driver)
        if not keycard_view.element_by_text_part(default_username).is_element_displayed():
            self.errors.append("%s is not found on keycard login screen!" % default_username)
        keycard_view.connect_selected_card_button.click()
        keycard_view.enter_default_pin()
        if not sign_in.home_button.is_element_displayed(10):
            self.driver.fail('Keycard user is not logged in')

        self.errors.verify_no_errors()

    @marks.testrail_id(6240)
    @marks.critical
    def test_restore_account_from_mnemonic_to_keycard(self):
        sign_in = SignInView(self.driver)
        sign_in.recover_access(passphrase=basic_user['passphrase'], keycard=True)

        sign_in.just_fyi('Check that after restring account with assets is restored')
        wallet_view = sign_in.wallet_button.click()
        wallet_view.set_up_wallet()
        for asset in ['ETHro', 'ADI', 'STT']:
            if wallet_view.get_asset_amount_by_name(asset) == 0:
                self.errors.append('Asset %s was not restored')

        sign_in.just_fyi('Check that wallet address matches expected')
        address = wallet_view.get_wallet_address()
        if address != '0x%s' % basic_user['address']:
            self.errors.append('Restored address %s does not match expected' % address)

        sign_in.just_fyi('Check that username and public key match expected')
        public_key, default_username = sign_in.get_public_key_and_username(return_username=True)
        profile_view = sign_in.get_profile_view()
        if public_key != basic_user['public_key']:
            self.errors.append('Public key %s does not match expected' % public_key)
        if default_username != basic_user['username']:
            self.errors.append('Default username %s does not match expected' % default_username)
        profile_view.logout()

        sign_in.just_fyi('Check that can login with restored from mnemonic keycard account')
        sign_in.multi_account_on_login_button.wait_for_visibility_of_element(5)
        sign_in.multi_account_on_login_button.click()
        keycard_view = KeycardView(self.driver)
        keycard_view.connect_selected_card_button.click()
        keycard_view.enter_default_pin()
        if not sign_in.home_button.is_element_displayed(10):
            self.driver.fail('Keycard user is not logged in')

        self.errors.verify_no_errors()

