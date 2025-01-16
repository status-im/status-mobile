import random
import pytest

from support.utilities import fill_string_with_char
from tests import marks, common_password
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from views.sign_in_view import SignInView
from tests.users import basic_user, transaction_senders


@pytest.mark.xdist_group(name="one_1")
@marks.critical
class TestOnboardingOneDeviceMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])
        self.password = basic_user['special_chars_password']

        self.home = self.sign_in.create_user(password=self.password)
        self.public_chat_name = self.home.get_random_chat_name()
        self.chat = self.home.join_public_chat(self.public_chat_name)
        self.profile = self.home.profile_button.click()
        self.username = self.profile.default_username_text.text

    @marks.testrail_id(700742)
    def test_onboarding_home_initial_popup(self):
        self.home.home_button.double_click()
        texts = ["chat-and-transact", "invite-friends"]
        for text in texts:
            if not self.home.element_by_translation_id(text).is_element_displayed():
                self.errors.append("'%s' text is not shown" % self.get_translation_by_key(text))
        if self.home.element_by_text(texts[0]).is_element_displayed():
            self.errors.append("'%s' text is shown, but welcome view was closed" % texts[0])
        self.home.relogin(password=self.password)
        if self.home.element_by_text(texts[0]).is_element_displayed():
            self.errors.append("'%s' text is shown after relogin, but welcome view was closed" % texts[0])
        self.errors.verify_no_errors()

    @marks.testrail_id(700743)
    def test_onboarding_share_contact_address(self):
        self.profile = self.home.profile_button.click()

        self.home.just_fyi("Copying contact code")
        self.profile.share_my_profile_button.click()
        public_key = self.profile.public_key_text.text
        self.profile.public_key_text.long_press_element()
        self.profile.copy_text()

        self.home.just_fyi("Sharing contact code via messenger")
        self.profile.share_button.click()
        self.profile.share_via_messenger()
        if not self.profile.element_by_text_part(public_key).is_element_displayed():
            self.errors.append("Can't share public key")
        self.profile.navigate_back_to_home_view()

        self.home.just_fyi("Check that can paste contact code in chat message input")
        self.profile.home_button.double_click()
        chat = self.home.add_contact(transaction_senders['M']['public_key'])
        chat.chat_message_input.click()
        chat.paste_text()
        input_text = chat.chat_message_input.text
        if input_text not in public_key or len(input_text) < 1:
            self.errors.append('Public key was not copied')
        chat.chat_message_input.clear()
        self.errors.verify_no_errors()

    @marks.testrail_id(700744)
    def test_onboarding_share_wallet_address(self):
        self.home.just_fyi("Copying wallet address")
        wallet = self.home.wallet_button.click()
        wallet.accounts_status_account.click()
        request = wallet.receive_transaction_button.click()
        address = wallet.address_text.text
        request.share_button.click()
        request.element_by_translation_id("sharing-copy-to-clipboard").click()

        self.home.just_fyi("Sharing wallet address via messenger")
        request.share_button.click()
        wallet.share_via_messenger()
        if not wallet.element_by_text_part(address).is_element_displayed():
            self.errors.append("Can't share address")
        wallet.navigate_back_to_home_view()

        self.home.just_fyi("Check that can paste wallet address in chat message input")
        wallet.home_button.click()
        if not self.chat.chat_message_input.is_element_displayed():
            self.home.get_chat('#%s' % self.public_chat_name).click()
        self.chat.chat_message_input.click()
        self.chat.paste_text()
        if self.chat.chat_message_input.text != address:
            self.errors.append('Wallet address was not copied')
        self.chat.chat_message_input.clear()
        self.errors.verify_no_errors()

    @marks.testrail_id(700745)
    def test_onboarding_backup_seed_phrase_restore_same_login_logcat(self):
        self.home.just_fyi("Check that badge on profile about back up seed phrase is presented")
        if self.home.profile_button.counter.text != '1':
            self.errors.append('Profile button counter is not shown')

        self.home.just_fyi("Back up seed phrase and check logcat")
        profile = self.home.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.backup_recovery_phrase_button.click()
        profile.ok_continue_button.click()
        recovery_phrase = profile.get_recovery_phrase()
        profile.next_button.click()
        word_number = profile.recovery_phrase_word_number.number
        profile.recovery_phrase_word_input.send_keys(recovery_phrase[word_number])
        profile.next_button.click()
        word_number_1 = profile.recovery_phrase_word_number.number
        profile.recovery_phrase_word_input.send_keys(recovery_phrase[word_number_1])
        profile.done_button.click()
        profile.yes_button.click()
        profile.ok_got_it_button.click()
        if self.home.profile_button.counter.is_element_displayed():
            self.errors.append('Profile button counter is shown after recovery phrase backup')
        values_in_logcat = profile.find_values_in_logcat(passphrase1=recovery_phrase[word_number],
                                                         passphrase2=recovery_phrase[word_number_1])
        if len(values_in_logcat) == 2:
            self.errors.append(values_in_logcat)
        profile.profile_button.double_click()

        self.home.just_fyi(
            "Try to restore same account from seed phrase (should be possible only to unlock existing account)")
        self.profile.logout()
        self.sign_in.navigate_up_button.click()
        self.sign_in.access_key_button.click()
        self.sign_in.enter_seed_phrase_button.click()
        self.sign_in.seedphrase_input.click()
        self.sign_in.seedphrase_input.send_keys(' '.join(recovery_phrase.values()))
        self.sign_in.next_button.click()
        self.sign_in.element_by_translation_id(translation_id="unlock", uppercase=True).click()
        self.sign_in.password_input.send_keys(self.password)
        self.sign_in.sign_in_button.click()
        self.home.plus_button.wait_and_click()
        if not self.home.start_new_chat_button.is_element_displayed():
            self.errors.append("Can't proceed using account after it's re-recover twice.")
        self.home.click_system_back_button()
        self.errors.verify_no_errors()

    @marks.testrail_id(700746)
    def test_onboarding_cant_sign_in_with_invalid_password_logcat(self):
        self.home.profile_button.double_click()
        self.profile.logout()

        self.sign_in.just_fyi('Check that cannot login with incorrect password, and can login with valid data')
        if self.sign_in.ok_button.is_element_displayed():
            self.sign_in.ok_button.click()
        self.sign_in.multi_account_on_login_button.click()
        self.sign_in.password_input.send_keys(common_password)
        self.sign_in.sign_in_button.click()
        self.sign_in.element_by_translation_id("wrong-password").wait_for_visibility_of_element(20)
        if not self.sign_in.element_by_text(self.username).is_element_displayed():
            self.errors.append('Username is not shown while login')
        self.sign_in.password_input.send_keys(self.password)
        self.sign_in.sign_in_button.click()
        if not self.sign_in.home_button.is_element_displayed(10):
            self.errors.append('User is not logged in')
        values_in_logcat = self.sign_in.find_values_in_logcat(password=self.password)
        if values_in_logcat:
            self.errors.append(values_in_logcat)
        self.errors.verify_no_errors()

    @marks.testrail_id(700747)
    def test_onboarding_add_new_multiaccount_username_by_position_pass_validation(self):
        self.home.profile_button.double_click()
        self.profile.logout()

        self.sign_in.just_fyi('Create another multiaccount')
        if self.sign_in.ok_button.is_element_displayed():
            self.sign_in.ok_button.click()
        self.sign_in.navigate_up_button.click()
        self.sign_in.your_keys_more_icon.click()
        self.sign_in.generate_new_key_button.click()
        from views.sign_in_view import MultiAccountButton
        account_button = self.sign_in.get_multiaccount_by_position(position=random.randint(1, 4),
                                                                   element_class=MultiAccountButton)
        username = account_button.username.text
        account_button.click()
        self.sign_in.next_button.click()
        self.sign_in.next_button.click()

        self.sign_in.just_fyi('Check password validation')
        cases = ['password is not confirmed', 'password is too short', "passwords don't match"]
        error = "Can create multiaccount when"

        self.sign_in.just_fyi('Checking case when %s' % cases[0])
        self.sign_in.create_password_input.send_keys('123456')
        self.sign_in.next_button.click()
        if self.sign_in.maybe_later_button.is_element_displayed(10):
            self.driver.fail('%s  %s' % (error, cases[0]))

        self.sign_in.just_fyi('Checking case when %s' % cases[1])
        self.sign_in.create_password_input.send_keys('123456')
        [field.send_keys('123456') for field in
         (self.sign_in.create_password_input, self.sign_in.confirm_your_password_input)]
        self.sign_in.confirm_your_password_input.delete_last_symbols(1)
        self.sign_in.create_password_input.delete_last_symbols(1)
        self.sign_in.next_button.click()
        if self.sign_in.maybe_later_button.is_element_displayed(10):
            self.driver.fail('%s  %s' % (error, cases[1]))

        self.sign_in.just_fyi("Checking case %s" % cases[2])
        self.sign_in.create_password_input.send_keys('1234565')
        self.sign_in.confirm_your_password_input.send_keys('1234567')
        if not self.sign_in.element_by_translation_id("password_error1").is_element_displayed():
            self.errors.append("'%s' is not shown" % self.sign_in.get_translation_by_key("password_error1"))
        self.sign_in.create_password_input.send_keys(common_password)
        self.sign_in.confirm_your_password_input.send_keys(common_password)

        self.sign_in.next_button.click()
        [element.wait_and_click(10) for element in (self.sign_in.maybe_later_button, self.sign_in.start_button)]
        self.home.cross_icon_inside_welcome_screen_button.wait_and_click(10)
        if not self.home.element_by_translation_id("welcome-blank-message").is_element_displayed():
            self.errors.append("'%s' text is not shown after welcome view was closed" %
                               self.home.get_translation_by_key("welcome-blank-message"))
        self.home.profile_button.click()
        shown_username = self.profile.default_username_text.text
        if shown_username != username:
            self.errors.append("Default username '%s' doesn't match '%s'" % (shown_username, username))
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="two_1")
@marks.critical
class TestRestoreOneDeviceMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.user = transaction_senders['ETH_ADI_STT_2']
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])
        self.passphrase = fill_string_with_char(self.user['passphrase'].upper(), ' ', 3, True, True)
        self.password = basic_user['special_chars_password']
        self.assets = ['ETH', 'YEENUS', 'STT']
        self.home = self.sign_in.recover_access(passphrase=self.passphrase, password=self.password)

    @marks.testrail_id(700748)
    def test_restore_uppercase_whitespaces_seed_phrase_special_char_passw_logcat(self):
        profile = self.home.profile_button.click()
        public_key, username = self.sign_in.get_public_key()

        self.sign_in.just_fyi("Check public key matches expected and no back up seed phrase is available")
        profile.privacy_and_security_button.click()
        profile.backup_recovery_phrase_button.click()
        if not profile.backup_recovery_phrase_button.is_element_displayed():
            self.errors.append('Back up seed phrase option is active for recovered account!')
        if username != self.user['username'] or public_key != self.user['public_key']:
            self.drivers[0].fail('Incorrect user was recovered')
        values_in_logcat = self.sign_in.find_values_in_logcat(passphrase=self.passphrase, password=self.password)
        if values_in_logcat:
            self.errors.append(values_in_logcat)
        self.errors.verify_no_errors()

    @marks.testrail_id(700749)
    def test_restore_set_up_wallet_sign_phrase(self):
        wallet = self.sign_in.wallet_button.click()

        wallet.just_fyi("Initiating some transaction so the wallet signing phrase pop-up appears")
        wallet.accounts_status_account.click()
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        send_transaction.amount_edit_box.send_keys("0")
        send_transaction.set_recipient_address("0x" + basic_user['address'])
        send_transaction.sign_transaction_button.click()

        texts = list(map(self.sign_in.get_translation_by_key,
                         ["this-is-you-signing", "three-words-description", "three-words-description-2"]))

        wallet.just_fyi('Check required text in set up wallet popup')
        for text in texts:
            if not wallet.element_by_text_part(text).is_element_displayed():
                self.errors.append("'%s' text is not displayed" % text)
        phrase = wallet.sign_in_phrase.list
        if len(phrase) != 3:
            self.errors.append('Transaction phrase length is %s' % len(phrase))

        wallet.just_fyi('Check popup will reappear if tap on "Remind me later"')
        wallet.remind_me_later_button.click()
        send_transaction.cancel_button.click()
        wallet.wallet_button.click()
        wallet.accounts_status_account.click()
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.amount_edit_box.send_keys('0')
        send_transaction.set_recipient_address('0x%s' % basic_user['address'])
        send_transaction.next_button.click_until_presence_of_element(send_transaction.sign_transaction_button)
        send_transaction.sign_transaction_button.click()
        for text in texts:
            if not wallet.element_by_text_part(text).is_element_displayed():
                self.errors.append("'%s' text is not displayed" % text)
        phrase_1 = wallet.sign_in_phrase.list
        if phrase_1 != phrase:
            self.errors.append("Transaction phrase '%s' doesn't match expected '%s'" % (phrase_1, phrase))
        wallet.ok_got_it_button.click()
        wallet.cancel_button.click()
        wallet.home_button.click()
        wallet.wallet_button.click()
        for text in texts:
            if wallet.element_by_text_part(text).is_element_displayed():
                self.errors.append('Signing phrase pop up appears after wallet set up')
                break
        self.errors.verify_no_errors()

    @marks.testrail_id(700750)
    def test_restore_seed_phrase_field_validation(self):
        validations = [
            {
                'case': 'empty value',
                'phrase': '    ',
                'validation message': 'Required field',
                'words count': 1,
                'popup': False
            },
            {
                'case': '1 word seed',
                'phrase': 'a',
                'validation message': '',
                'words count': 1,
                'popup': False
            },
            {
                'case': 'mnemonic but checksum validation fails',
                'phrase': 'one two three four five six seven eight nine ten eleven twelve',
                'validation message': '',
                'words count': 12,
                'popup': True
            },
        ]
        self.home.driver.close_app()
        self.home.driver.launch_app()
        if self.sign_in.ok_button.is_element_displayed():
            self.sign_in.ok_button.click()
        self.sign_in.navigate_up_button.click()
        self.sign_in.access_key_button.click()
        self.sign_in.element_by_translation_id("recover-with-seed-phrase").click()

        self.sign_in.just_fyi("check that seed phrase is required (can't be empty)")
        self.sign_in.next_button.click()
        if self.sign_in.element_by_translation_id('keycard-recovery-success-header').is_element_displayed():
            self.errors.append("Possible to create account with empty seed phrase")
        for validation in validations:
            self.sign_in.just_fyi("Checking %s" % validation.get('case'))
            phrase, msg, words_count, popup = validation.get('phrase'), validation.get('validation message'), \
                                              validation.get('words count'), \
                                              validation.get('popup')
            if self.sign_in.access_key_button.is_element_displayed():
                self.sign_in.access_key_button.click()
            if self.sign_in.enter_seed_phrase_button.is_element_displayed():
                self.sign_in.enter_seed_phrase_button.click()
            self.sign_in.seedphrase_input.send_keys(phrase)

            if msg:
                if not self.sign_in.element_by_text(msg).is_element_displayed():
                    self.errors.append('"%s" message is not shown' % msg)

            self.sign_in.just_fyi('check that words count is shown')
            if words_count:
                if not self.sign_in.element_by_text('%s word' % words_count):
                    self.errors.append('"%s word" is not shown ' % words_count)
            else:
                if not self.sign_in.element_by_text('%s words' % words_count):
                    self.errors.append('"%s words" is not shown ' % words_count)

            self.sign_in.just_fyi('check that "Next" is disabled unless we use allowed count of words')
            if words_count != 12 or 15 or 18 or 21 or 24:
                self.sign_in.next_button.click()
                if self.sign_in.element_by_translation_id('keycard-recovery-success-header').is_element_displayed():
                    self.errors.append("Possible to create account with wrong count (%s) of words" % words_count)

            self.sign_in.just_fyi('check behavior for popup "Custom seed phrase"')
            if popup:
                if not self.sign_in.custom_seed_phrase_label.is_element_displayed():
                    self.errors.append("Popup about custom seed phrase is not shown")
                self.sign_in.cancel_custom_seed_phrase_button.click()
            self.sign_in.click_system_back_button()
        self.errors.verify_no_errors()

    @marks.testrail_id(702189)
    def test_restore_account_migrate_multiaccount_to_keycard_no_db_saved_add_wallet_send_tx(self):
        self.sign_in.driver.close_app()
        self.sign_in.driver.launch_app()
        self.sign_in.sign_in(password=self.password)
        self.home.home_button.wait_for_visibility_of_element(30)
        profile = self.home.profile_button.click()
        profile.profile_button.double_click()
        profile.privacy_and_security_button.click()
        profile.element_by_translation_id("manage-keys-and-storage").scroll_and_click()
        profile.logout_dialog.logout_button.wait_and_click()
        profile.logout_button.wait_for_invisibility_of_element(30)
        if not self.sign_in.element_by_translation_id("move-keystore-file").is_element_displayed():
            self.errors.append("Was not redirected to Key management screen when Manage keys from logged in state!")

        self.home.just_fyi("Checking keycard banner and starting migrate multiaccount to keycard: no db saved")
        self.sign_in.close_button.click_if_shown()
        self.sign_in.navigate_up_button.click_if_shown()
        self.sign_in.multi_account_on_login_button.wait_for_visibility_of_element(30)
        self.sign_in.get_multiaccount_by_position(1).click()
        if not self.sign_in.get_keycard_banner.is_element_displayed():
            self.errors.append("Get a keycard banner is not shown on login screen for ordinary multiaccount")
        self.sign_in.options_button.click()
        self.sign_in.manage_keys_and_storage_button.click()
        if not self.sign_in.element_by_text(self.user['username']).is_element_displayed():
            self.driver.fail("Default username is not shown when migrating multiaccount to keycard!")

        self.home.just_fyi("Checking validation of seed phrase during migration")
        self.sign_in.enter_seed_phrase_next_button.click()
        if self.sign_in.seedphrase_input.is_element_displayed():
            self.sign_in.driver.fail("Proceeded to seedphrase input without confirmed Actions")
        self.sign_in.move_keystore_file_option.click()
        self.sign_in.reset_database_checkbox.click()
        self.sign_in.enter_seed_phrase_next_button.click()
        self.sign_in.seedphrase_input.send_keys(transaction_senders['A']['passphrase'])
        self.sign_in.choose_storage_button.click()
        if not self.sign_in.element_by_translation_id("seed-key-uid-mismatch").is_element_displayed():
            self.driver.fail("Can proceed with seed phrase of another user")
        self.sign_in.element_by_translation_id("try-again").click()
        self.sign_in.seedphrase_input.send_keys(self.user['passphrase'][:-1])
        self.sign_in.choose_storage_button.click()
        if not self.sign_in.custom_seed_phrase_label.is_element_displayed():
            self.driver.fail("Can proceed with invalid seed phrase")
        self.sign_in.cancel_button.click()
        self.sign_in.seedphrase_input.send_keys(self.user['passphrase'])
        self.sign_in.choose_storage_button.click()
        if not self.sign_in.get_keycard_banner.is_element_displayed():
            self.errors.append("Get a keycard banner is not shown on Key management screen")
        self.sign_in.keycard_required_option.click()
        if self.sign_in.get_keycard_banner.is_element_displayed():
            self.errors.append("Get a keycard banner is shown when keycard storage is chosen")

        self.sign_in.just_fyi("Finishing migration to keycard")
        self.sign_in.confirm_button.click()
        keycard = self.sign_in.move_and_reset_button.click()
        keycard.begin_setup_button.click()
        keycard.connect_card_button.wait_and_click()
        keycard.enter_default_pin()
        keycard.enter_default_pin()
        if not self.sign_in.element_by_translation_id("migration-successful").is_element_displayed(30):
            self.driver.fail("No popup about successfull migration is shown!")
        self.sign_in.ok_button.click()
        self.sign_in.maybe_later_button.wait_and_click(30)
        self.sign_in.start_button.wait_and_click(30)

        self.sign_in.just_fyi('Check that after migrating account with assets is restored')
        wallet = self.sign_in.wallet_button.click()
        for asset in self.assets:
            if wallet.get_asset_amount_by_name(asset) == 0:
                self.errors.append('Asset %s was not restored' % asset)

        self.sign_in.just_fyi('Check that after migration wallet address matches expected')
        address = wallet.get_wallet_address()
        if address != '0x%s' % self.user['address']:
            self.errors.append('Restored address %s does not match expected' % address)

        self.sign_in.just_fyi('Check that after migration username and public key match expected')
        public_key, default_username = self.sign_in.get_public_key()
        profile = self.sign_in.get_profile_view()
        if public_key != self.user['public_key']:
            self.errors.append('Public key %s does not match expected' % public_key)
        if default_username != self.user['username']:
            self.errors.append('Default username %s does not match expected' % default_username)
        profile.logout()

        self.sign_in.just_fyi(
            'Check that can login with migrated account, keycard banner is not shown and no option to migrate')
        self.sign_in.get_multiaccount_by_position(1).click()
        if self.sign_in.get_keycard_banner.is_element_displayed():
            self.errors.append("Get a keycard banner is shown on migrated keycard multiaccount")
        keycard.one_button.wait_for_visibility_of_element(10)
        keycard.enter_default_pin()
        if not self.sign_in.home_button.is_element_displayed(30):
            self.driver.fail('Keycard user is not logged in')

        self.sign_in.just_fyi('Check that can add another wallet account and send transaction')
        self.home.wallet_button.click()
        wallet.add_account(account_name="another_keycard_account", keycard=True)
        transaction_amount_added = wallet.get_unique_amount()
        wallet.send_transaction(amount=transaction_amount_added, recipient=transaction_senders['ETH_8']['address'],
                                keycard=True, sign_transaction=True)
        self.errors.verify_no_errors()