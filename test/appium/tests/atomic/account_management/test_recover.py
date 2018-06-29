import pytest

from tests import marks, common_password, group_chat_users
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from views.sign_in_view import SignInView


@marks.all
@marks.account
class TestRecoverAccountMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(759)
    def test_recover_account(self):
        self.create_drivers(2)
        device, device_2 = self.drivers[0], self.drivers[1]
        sign_in, sign_in_2 = SignInView(device), SignInView(device_2)
        username_1 = 'user_1'
        username_2 = group_chat_users['A_USER']['username']
        home = sign_in.create_user(username_1)
        home_2 = sign_in_2.recover_access(passphrase=group_chat_users['A_USER']['passphrase'],
                                          password=group_chat_users['A_USER']['password'])
        public_key = home.get_public_key()
        chat_2 = home_2.add_contact(public_key)
        message = 'test message'
        chat_2.chat_message_input.send_keys(message)
        chat_2.send_message_button.click()
        device_2.quit()

        profile = home.get_profile_view()
        profile.backup_seed_phrase_button.click()
        profile.ok_continue_button.click()
        seed_phrase = profile.get_seed_phrase()
        profile.back_button.click()
        profile.advanced_button.click()
        profile.debug_mode_toggle.click()
        profile.home_button.click()
        console_chat = home.get_chat_with_user('Status Console').click()
        console_chat.send_faucet_request()
        console_chat.get_back_to_home_view()
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        address = wallet.get_wallet_address()
        wallet.wait_balance_changed_on_wallet_screen()

        device.reset()
        sign_in.accept_agreements()
        sign_in.recover_access(passphrase=' '.join(seed_phrase.values()), password=common_password)
        home.connection_status.wait_for_invisibility_of_element(30)
        chat_element = home.get_chat_with_user(username_2)
        if chat_element.is_element_displayed():
            chat = chat_element.click()
            if not chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append("Message with text '%s' was not received" % message)
            chat.get_back_to_home_view()
        else:
            self.errors.append('Chat with user %s is not recovered' % username_2)
        home.wallet_button.click()
        wallet.set_up_wallet()
        if wallet.get_wallet_address() != address:
            self.errors.append('Wallet address is changed after recover')
        if wallet.get_eth_value() != 0.1:
            self.errors.append('Wallet balance is changed after recover')
        if wallet.get_public_key() != public_key:
            self.errors.append('Public key is changed after recover')
        self.verify_no_errors()


@marks.all
@marks.account
class TestRecoverAccountSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(845)
    def test_recover_account_with_incorrect_passphrase(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        public_key = sign_in.get_public_key()
        profile = sign_in.get_profile_view()
        profile.backup_seed_phrase_button.click()
        profile.ok_continue_button.click()
        seed_phrase = profile.get_seed_phrase()

        self.driver.reset()
        sign_in.accept_agreements()
        sign_in.recover_access(passphrase=' '.join(list(seed_phrase.values())[::-1]), password=common_password)
        if sign_in.get_public_key() == public_key:
            pytest.fail('The same account is recovered with reversed passphrase')
