import pytest

from tests import marks, unique_password
from tests.base_test_case import SingleDeviceTestCase
from tests.users import basic_user
from views.sign_in_view import SignInView


@marks.all
@marks.account
class TestRecoverAccountSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(5301)
    @marks.critical
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
        home.connection_status.wait_for_invisibility_of_element(30)
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
