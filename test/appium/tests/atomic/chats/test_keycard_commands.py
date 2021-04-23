import time

from tests import marks
from tests.users import transaction_senders, transaction_recipients, ens_user_ropsten
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from views.sign_in_view import SignInView

@marks.transaction
class TestCommandsMultipleDevices(MultipleDeviceTestCase):

    @marks.testrail_id(6293)
    @marks.critical
    def test_keycard_send_eth_in_1_1_chat(self):
        sender = transaction_senders['E']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1 = device_1.recover_access(passphrase=sender['passphrase'], keycard=True)
        home_2 = device_2.create_user(keycard=True)
        recipient_public_key, recipient_username = home_2.get_public_key_and_username(return_username=True)
        wallet_1, wallet_2 = home_1.wallet_button.click(), home_2.wallet_button.click()
        for wallet in (wallet_1, wallet_2):
            wallet.set_up_wallet()
            wallet.home_button.click()

        chat_1 = home_1.add_contact(recipient_public_key)
        amount = chat_1.get_unique_amount()
        account_name = wallet_1.status_account_name

        home_1.just_fyi('Send %s ETH in 1-1 chat and check it for sender and receiver: Address requested' % amount)
        chat_1.commands_button.click()
        send_transaction = chat_1.send_command.click()
        send_transaction.get_username_in_transaction_bottom_sheet_button(recipient_username).click()
        if send_transaction.scan_qr_code_button.is_element_displayed():
            self.drivers[0].fail('Recipient is editable in bottom sheet when send ETH from 1-1 chat')
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.sign_transaction_button.click()
        sender_message = chat_1.get_outgoing_transaction()
        if not sender_message.is_element_displayed():
            self.drivers[0].fail('No message is shown after sending ETH in 1-1 chat for sender')
        sender_message.transaction_status.wait_for_element_text(sender_message.address_requested)

        chat_2 = home_2.get_chat(sender['username']).click()
        receiver_message = chat_2.get_incoming_transaction()
        timestamp_sender = sender_message.timestamp_message.text
        if not receiver_message.is_element_displayed():
            self.drivers[0].fail('No message about incoming transaction in 1-1 chat is shown for receiver')
        receiver_message.transaction_status.wait_for_element_text(receiver_message.address_requested)

        home_2.just_fyi('Accept and share address for sender and receiver')
        for option in (receiver_message.decline_transaction, receiver_message.accept_and_share_address):
            if not option.is_element_displayed():
                self.drivers[0].fail("Required options accept or share are not shown")

        select_account_bottom_sheet = receiver_message.accept_and_share_address.click()
        if not select_account_bottom_sheet.get_account_in_select_account_bottom_sheet_button(account_name).is_element_displayed():
            self.errors.append('Not expected value in "From" in "Select account": "Status" is expected')
        select_account_bottom_sheet.select_button.click()
        receiver_message.transaction_status.wait_for_element_text(receiver_message.shared_account)
        sender_message.transaction_status.wait_for_element_text(sender_message.address_request_accepted)

        home_1.just_fyi("Sign and send transaction and check that timestamp on message is updated")
        time.sleep(20)
        send_message = sender_message.sign_and_send.click()
        send_message.next_button.click()
        send_message.sign_transaction(keycard=True, default_gas_price=False)
        updated_timestamp_sender = sender_message.timestamp_message.text
        if updated_timestamp_sender == timestamp_sender:
            self.errors.append("Timestamp of message is not updated after signing transaction")

        chat_1.wallet_button.click()
        wallet_1.find_transaction_in_history(amount=amount)
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount)
        wallet_1.home_button.click(desired_view='chat')

        home_1.just_fyi("Check 'Confirmed' state for sender and receiver(use pull-to-refresh to update history)")
        chat_2.wallet_button.click()
        wallet_2.find_transaction_in_history(amount=amount)
        wallet_2.home_button.click(desired_view="chat")
        [message.transaction_status.wait_for_element_text(message.confirmed, 60) for message in
         (sender_message, receiver_message)]

        self.errors.verify_no_errors()

    @marks.testrail_id(6294)
    @marks.medium
    def test_keycard_request_and_receive_stt_in_1_1_chat_offline_opened_from_push(self):
        sender = transaction_senders['D']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        device_1.just_fyi('Grab user data for transactions and public chat, set up wallets')
        home_1 = device_1.create_user(keycard=True)
        recipient_public_key, recipient_username = home_1.get_public_key_and_username(return_username=True)
        amount = device_1.get_unique_amount()
        asset_name = 'STT'
        wallet_1 = home_1.wallet_button.click()
        wallet_1.set_up_wallet()
        wallet_1.select_asset(asset_name)
        wallet_1.home_button.click()

        home_2 = device_2.recover_access(passphrase=sender['passphrase'], keycard=True, enable_notifications=True)
        wallet_2 = home_2.wallet_button.click()
        wallet_2.set_up_wallet()
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
        send_message.sign_transaction(keycard=True, default_gas_price=False)

        home_2.just_fyi('Check that transaction message is updated with new status after offline')
        chat_2.toggle_airplane_mode()
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount, token=True)
        chat_2.toggle_airplane_mode()
        [message.transaction_status.wait_for_element_text(message.confirmed, wait_time=60) for message in
         (chat_2_sender_message, chat_1_request_message)]
        self.errors.verify_no_errors()


@marks.transaction
class TestCommandsSingleDevices(SingleDeviceTestCase):

    @marks.testrail_id(6295)
    @marks.medium
    def test_keycard_send_eth_to_ens(self):
        sign_in = SignInView(self.driver)
        sender = transaction_senders['E']
        home = sign_in.recover_access(sender['passphrase'], keycard=True)
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
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount, confirmations=15)
        chat_sender_message.transaction_status.wait_for_element_text(chat_sender_message.confirmed)
