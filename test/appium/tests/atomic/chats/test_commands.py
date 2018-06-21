from selenium.common.exceptions import TimeoutException

from tests import marks, transaction_users, common_password
from tests.base_test_case import MultipleDeviceTestCase
from views.sign_in_view import SignInView


@marks.chat
@marks.transaction
class TestCommands(MultipleDeviceTestCase):

    @marks.testrail_case_id(3742)
    def test_network_mismatch_for_send_request_commands(self):
        sender = self.senders['d_user'] = transaction_users['D_USER']
        self.create_drivers(2)
        device_1_sign_in, device_2_sign_in = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_sign_in.recover_access(passphrase=sender['passphrase'], password=sender['password'])
        device_2_sign_in.create_user()
        device_1_home, device_2_home = device_1_sign_in.get_home_view(), device_2_sign_in.get_home_view()

        public_key = device_2_home.get_public_key()
        device_2_profile = device_2_home.get_profile_view()
        device_2_profile.switch_network('Mainnet with upstream RPC')
        device_2_sign_in.click_account_by_position(0)
        device_2_sign_in.sign_in()

        device_1_chat = device_1_home.add_contact(public_key)
        amount_1 = device_1_chat.get_unique_amount()
        device_1_chat.send_transaction_in_1_1_chat(amount_1, common_password, wallet_set_up=True)
        status_text_1 = device_1_chat.chat_element_by_text(amount_1).status.text
        if status_text_1 != 'Sent':
            self.errors.append("Message about sent funds has status '%s' instead of 'Sent'" % status_text_1)

        device_2_chat = device_2_home.get_chat_with_user(sender['username']).click()
        chat_element_1 = device_2_chat.chat_element_by_text(amount_1)
        try:
            chat_element_1.wait_for_visibility_of_element(120)
            if chat_element_1.status.text != 'Network mismatch':
                self.errors.append("'Network mismatch' warning is not shown for send transaction message")
            if not chat_element_1.contains_text('testnet'):
                self.errors.append("Sent transaction message doesn't contain text 'testnet'")
        except TimeoutException:
            self.errors.append('Sent transaction message was not received')

        amount_2 = device_1_chat.get_unique_amount()
        device_1_chat.request_transaction_in_1_1_chat(amount_2)
        status_text_2 = device_1_chat.chat_element_by_text(amount_2).status.text
        if status_text_2 != 'Sent':
            self.errors.append("Request funds message has status '%s' instead of 'Sent'" % status_text_2)

        chat_element_2 = device_2_chat.chat_element_by_text(amount_2)
        try:
            chat_element_2.wait_for_visibility_of_element(120)
            if chat_element_2.status.text != 'Network mismatch':
                self.errors.append("'Network mismatch' warning is not shown for request funds message")
            if not chat_element_2.contains_text('On testnet'):
                self.errors.append("Request funds message doesn't contain text 'testnet'")
            if not chat_element_2.contains_text('Transaction Request'):
                self.errors.append("Request funds message doesn't contain text 'Transaction Request'")
        except TimeoutException:
            self.errors.append('Request funds message was not received')

        self.verify_no_errors()
