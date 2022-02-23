import random
import pytest

from support.utilities import fill_string_with_char
from tests import marks, common_password, unique_password
from tests.base_test_case import SingleDeviceTestCase, MultipleSharedDeviceTestCase, create_shared_drivers
from views.sign_in_view import SignInView
from tests.users import basic_user, transaction_senders, recovery_users


@pytest.mark.xdist_group(name="onboarding_1")
class TestOnboardingOneDeviceMerged(MultipleSharedDeviceTestCase):

    @classmethod
    def setup_class(cls):
        cls.drivers, cls.loop = create_shared_drivers(1)
        cls.sign_in = SignInView(cls.drivers[0])
        cls.password = basic_user['special_chars_password']

        cls.home = cls.sign_in.create_user(password=cls.password)
        cls.public_chat_name = cls.home.get_random_chat_name()
        cls.chat = cls.home.join_public_chat(cls.public_chat_name)
        cls.profile = cls.home.profile_button.click()
        cls.username = cls.profile.default_username_text.text

    @marks.testrail_id(700742)
    @marks.critical
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
    @marks.critical
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
        if not self.profile.element_by_text_part(public_key).is_element_present():
            self.errors.append("Can't share public key")
        self.profile.click_system_back_button()

        self.home.just_fyi("Check that can paste contact code in chat message input")
        home = self.profile.home_button.click()
        chat = home.add_contact(transaction_senders['M']['public_key'])
        chat.chat_message_input.click()
        chat.paste_text()
        input_text = chat.chat_message_input.text
        if input_text not in public_key or len(input_text) < 1:
            self.errors.append('Public key was not copied')
        chat.chat_message_input.clear()
        self.errors.verify_no_errors()

    @marks.testrail_id(700744)
    @marks.critical
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
        if not wallet.element_by_text_part(address).is_element_present():
            self.errors.append("Can't share address")
        wallet.click_system_back_button()

        self.home.just_fyi("Check that can paste wallet address in chat message input")
        wallet.home_button.click()
        self.home.get_chat('#%s' % self.public_chat_name).click()
        self.chat.chat_message_input.click()
        self.chat.paste_text()
        if self.chat.chat_message_input.text != address:
            self.errors.append('Wallet address was not copied')
        self.chat.chat_message_input.clear()
        self.errors.verify_no_errors()

    @marks.testrail_id(700745)
    @marks.critical
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
        profile.recovery_phrase_word_input.set_value(recovery_phrase[word_number])
        profile.next_button.click()
        word_number_1 = profile.recovery_phrase_word_number.number
        profile.recovery_phrase_word_input.set_value(recovery_phrase[word_number_1])
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
        self.sign_in.back_button.click()
        self.sign_in.access_key_button.click()
        self.sign_in.enter_seed_phrase_button.click()
        self.sign_in.seedphrase_input.click()
        self.sign_in.seedphrase_input.set_value(' '.join(recovery_phrase.values()))
        self.sign_in.next_button.click()
        self.sign_in.element_by_translation_id(translation_id="unlock", uppercase=True).click()
        self.sign_in.password_input.set_value(self.password)
        self.sign_in.sign_in_button.click()
        self.home.plus_button.wait_and_click()
        if not self.home.start_new_chat_button.is_element_displayed():
            self.errors.append("Can't proceed using account after it's re-recover twice.")
        self.home.click_system_back_button()
        self.errors.verify_no_errors()

    @marks.testrail_id(700746)
    @marks.critical
    def test_onboarding_cant_sign_in_with_invalid_password_logcat(self):
        self.home.profile_button.click()
        self.profile.logout()

        self.sign_in.just_fyi('Check that cannot login with incorrect password, and can login with valid data')
        if self.sign_in.ok_button.is_element_displayed():
            self.sign_in.ok_button.click()
        self.sign_in.multi_account_on_login_button.click()
        self.sign_in.password_input.set_value(common_password)
        self.sign_in.sign_in_button.click()
        self.sign_in.element_by_translation_id("wrong-password").wait_for_visibility_of_element(20)
        if not self.sign_in.element_by_text(self.username).is_element_displayed():
            self.errors.append('Username is not shown while login')
        self.sign_in.password_input.set_value(self.password)
        self.sign_in.sign_in_button.click()
        if not self.sign_in.home_button.is_element_displayed(10):
            self.errors.append('User is not logged in')
        values_in_logcat = self.sign_in.find_values_in_logcat(password=self.password)
        if values_in_logcat:
            self.errors.append(values_in_logcat)
        self.errors.verify_no_errors()

    @marks.testrail_id(700747)
    @marks.critical
    def test_onboarding_add_new_multiaccount_username_by_position_pass_validation(self):
        self.home.profile_button.click()
        self.profile.logout()

        self.sign_in.just_fyi('Create another multiaccount')
        if self.sign_in.ok_button.is_element_displayed():
            self.sign_in.ok_button.click()
        self.sign_in.back_button.click()
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
        [field.send_keys('123456') for field in (self.sign_in.create_password_input, self.sign_in.confirm_your_password_input)]
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
        self.sign_in.create_password_input.set_value(common_password)
        self.sign_in.confirm_your_password_input.set_value(common_password)

        self.sign_in.next_button.click()
        [element.wait_and_click(10) for element in (self.sign_in.maybe_later_button, self.sign_in.lets_go_button)]
        self.home.cross_icon_inside_welcome_screen_button.wait_and_click(10)
        if not self.home.element_by_translation_id("welcome-blank-message").is_element_displayed():
            self.errors.append("'%s' text is not shown after welcome view was closed" % self.home.get_translation_by_key(
                "welcome-blank-message"))
        self.home.profile_button.click()
        shown_username = self.profile.default_username_text.text
        if shown_username != username:
            self.errors.append("Default username '%s' doesn't match '%s'" % (shown_username, username))
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="recover_1")
class TestRecoverOneDeviceMerged(MultipleSharedDeviceTestCase):
    @classmethod
    def setup_class(cls):
        cls.user = transaction_senders['A']
        cls.drivers, cls.loop = create_shared_drivers(1)
        cls.sign_in = SignInView(cls.drivers[0])
        cls.passphrase = fill_string_with_char(cls.user['passphrase'].upper(), ' ', 3, True, True)
        cls.password = basic_user['special_chars_password']

        cls.home = cls.sign_in.recover_access(passphrase=cls.passphrase, password=cls.password)

    @marks.testrail_id(700748)
    @marks.critical
    def test_recover_uppercase_whitespaces_seed_phrase_special_char_passw_logcat(self):
        profile = self.home.profile_button.click()
        public_key, username = self.sign_in.get_public_key_and_username(return_username=True)

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
    @marks.critical
    def test_recover_set_up_wallet_sign_phrase(self):
        wallet = self.sign_in.wallet_button.click()

        wallet.just_fyi("Initiating some transaction so the wallet signing phrase pop-up appears")
        wallet.accounts_status_account.click()
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        send_transaction.amount_edit_box.set_value("0")
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
        send_transaction.amount_edit_box.set_value('0')
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
    @marks.critical
    def test_recover_validation_seed_phrase_field(self):
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
        profile = self.home.profile_button.click()
        profile.logout()
        if self.sign_in.ok_button.is_element_displayed():
            self.sign_in.ok_button.click()
        self.sign_in.back_button.click()
        self.sign_in.access_key_button.click()
        self.sign_in.element_by_translation_id("recover-with-seed-phrase").click()

        self.sign_in.just_fyi("check that seed phrase is required (can't be empty)")
        self.sign_in.next_button.click()
        if self.sign_in.element_by_translation_id('keycard-recovery-success-header').is_element_displayed():
            self.errors.append("Possible to create account with empty seed phrase")
        for validation in validations:
            self.sign_in.just_fyi("Checking %s" % validation.get('case'))
            phrase, msg, words_count, popup = validation.get('phrase'), \
                                              validation.get('validation message'), \
                                              validation.get('words count'), \
                                              validation.get('popup')
            if self.sign_in.access_key_button.is_element_displayed():
                self.sign_in.access_key_button.click()
            if self.sign_in.enter_seed_phrase_button.is_element_displayed():
                self.sign_in.enter_seed_phrase_button.click()
            self.sign_in.seedphrase_input.set_value(phrase)

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


class TestCreateAccount(SingleDeviceTestCase):

    @marks.testrail_id(5455)
    @marks.medium
    def test_recover_accounts_with_certain_seedphrase(self):
        sign_in = SignInView(self.driver)
        for phrase, account in recovery_users.items():
            home_view = sign_in.recover_access(passphrase=phrase, password=unique_password)
            wallet_view = home_view.wallet_button.click()
            address = wallet_view.get_wallet_address()
            if address != account:
                self.errors.append('Restored wallet address "%s" does not match expected "%s"' % (address, account))
            profile = home_view.profile_button.click()
            profile.privacy_and_security_button.click()
            profile.delete_my_profile_button.scroll_and_click()
            profile.delete_my_profile_password_input.set_value(unique_password)
            profile.delete_profile_button.click()
            profile.ok_button.click()
        self.errors.verify_no_errors()
