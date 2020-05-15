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

    @marks.testrail_id(5742)
    @marks.medium
    def test_keycard_interruption_creating_onboarding_flow(self):
        sign_in = SignInView(self.driver)

        sign_in.just_fyi('Cancel on PIN code setup stage')
        sign_in.get_started_button.click()
        sign_in.generate_key_button.click()
        username = sign_in.first_username_on_choose_chat_name.text
        sign_in.next_button.click()
        keycard_flow = sign_in.keycard_storage_button.click()
        keycard_flow.next_button.click()
        keycard_flow.begin_setup_button.click()
        keycard_flow.connect_card_button.click()
        keycard_flow.enter_another_pin()
        keycard_flow.cancel_button.click()
        if not keycard_flow.element_by_text_part('Dangerous operation').is_element_displayed():
            self.driver.fail('No Dangerous operation popup is shown on canceling operation from PIN code stage')
        keycard_flow.yes_button.click()

        sign_in.just_fyi('Cancel on Pair code stage: initialized')
        keycard_flow.begin_setup_button.click()
        keycard_flow.enter_default_pin()
        keycard_flow.enter_default_pin()
        keycard_flow.wait_for_element_starts_with_text('Write codes down')
        pair_code = keycard_flow.pair_code_text.text
        keycard_flow.cancel_button.click()
        if not keycard_flow.element_by_text_part('Dangerous operation').is_element_displayed():
            self.driver.fail('No Dangerous operation popup is shown on canceling operation from Pair code stage')
        keycard_flow.yes_button.click()

        sign_in.just_fyi('Cancel from Confirm seed phrase: initialized + 1 pairing slot is used')
        keycard_flow.begin_setup_button.click()
        keycard_flow.pair_code_input.set_value(pair_code)
        keycard_flow.pair_to_this_device_button.click()
        seed_phrase = keycard_flow.get_seed_phrase()
        keycard_flow.confirm_button.click()
        keycard_flow.yes_button.click()
        keycard_flow.cancel_button.click()
        if not keycard_flow.element_by_text_part('Dangerous operation').is_element_displayed():
            self.driver.fail('No Dangerous operation popup is shown on canceling operation during Backup seed phrase stage')
        keycard_flow.yes_button.click()
        if not keycard_flow.element_by_text_part('Back up seed phrase').is_element_displayed():
            self.driver.fail('On canceling setup from Confirm seed phrase was not redirected to expected screen')

        sign_in.just_fyi('Cancel from Back Up seed phrase: initialized + 1 pairing slot is used')
        keycard_flow.cancel_button.click()
        keycard_flow.yes_button.click()
        keycard_flow.begin_setup_button.click()
        keycard_flow.wait_for_element_starts_with_text('Back up seed phrase')
        new_seed_phrase = keycard_flow.get_seed_phrase()
        if new_seed_phrase != seed_phrase:
            self.errors.append('Another seed phrase is shown after cancelling setup during Back up seed phrase')
        keycard_flow.backup_seed_phrase()
        keycard_flow.enter_default_pin()
        sign_in.lets_go_button.wait_for_visibility_of_element(30)
        sign_in.lets_go_button.click_until_absense_of_element(sign_in.lets_go_button)
        sign_in.profile_button.wait_for_visibility_of_element(30)

        sign_in.just_fyi('Check username and relogin')
        profile = sign_in.get_profile_view()
        public_key, real_username = profile.get_public_key_and_username(return_username=True)
        if real_username != username:
            self.errors.append('Username was changed after interruption of creating account')
        profile.logout()
        sign_in.sign_in(keycard=True)
        self.errors.verify_no_errors()

    @marks.testrail_id(6246)
    @marks.medium
    def test_keycard_interruption_access_key_onboarding_flow(self):
        sign_in = SignInView(self.driver)

        recover_access = sign_in.access_key_button.click()
        recover_access.enter_seed_phrase_button.click()
        recover_access.seedphrase_input.click()
        recover_access.seedphrase_input.set_value(basic_user['passphrase'])
        recover_access.next_button.click()
        recover_access.reencrypt_your_key_button.click()
        keycard_flow = sign_in.keycard_storage_button.click()

        sign_in.just_fyi('Cancel on PIN code setup stage')
        keycard_flow.next_button.click()
        keycard_flow.begin_setup_button.click()
        keycard_flow.connect_card_button.click()
        keycard_flow.enter_another_pin()
        keycard_flow.cancel_button.click()
        if not keycard_flow.element_by_text_part('Dangerous operation').is_element_displayed():
            self.driver.fail('No Dangerous operation popup is shown on canceling operation from PIN code stage')
        keycard_flow.yes_button.click()

        sign_in.just_fyi('Cancel on Pair code stage: initialized')
        keycard_flow.begin_setup_button.click()
        keycard_flow.enter_default_pin()
        keycard_flow.enter_default_pin()
        keycard_flow.wait_for_element_starts_with_text('Write codes down')
        pair_code = keycard_flow.pair_code_text.text
        keycard_flow.cancel_button.click()
        if not keycard_flow.element_by_text_part('Dangerous operation').is_element_displayed():
            self.driver.fail('No Dangerous operation popup is shown on canceling operation from Pair code stage')
        keycard_flow.yes_button.click()

        sign_in.just_fyi('Finish setup and relogin')
        keycard_flow.begin_setup_button.click()
        if not keycard_flow.element_by_text_part('5 free pairing slots').is_element_displayed():
            self.errors.append('Number of free pairing slots is not shown or wrong')
        keycard_flow.pair_code_input.set_value(pair_code)
        keycard_flow.pair_to_this_device_button.click()
        keycard_flow.enter_default_pin()
        sign_in.lets_go_button.wait_for_visibility_of_element(30)
        sign_in.lets_go_button.click_until_absense_of_element(sign_in.lets_go_button)
        sign_in.profile_button.wait_for_visibility_of_element(30)
        public_key, default_username = sign_in.get_public_key_and_username(return_username=True)
        profile_view = sign_in.get_profile_view()
        if public_key != basic_user['public_key']:
            self.errors.append('Public key %s does not match expected' % public_key)
        if default_username != basic_user['username']:
            self.errors.append('Default username %s does not match expected' % default_username)
        profile_view.logout()
        sign_in.sign_in(keycard=True)

        self.errors.verify_no_errors()
