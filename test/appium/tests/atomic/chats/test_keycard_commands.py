import time

from tests import marks
from tests.users import transaction_senders, ens_user_ropsten
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from views.sign_in_view import SignInView


class TestCommandsMultipleDevices(MultipleDeviceTestCase):

    @marks.testrail_id(6294)
    @marks.medium
    @marks.transaction
    def test_keycard_request_and_receive_stt_in_1_1_chat_offline_opened_from_push(self):
        sender = transaction_senders['ETH_STT_1']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        device_1.just_fyi('Grab user data for transactions and public chat, set up wallets')
        home_1 = device_1.create_user(keycard=True)
        recipient_public_key, recipient_username = home_1.get_public_key_and_username(return_username=True)
        amount = device_1.get_unique_amount()
        asset_name = 'STT'
        wallet_1 = home_1.wallet_button.click()
        wallet_1.select_asset(asset_name)
        wallet_1.home_button.click()

        home_2 = device_2.recover_access(passphrase=sender['passphrase'], keycard=True, enable_notifications=True)
        wallet_2 = home_2.wallet_button.click()
        initial_amount_stt = wallet_2.get_asset_amount_by_name('STT')
        wallet_2.home_button.click()

        device_2.just_fyi('Add recipient to contact and send 1 message')
        chat_2 = home_2.add_contact(recipient_public_key)
        chat_2.send_message("Hey there!")

        profile_2 = wallet_2.profile_button.click()
        profile_2.airplane_mode_button.click()
        device_2.home_button.double_click()
        chat_element = home_1.get_chat(sender['username'])
        chat_element.wait_for_visibility_of_element(30)
        chat_1 = chat_element.click()

        home_1.just_fyi('Request %s STT in 1-1 chat and check it is visible for sender and receiver' % amount)
        chat_1.commands_button.click()
        request_transaction = chat_1.request_command.click()
        request_transaction.amount_edit_box.set_value(amount)
        request_transaction.confirm()
        asset_button = request_transaction.asset_by_name(asset_name)
        request_transaction.select_asset_button.click_until_presence_of_element(asset_button)
        asset_button.click()
        request_transaction.request_transaction_button.click()
        chat_1_request_message = chat_1.get_incoming_transaction()
        if not chat_1_request_message.is_element_displayed():
            self.drivers[0].fail('No incoming transaction in 1-1 chat is shown for recipient after requesting STT')

        home_2.just_fyi('Check that transaction message is fetched from offline and sign transaction')
        profile_2.airplane_mode_button.click()
        transaction_request_pn = 'Request transaction'
        device_2.open_notification_bar()
        if not device_2.element_by_text(transaction_request_pn).is_element_displayed(60):
            self.errors.append("Push notification is not received after going back from offline")
        device_2.element_by_text(transaction_request_pn).click()
        home_2.connection_offline_icon.wait_for_invisibility_of_element(120)
        home_2.get_chat(recipient_username).click()
        chat_2_sender_message = chat_2.get_outgoing_transaction()
        chat_2_sender_message.wait_for_visibility_of_element(60)
        chat_2_sender_message.transaction_status.wait_for_element_text(chat_2_sender_message.address_received)
        send_message = chat_2_sender_message.sign_and_send.click()
        send_message.next_button.click()
        send_message.sign_transaction(keycard=True)
        chat_1.toggle_airplane_mode()

        home_2.just_fyi('Check that transaction message is updated with new status after offline')
        [chat.toggle_airplane_mode() for chat in (chat_1, chat_2)]
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount, token=True)
        for home in (home_1, home_2):
            home.toggle_airplane_mode()
            home.home_button.double_click()
        home_1.get_chat(sender['username']).click()
        home_2.get_chat(recipient_username).click()
        chat_2_sender_message.transaction_status.wait_for_element_text(chat_2_sender_message.confirmed, wait_time=120)

        home_1.just_fyi('Check that can find tx in history and balance is updated after offline')
        [home.wallet_button.click() for home in (home_1, home_2)]
        wallet_2.wait_balance_is_changed('STT', initial_amount_stt)
        wallet_1.wait_balance_is_changed('STT', scan_tokens=True)
        [wallet.find_transaction_in_history(amount=amount, asset='STT') for wallet in (wallet_1, wallet_2)]
        self.errors.verify_no_errors()


class TestCommandsSingleDevices(SingleDeviceTestCase):

    @marks.testrail_id(6295)
    @marks.medium
    @marks.transaction
    def test_keycard_send_eth_to_ens(self):
        sign_in = SignInView(self.driver)
        sender = transaction_senders['ETH_4']
        home = sign_in.recover_access(sender['passphrase'], keycard=True)
        wallet = home.wallet_button.click()
        wallet.home_button.click()

        chat = home.add_contact(ens_user_ropsten['ens'])
        chat.commands_button.click()
        amount = chat.get_unique_amount()

        send_message = chat.send_command.click()
        send_message.amount_edit_box.set_value(amount)
        send_message.confirm()
        send_message.next_button.click()

        from views.send_transaction_view import SendTransactionView
        send_transaction = SendTransactionView(self.driver)
        send_transaction.sign_transaction(keycard=True)
        chat_sender_message = chat.get_outgoing_transaction()
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount)
        chat_sender_message.transaction_status.wait_for_element_text(chat_sender_message.confirmed)
