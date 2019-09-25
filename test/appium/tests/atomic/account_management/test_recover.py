import pytest

from support.utilities import fill_string_with_char
from tests import marks, unique_password
from tests.base_test_case import SingleDeviceTestCase
from tests.users import basic_user, transaction_senders, recovery_users
from views.sign_in_view import SignInView
from views.recover_access_view import RecoverAccessView


@marks.all
@marks.account
class TestRecoverAccountSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(5301)
    @marks.critical
    @marks.battery_consumption
    @marks.skip
    def test_recover_account(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        public_key = home.get_public_key()
        profile = home.get_profile_view()
        profile.backup_recovery_phrase_button.click()
        profile.ok_continue_button.click()
        recovery_phrase = profile.get_recovery_phrase()
        profile.back_button.click()
        wallet = profile.wallet_button.click()
        wallet.set_up_wallet()
        address = wallet.get_wallet_address()
        self.driver.reset()
        sign_in.accept_agreements()
        sign_in.recover_access(passphrase=' '.join(recovery_phrase.values()))
        home.wallet_button.click()
        wallet.set_up_wallet()
        address2 = wallet.get_wallet_address()
        if address2 != address:
            self.errors.append('Wallet address is %s after recovery, but %s is expected' % (address2, address))
        public_key2 = wallet.get_public_key()
        if public_key2 != public_key:
            self.errors.append('Public key is %s after recovery, but %s is expected' % (public_key2, public_key))
        self.verify_no_errors()

    @marks.skip
    @marks.testrail_id(845)
    def test_recover_account_with_incorrect_passphrase(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        public_key = sign_in.get_public_key()
        profile = sign_in.get_profile_view()
        profile.backup_recovery_phrase_button.click()
        profile.ok_continue_button.click()
        recovery_phrase = profile.get_recovery_phrase()

        self.driver.reset()
        sign_in.accept_agreements()
        sign_in.recover_access(passphrase=' '.join(list(recovery_phrase.values())[::-1]))
        if sign_in.get_public_key() == public_key:
            pytest.fail('The same account is recovered with reversed passphrase')

    @marks.logcat
    @marks.testrail_id(5366)
    @marks.critical
    def test_logcat_recovering_account(self):
        sign_in = SignInView(self.driver)
        sign_in.recover_access(passphrase=basic_user['passphrase'], password=unique_password)
        values_in_logcat = sign_in.find_values_in_logcat(passphrase=basic_user['passphrase'], password=unique_password)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)


class TestRecoverAccessFromSignInScreen(SingleDeviceTestCase):
    @marks.testrail_id(5363)
    @marks.high
    def test_pass_phrase_validation(self):
        signin_view = SignInView(self.driver)
        signin_view.access_key_button.click()
        recover_access_view = RecoverAccessView(self.driver)
        validations = [
            # empty value
            {
                'phrase': '    ',
                'element to check': recover_access_view.warnings.invalid_recovery_phrase,
                'validation message': 'Required field',
                'words count': 1,
                'popup': False
            },
            # invalid seed phrase
            {
                'phrase': 'a',
                'element to check': recover_access_view.warnings.invalid_recovery_phrase,
                'validation message': 'Seed phrase is invalid',
                'words count': 1,
                'popup' : False
            },
            # outside mnemonic
            {
                'phrase': '; two three four five six seven eight nine ten eleven twelve',
                'element to check': recover_access_view.warnings.invalid_recovery_phrase,
                'validation message': '',
                'words count': 12,
                'popup': True
            },
        ]

        # check that seed phrase is required (can't be empty)
        recover_access_view.enter_seed_phrase_button.click()
        recover_access_view.next_button.click()
        if recover_access_view.reencrypt_your_key_button.is_element_displayed():
            self.errors.append("Possible to create account with empty seed phrase")

        # we're performing the same steps changing only phrase per attempt
        for validation in validations:
            phrase, elm, msg, words_count, popup = validation.get('phrase'), \
                                            validation.get('element to check'), \
                                            validation.get('validation message'), \
                                            validation.get('words count'),\
                                            validation.get('popup')
            if signin_view.access_key_button.is_element_displayed():
                signin_view.access_key_button.click()
            if recover_access_view.enter_seed_phrase_button.is_element_displayed():
                recover_access_view.enter_seed_phrase_button.click()

            recover_access_view.send_as_keyevent(phrase)

            # TODO: uncomment after 8567 fix
            #if msg and not elm.is_element_displayed():
            #     self.errors.append('"{}" message is not shown'.format(msg))

            # check that words count is shown
            if words_count == 1:
                if not signin_view.element_by_text('%s word' % words_count):
                    self.errors.append('"%s word" is not shown ' % words_count)
            else:
                if not signin_view.element_by_text('%s words' % words_count):
                    self.errors.append('"%s words" is not shown ' % words_count)

            # check that "Next" is disabled unless we use allowed count of words
            if words_count != 12 or 15 or 18 or 21 or 24:
                recover_access_view.next_button.click()
                if recover_access_view.reencrypt_your_key_button.is_element_displayed():
                    self.errors.append("Possible to create account with wrong count (%s) of words" % words_count)

            # check behavior for popup "Custom seed phrase"
            if popup:
                text = 'Custom seed phrase'
                common_password = 'qwerty'
                if not recover_access_view.find_full_text(text):
                    self.errors.append('"%s" text is not shown' % text)
                recover_access_view.cancel_custom_seed_phrase_button.click()
                recover_access_view.next_button.click()
                recover_access_view.continue_custom_seed_phrase_button.click()
                recover_access_view.reencrypt_your_key_button.click()
                recover_access_view.next_button.click()
                recover_access_view.create_password_input.set_value(common_password)
                recover_access_view.next_button.click()
                recover_access_view.confirm_your_password_input.set_value(common_password)
                recover_access_view.next_button.click_until_presence_of_element(recover_access_view.home_button)
            else:
                recover_access_view.click_system_back_button()

        self.verify_no_errors()

    @marks.testrail_id(5499)
    @marks.medium
    def test_passphrase_whitespaces_ignored_while_recovering_access(self):
        signin_view = SignInView(self.driver)
        sender = transaction_senders['U']
        passphrase = fill_string_with_char(sender['passphrase'], ' ', 3, True, True)
        home_view = signin_view.recover_access(passphrase=passphrase)

        if not home_view.profile_button.is_element_displayed():
            self.driver.fail('Something went wrong. Probably, could not reach the home screen out.')

    @marks.testrail_id(5394)
    @marks.high
    def test_account_recovery_with_uppercase_recovery_phrase(self):
        user = transaction_senders['A']
        passphrase = user['passphrase']
        capitalized_passphrase = passphrase.upper()
        signin_view = SignInView(self.driver)
        signin_view.recover_access(capitalized_passphrase)
        profile_view = signin_view.profile_button.click()
        username = profile_view.default_username_text.text
        public_key = signin_view.get_public_key()
        if username != user['username'] or public_key != user['public_key']:
            self.driver.fail('Incorrect user was recovered')

    @marks.testrail_id(5719)
    @marks.medium
    def test_special_characters_in_password_when_recover_account(self):
        sign_in = SignInView(self.driver)
        sign_in.recover_access(passphrase=basic_user['passphrase'], password=basic_user['special_chars_password'])
        sign_in.relogin(password=basic_user['special_chars_password'])

    @marks.testrail_id(5455)
    @marks.medium
    def test_recover_accounts_with_certain_seedphrase(self):
        sign_in = SignInView(self.driver)
        for phrase, account in recovery_users.items():
            home_view = sign_in.recover_access(passphrase=phrase, password=unique_password)
            wallet_view = home_view.wallet_button.click()
            wallet_view.set_up_wallet()
            address = wallet_view.get_wallet_address()
            if address != account:
                self.errors.append('Restored wallet address "%s" does not match expected "%s"' % (address, account))
            profile_view = home_view.profile_button.click()
            profile_view.logout()
        self.verify_no_errors()
