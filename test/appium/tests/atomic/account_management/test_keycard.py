from tests import marks, pair_code, common_password
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView
from views.keycard_view import KeycardView
from tests.users import basic_user, transaction_senders


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

        sign_in.just_fyi('Check that after restoring account with assets is restored')
        wallet_view = sign_in.wallet_button.click()
        wallet_view.set_up_wallet()
        for asset in ['ETH', 'ADI', 'STT']:
            if wallet_view.get_asset_amount_by_name(asset) == 0:
                self.errors.append('Asset %s was not restored' % asset)

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
        sign_in.sign_in(keycard=True)
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
        if not keycard_flow.element_by_translation_id("keycard-cancel-setup-title").is_element_displayed():
            self.driver.fail('No Dangerous operation popup is shown on canceling operation from PIN code stage')
        keycard_flow.yes_button.click()

        sign_in.just_fyi('Cancel on Pair code stage: initialized')
        keycard_flow.begin_setup_button.click()
        keycard_flow.enter_default_pin()
        keycard_flow.enter_default_pin()
        keycard_flow.wait_for_element_starts_with_text('Write codes down')
        pair_code = keycard_flow.pair_code_text.text
        keycard_flow.cancel_button.click()
        if not keycard_flow.element_by_translation_id("keycard-cancel-setup-title").is_element_displayed():
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
        if not keycard_flow.element_by_translation_id("keycard-cancel-setup-title").is_element_displayed():
            self.driver.fail('No Dangerous operation popup is shown on canceling operation during Backup seed phrase stage')
        keycard_flow.yes_button.click()
        if not keycard_flow.element_by_text_part('Back up seed phrase').is_element_displayed():
            self.driver.fail('On canceling setup from Confirm seed phrase was not redirected to expected screen')

        sign_in.just_fyi('Cancel from Back Up seed phrase: initialized + 1 pairing slot is used')
        keycard_flow.cancel_button.click()
        keycard_flow.yes_button.click()
        keycard_flow.begin_setup_button.click()
        keycard_flow.element_by_translation_id("back-up-seed-phrase").wait_for_element(10)
        new_seed_phrase = keycard_flow.get_seed_phrase()
        if new_seed_phrase != seed_phrase:
            self.errors.append('Another seed phrase is shown after cancelling setup during Back up seed phrase')
        keycard_flow.backup_seed_phrase()
        keycard_flow.enter_default_pin()
        for element in sign_in.maybe_later_button, sign_in.lets_go_button:
            element.wait_for_visibility_of_element(30)
            element.click()
        sign_in.profile_button.wait_for_visibility_of_element(30)

        sign_in.just_fyi('Check username and relogin')
        profile = sign_in.get_profile_view()
        public_key, real_username = profile.get_public_key_and_username(return_username=True)
        if real_username != username:
            self.errors.append('Username was changed after interruption of creating account')
        profile.logout()
        home = sign_in.sign_in(keycard=True)
        if not home.wallet_button.is_element_displayed(10):
            self.errors.append("Failed to login to Keycard account")
        self.errors.verify_no_errors()

    @marks.testrail_id(6246)
    @marks.medium
    @marks.flaky
    def test_keycard_interruption_access_key_onboarding_flow(self):
        sign_in = SignInView(self.driver)
        sign_in.get_started_button.click()

        sign_in.access_key_button.click()
        sign_in.enter_seed_phrase_button.click()
        sign_in.seedphrase_input.click()
        sign_in.seedphrase_input.set_value(basic_user['passphrase'])
        sign_in.next_button.click()
        sign_in.reencrypt_your_key_button.click()
        keycard_flow = sign_in.keycard_storage_button.click()

        sign_in.just_fyi('Cancel on PIN code setup stage')
        keycard_flow.next_button.click()
        keycard_flow.begin_setup_button.click()
        keycard_flow.connect_card_button.click()
        keycard_flow.enter_another_pin()
        keycard_flow.cancel_button.click()
        if not keycard_flow.element_by_translation_id("keycard-cancel-setup-title").is_element_displayed():
            self.driver.fail('No Dangerous operation popup is shown on canceling operation from PIN code stage')
        keycard_flow.yes_button.click()

        sign_in.just_fyi('Cancel on Pair code stage: initialized')
        keycard_flow.begin_setup_button.click()
        keycard_flow.enter_default_pin()
        keycard_flow.enter_default_pin()
        keycard_flow.wait_for_element_starts_with_text('Write codes down')
        pair_code = keycard_flow.pair_code_text.text
        keycard_flow.cancel_button.click()
        if not keycard_flow.element_by_translation_id("keycard-cancel-setup-title").is_element_displayed():
            self.driver.fail('No Dangerous operation popup is shown on canceling operation from Pair code stage')
        keycard_flow.yes_button.click()

        sign_in.just_fyi('Finish setup and relogin')
        keycard_flow.begin_setup_button.click()
        if not keycard_flow.element_by_text_part('5 free pairing slots').is_element_displayed():
            self.errors.append('Number of free pairing slots is not shown or wrong')
        keycard_flow.pair_code_input.set_value(pair_code)
        keycard_flow.pair_to_this_device_button.click()
        keycard_flow.enter_default_pin()
        for element in sign_in.maybe_later_button, sign_in.lets_go_button:
            element.wait_for_visibility_of_element(30)
            element.click()
        sign_in.profile_button.wait_for_visibility_of_element(30)
        public_key, default_username = sign_in.get_public_key_and_username(return_username=True)
        profile_view = sign_in.get_profile_view()
        if public_key != basic_user['public_key']:
            self.errors.append('Public key %s does not match expected' % public_key)
        if default_username != basic_user['username']:
            self.errors.append('Default username %s does not match expected' % default_username)
        profile_view.logout()
        home = sign_in.sign_in(keycard=True)
        if not home.wallet_button.is_element_displayed(10):
            self.errors.append("Failed to login to Keycard account")
        self.errors.verify_no_errors()

    @marks.testrail_id(5758)
    @marks.high
    def test_keycard_can_recover_keycard_account_card_pairing(self):
        sign_in = SignInView(self.driver)
        recovered_user = transaction_senders['A']

        sign_in.just_fyi('Recover multiaccount')
        sign_in.get_started_button.click_until_presence_of_element(sign_in.access_key_button)
        sign_in.access_key_button.click()
        sign_in.recover_with_keycard_button.click()
        keycard_view = sign_in.begin_recovery_button.click()
        keycard_view.connect_pairing_card_button.click()
        keycard_view.pair_code_input.set_value(pair_code)
        sign_in.pair_to_this_device_button.click()
        keycard_view.enter_default_pin()
        sign_in.home_button.wait_for_visibility_of_element(30)

        sign_in.just_fyi('Check assets after pairing keycard for recovered multiaccount')
        wallet_view = sign_in.wallet_button.click()
        wallet_view.set_up_wallet()
        for asset in ['ETH', 'LXS']:
            if wallet_view.get_asset_amount_by_name(asset) == 0:
                self.errors.append("%s value is not restored" % asset)

        sign_in.just_fyi('Check that wallet address matches expected for recovered multiaccount')
        address = wallet_view.get_wallet_address()
        if str(address).lower() != '0x%s' % recovered_user['address']:
            self.errors.append('Restored address %s does not match expected' % address)

        sign_in.just_fyi('Check that username and public key match expected for recovered multiaccount')
        public_key, default_username = sign_in.get_public_key_and_username(return_username=True)
        profile = sign_in.get_profile_view()
        if public_key != recovered_user['public_key']:
            self.errors.append('Public key %s does not match expected' % public_key)
        if default_username != recovered_user['username']:
            self.errors.append('Default username %s does not match expected' % default_username)
        profile.logout()

        sign_in.just_fyi('Check that can login with recovered keycard account')
        sign_in.sign_in(keycard=True)
        if not sign_in.home_button.is_element_displayed(10):
            self.driver.fail('Keycard user is not logged in')

        self.errors.verify_no_errors()

    @marks.testrail_id(6243)
    @marks.medium
    def test_keycard_can_recover_keycard_account_offline_and_add_watch_only_acc(self):
        sign_in = SignInView(self.driver)
        sign_in.toggle_airplane_mode()

        sign_in.just_fyi('Recover multiaccount offline')
        sign_in.get_started_button.click_until_presence_of_element(sign_in.access_key_button)
        sign_in.access_key_button.click()
        sign_in.recover_with_keycard_button.click()
        keycard_view = sign_in.begin_recovery_button.click()
        keycard_view.connect_pairing_card_button.click()
        keycard_view.pair_code_input.set_value(pair_code)
        sign_in.pair_to_this_device_button.click()
        keycard_view.enter_default_pin()
        sign_in.home_button.wait_for_visibility_of_element(30)
        wallet_view = sign_in.wallet_button.click()
        wallet_view.set_up_wallet()

        sign_in.just_fyi('Relogin offline')
        self.driver.close_app()
        self.driver.launch_app()
        sign_in.sign_in(keycard=True)
        if not sign_in.home_button.is_element_displayed(10):
            self.driver.fail('Keycard user is not logged in')

        sign_in.just_fyi('Turn off airplane mode and turn on cellular network')
        sign_in.toggle_airplane_mode()
        sign_in.toggle_mobile_data()
        sign_in.element_by_text_part('Stop syncing').wait_and_click(60)
        sign_in.wallet_button.click()
        if wallet_view.asset_by_name('LXS').is_element_displayed():
            self.errors.append('Token balance is fetched while on cellular network!')

        wallet_view.just_fyi('Add watch-only account when on cellular network')
        wallet_view.add_account_button.click()
        wallet_view.add_watch_only_address_button.click()
        wallet_view.enter_address_input.send_keys(basic_user['address'])
        account_name = 'watch-only'
        wallet_view.account_name_input.send_keys(account_name)
        wallet_view.add_account_generate_account_button.click()
        account_button = wallet_view.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.driver.fail('Account was not added')

        wallet_view.just_fyi('Check that balance is changed after go back to WI-FI')
        sign_in.toggle_mobile_data()
        for asset in ('ADI', 'STT'):
            wallet_view.asset_by_name(asset).scroll_to_element()
            wallet_view.wait_balance_is_changed(asset, wait_time=60)

        wallet_view.just_fyi('Delete watch-only account')
        wallet_view.get_account_by_name(account_name).click()
        wallet_view.get_account_options_by_name(account_name).click()
        wallet_view.account_settings_button.click()
        wallet_view.delete_account_button.click()
        wallet_view.yes_button.click()
        if wallet_view.get_account_by_name(account_name).is_element_displayed(20):
            self.errors.append('Account was not deleted')

        self.errors.verify_no_errors()


    @marks.testrail_id(6311)
    @marks.medium
    def test_same_seed_added_inside_multiaccount_and_keycard(self):
        sign_in = SignInView(self.driver)
        recipient = "0x" + transaction_senders['G']['address']

        sign_in.just_fyi('Restore keycard multiaccount and logout')
        sign_in.recover_access(passphrase=basic_user['passphrase'], keycard=True)
        profile_view = sign_in.profile_button.click()
        profile_view.logout()

        sign_in.just_fyi('Create new multiaccount')
        sign_in.your_keys_more_icon.click()
        sign_in.generate_new_key_button.click()
        sign_in.generate_key_button.click()
        sign_in.next_button.click()
        sign_in.next_button.click()
        sign_in.create_password_input.set_value(common_password)
        sign_in.next_button.click()
        sign_in.confirm_your_password_input.set_value(common_password)
        sign_in.next_button.click()
        sign_in.maybe_later_button.click_until_presence_of_element(sign_in.lets_go_button)
        sign_in.lets_go_button.click()

        sign_in.just_fyi('Add to wallet seed phrase for restored multiaccount')
        wallet_view = sign_in.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.add_account_button.click()
        wallet_view.enter_a_seed_phrase_button.click()
        wallet_view.enter_your_password_input.send_keys(common_password)
        account_name = 'subacc'
        wallet_view.account_name_input.send_keys(account_name)
        wallet_view.enter_seed_phrase_input.set_value(basic_user['passphrase'])
        wallet_view.add_account_generate_account_button.click()
        wallet_view.get_account_by_name(account_name).click()

        sign_in.just_fyi('Send transaction from added account and log out')
        transaction_amount_added = wallet_view.get_unique_amount()
        wallet_view.send_transaction(amount=transaction_amount_added, recipient=recipient, sign_transaction=True)
        wallet_view.profile_button.click()
        profile_view.logout()

        sign_in.just_fyi('Login to keycard account and send another transaction')
        sign_in.sign_in(position=2, keycard=True)
        sign_in.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.wait_balance_is_changed('ETH')
        wallet_view.accounts_status_account.click()
        transaction_amount_keycard = wallet_view.get_unique_amount()
        wallet_view.send_transaction(amount=transaction_amount_keycard, recipient=recipient, keycard=True, sign_transaction=True)

        sign_in.just_fyi('Check both transactions from keycard multiaccount and from added account in network')
        for amount in [transaction_amount_keycard, transaction_amount_added]:
            self.network_api.find_transaction_by_unique_amount(basic_user['address'], amount)

        self.errors.verify_no_errors()