import pytest

from support.utilities import fill_string_with_char
from tests import marks, unique_password
from tests.base_test_case import SingleDeviceTestCase
from tests.users import basic_user, transaction_senders
from views.sign_in_view import SignInView


@marks.all
@marks.account
class TestRecoverAccountSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(5301)
    @marks.critical
    @marks.battery_consumption
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
        sign_in.check_no_values_in_logcat(passphrase=basic_user['passphrase'], password=unique_password)


class TestRecoverAccessFromSignInScreen(SingleDeviceTestCase):
    @marks.testrail_id(5363)
    @marks.critical
    def test_pass_phrase_validation(self):
        signin_view = SignInView(self.driver)
        recover_access_view = signin_view.i_have_account_button.click()
        phrase_outside_the_mnemonic = 'one two three four five six seven eight nine ten eleven twelve'
        validations = [
            {
                'phrase': '    ',
                'element to check': recover_access_view.warnings.required_field,
                'validation message': 'Required field',
            },
            {
                'phrase': 'a',
                'element to check': recover_access_view.warnings.invalid_recovery_phrase,
                'validation message': 'Recovery phrase is invalid'
            },
            {
                'phrase': 'one two three four five six seven eight nine ten eleven twelve thirteen',
                'element to check': recover_access_view.warnings.invalid_recovery_phrase,
                'validation message': 'Recovery phrase is invalid'
            },
            {
                'phrase': '; two three four five six seven eight nine ten eleven twelve',
                'element to check': recover_access_view.warnings.invalid_recovery_phrase,
                'validation message': 'Recovery phrase is invalid'
            },
            {
                'phrase': phrase_outside_the_mnemonic,
                'element to check': recover_access_view.warnings.misspelled_words,
                'validation message': 'Some words might be misspelled'
            },
        ]

        # we're performing the same steps changing only phrase per attempt
        for validation in validations:
            phrase, elm, msg = validation.get('phrase'), validation.get('element to check'), validation.get(
                'validation message')
            if signin_view.i_have_account_button.is_element_displayed():
                signin_view.i_have_account_button.click()
            recover_access_view.send_as_keyevent(phrase)
            recover_access_view.password_input.click()

            if not elm.is_element_displayed():
                self.errors.append('"{}" message is not shown'.format(msg))
            recover_access_view.click_system_back_button()

        signin_view.i_have_account_button.click()
        recover_access_view.send_as_keyevent(phrase_outside_the_mnemonic)
        recover_access_view.password_input.click()
        recover_access_view.send_as_keyevent('123456')
        recover_access_view.sign_in_button.click()
        recover_access_view.cancel_button.click()

        if recover_access_view.cancel_button.is_element_displayed():
            self.errors.append('Something went wrong. Probably, the confirmation pop up did not disappear')

        recover_access_view.click_system_back_button()
        signin_view.i_have_account_button.click()
        recover_access_view.send_as_keyevent(phrase_outside_the_mnemonic)
        recover_access_view.password_input.click()
        recover_access_view.send_as_keyevent('123456')
        recover_access_view.sign_in_button.click()
        home_view = recover_access_view.confirm_phrase_button.click()

        if not home_view.profile_button.is_element_displayed():
            self.errors.append('Something went wrong. Probably, could not reach the home screen out.')

        self.verify_no_errors()

    @marks.testrail_id(5499)
    @marks.critical
    def test_passphrase_whitespaces_ignored_while_recovering_access(self):
        signin_view = SignInView(self.driver)
        sender = transaction_senders['U']
        passphrase = fill_string_with_char(sender['passphrase'], ' ', 3, True, True)
        home_view = signin_view.recover_access(passphrase=passphrase)

        if not home_view.profile_button.is_element_displayed():
            self.driver.fail('Something went wrong. Probably, could not reach the home screen out.')

    @marks.testrail_id(5394)
    @marks.high
    def test_uppercase_is_replaced_by_lowercase_automatically(self):
        passphrase = transaction_senders['A']['passphrase']
        capitalized_passphrase = passphrase.upper()
        signin_view = SignInView(self.driver)
        recover_access_view = signin_view.i_have_account_button.click()
        recover_access_view.passphrase_input.click()
        recover_access_view.send_as_keyevent(capitalized_passphrase)
        if recover_access_view.passphrase_input.text != passphrase:
            self.driver.fail('Upper case was not replaced by lower case!')
