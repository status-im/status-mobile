from tests import marks, transaction_users, common_password
from tests.base_test_case import MultipleDeviceTestCase
from views.sign_in_view import SignInView


@marks.chat
@marks.transaction
class TestCommands(MultipleDeviceTestCase):

    @marks.skip
    @marks.testrail_case_id(3697)
    def test_network_mismatch_for_send_request_commands(self):
        recipient = transaction_users['C_USER']
        sender = self.senders['d_user'] = transaction_users['D_USER']
        self.create_drivers(2)
        device_1_sign_in, device_2_sign_in = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        for user_details in (sender, device_1_sign_in), (recipient, device_2_sign_in):
            user_details[1].recover_access(passphrase=user_details[0]['passphrase'],
                                           password=user_details[0]['password'])
        device_1_home, device_2_home = device_1_sign_in.get_home_view(), device_2_sign_in.get_home_view()

        device_2_profile = device_2_home.profile_button.click()
        device_2_profile.switch_network('Mainnet with upstream RPC')
        device_2_sign_in.click_account_by_position(0)
        device_2_sign_in.sign_in()

        device_1_chat = device_1_home.add_contact(recipient['public_key'])
        amount_1 = device_1_chat.get_unique_amount()
        device_1_chat.send_transaction_in_1_1_chat(amount_1, common_password, wallet_set_up=True)
        assert device_1_chat.chat_element_by_text(amount_1).status.text == 'Sent'

        device_2_chat = device_2_home.get_chat_with_user(sender['username']).click()
        chat_element_1 = device_2_chat.chat_element_by_text(amount_1)
        chat_element_1.wait_for_visibility_of_element(120)
        assert chat_element_1.status.text == 'Network mismatch'
        assert chat_element_1.contains_text('On testnet')

        amount_2 = device_1_chat.get_unique_amount()
        device_1_chat.request_transaction_in_1_1_chat(amount_2)
        assert device_1_chat.chat_element_by_text(amount_2).status.text == 'Sent'

        chat_element_2 = device_2_chat.chat_element_by_text(amount_2)
        chat_element_2.wait_for_visibility_of_element(120)
        assert chat_element_2.status.text == 'Network mismatch'
        assert chat_element_2.contains_text('On testnet')
        assert chat_element_2.contains_text('Transaction Request')
