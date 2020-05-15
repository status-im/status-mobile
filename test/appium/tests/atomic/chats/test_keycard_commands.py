from _pytest.outcomes import Failed
import time

from tests import marks
from tests.users import transaction_senders, transaction_recipients, ens_user_ropsten
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.chat
@marks.transaction
@marks.skip
## TODO: uncomment and check when commands in 1-1 chat will be ready in kk framework
class TestCommandsMultipleDevices(MultipleDeviceTestCase):

    @marks.testrail_id(6293)
    @marks.critical
    def test_keycard_send_eth_in_1_1_chat(self):
        recipient = transaction_recipients['A']
        sender = transaction_senders['A']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1 = device_1.recover_access(passphrase=sender['passphrase'], keycard=True)
        home_2 = device_2.recover_access(passphrase=recipient['passphrase'], keycard=True)
        wallet_1, wallet_2 = home_1.wallet_button.click(), home_2.wallet_button.click()
        for wallet in (wallet_1, wallet_2):
            wallet.set_up_wallet()
            wallet.home_button.click()

        chat_1 = home_1.add_contact(recipient['public_key'])
        amount = chat_1.get_unique_amount()

        home_1.just_fyi('Send %s ETH in 1-1 chat and check it for sender and receiver: Address requested' % amount)
        chat_1.commands_button.click()
        send_transaction = chat_1.send_command.click()
        if not send_transaction.get_username_in_transaction_bottom_sheet_button(recipient['username']).is_element_displayed():
            self.driver.fail('%s is not shown in "Send Transaction" bottom sheet' % recipient['username'])
        send_transaction.get_username_in_transaction_bottom_sheet_button(recipient['username']).click()
        if send_transaction.scan_qr_code_button.is_element_displayed():
            self.driver.fail('Recipient is editable in bootom sheet when send ETH from 1-1 chat')
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.sign_transaction_button.click()
        chat_1_sender_message = chat_1.chat_element_by_text('↑ Outgoing transaction')
        if not chat_1_sender_message.is_element_displayed():
            self.driver.fail('No message is shown after sending ETH in 1-1 chat for sender')
        if chat_1_sender_message.transaction_status.text != 'Address requested':
            self.errors.append('Wrong state is shown for outgoing transaction: "Address requested" is expected, in fact'
                               ' %s ' % chat_1_sender_message.transaction_status.text)

        chat_2 = home_2.get_chat(sender['username']).click()
        chat_2_receiver_message = chat_2.chat_element_by_text('↓ Incoming transaction')
        timestamp_sender = chat_1_sender_message.timestamp_message.text
        if not chat_2_receiver_message.is_element_displayed():
            self.driver.fail('No message about incoming transaction in 1-1 chat is shown for receiver')
        if chat_2_receiver_message.transaction_status.text != 'Address requested':
            self.errors.append('Wrong state is shown for incoming transaction: "Address requested" is expected, in fact'
                               ' %s' % chat_2_receiver_message.transaction_status.text)

        home_2.just_fyi('Accept and share address for sender and receiver')
        for text in ('Accept and share address', 'Decline'):
            if not chat_2_receiver_message.contains_text(text):
                self.driver.fail("Transaction message doesn't contain required option %s" % text)
        select_account_bottom_sheet = chat_2_receiver_message.accept_and_share_address.click()
        if not select_account_bottom_sheet.get_account_in_select_account_bottom_sheet_button('Status').is_element_displayed():
            self.errors.append('Not expected value in "From" in "Select account": "Status" is expected')
        select_account_bottom_sheet.select_button.click()
        if chat_2_receiver_message.transaction_status.text != "Shared 'Status account'":
            self.errors.append('Wrong state is shown for incoming transaction: "Shared \'Status account\' is expected, '
                               'in fact  %s ' %  chat_2_receiver_message.transaction_status.text)
        if chat_1_sender_message.transaction_status.text != 'Address request accepted':
            self.errors.append('Wrong state is shown for outgoing transaction: "Address request accepted" is expected, '
                               'in fact %s ' % chat_1_sender_message.transaction_status.text)

        home_1.just_fyi("Sign and send transaction and check that timestamp on message is updated")
        time.sleep(40)
        send_message = chat_1_sender_message.sign_and_send.click()
        send_message.next_button.click()
        send_message.sign_transaction(keycard=True)
        if chat_1_sender_message.transaction_status.text != 'Pending':
            self.errors.append('Wrong state is shown for outgoing transaction: "Pending" is expected, in fact'
                               ' %s ' % chat_1_sender_message.transaction_status.text)
        updated_timestamp_sender = chat_1_sender_message.timestamp_message.text
        if updated_timestamp_sender == timestamp_sender:
            self.errors.append("Timestamp of message is not updated after signing transaction")

        chat_1.wallet_button.click()
        wallet_1.accounts_status_account.click()
        transactions_view = wallet_1.transaction_history_button.click()
        transactions_view.transactions_table.find_transaction(amount=amount)
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount)
        wallet_1.home_button.click()

        home_1.just_fyi("Check 'Confirmed' state for sender")
        if chat_1_sender_message.transaction_status.text != 'Confirmed':
            self.errors.append('Wrong state is shown for outgoing transaction: "Confirmed" is expected, in fact'
                               ' %s ' % chat_1_sender_message.transaction_status.text)
        self.errors.verify_no_errors()

    @marks.testrail_id(6294)
    @marks.critical
    def test_keycard_request_and_receive_stt_in_1_1_chat_offline(self):
        sender = transaction_senders['C']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        device_1.just_fyi('Grab user data for transactions and public chat, set up wallets')
        home_1 = device_1.create_user(keycard=True)
        recipient_public_key, recipient_username = home_1.get_public_key_and_username(return_username=True)
        wallet_1 = home_1.wallet_button.click()
        wallet_1.set_up_wallet()
        recipient_address = wallet_1.get_wallet_address()
        wallet_1.back_button.click()
        wallet_1.select_asset('STT')
        wallet_1.home_button.click()

        home_2 = device_2.recover_access(passphrase=sender['passphrase'], keycard=True)
        wallet_2 = home_2.wallet_button.click()
        wallet_2.set_up_wallet()
        wallet_2.home_button.click()

        device_2.just_fyi('Add recipient to contact and send 1 message')
        chat_2 = home_2.add_contact(recipient_public_key)
        chat_2.send_message("Hey there!")
        amount = chat_2.get_unique_amount()
        asset_name = 'STT'
        profile_2 = wallet_2.profile_button.click()
        profile_2.logout()
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
        chat_1_request_message = chat_1.chat_element_by_text('↓ Incoming transaction')
        if not chat_1_request_message.is_element_displayed():
            self.driver.fail('No incoming transaction in 1-1 chat is shown for recipient after requesting STT')

        home_2.just_fyi('Check that transaction message is fetched from offline and sign transaction')
        device_2.sign_in()
        home_2.connection_status.wait_for_invisibility_of_element(30)
        home_2.get_chat(recipient_username).click()
        chat_2_sender_message = chat_2.chat_element_by_text('↑ Outgoing transaction')
        if not chat_2_sender_message.is_element_displayed():
                self.driver.fail('No outgoing transaction in 1-1 chat is shown for sender after requesting STT')
        if chat_2_sender_message.transaction_status.text != 'Address received':
            self.errors.append('Wrong state is shown for outgoing transaction: "Address request accepted" is expected, '
                               'in fact %s ' % chat_2_sender_message.transaction_status.text)
        send_message = chat_2_sender_message.sign_and_send.click()
        send_message.next_button.click()
        send_message.sign_transaction(keycard=True)

        home_2.just_fyi('Check that transaction message is updated with new status after offline')
        chat_2.toggle_airplane_mode()
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount, confirmations=15, token=True)
        chat_2.toggle_airplane_mode()
        chat_2.connection_status.wait_for_invisibility_of_element(30)
        if chat_2_sender_message.transaction_status.text != 'Confirmed':
            self.errors.append('Wrong state is shown for outgoing transaction: "Confirmed" is expected, in fact'
                               ' %s ' % chat_2_sender_message.transaction_status.text)
        try:
            self.network_api.find_transaction_by_unique_amount(recipient_address[2:], amount, token=True)
        except Failed as e:
            self.errors.append(e.msg)
        self.errors.verify_no_errors()


@marks.chat
@marks.transaction
@marks.skip
## TODO: uncomment and check when commands in 1-1 chat will be ready in kk framework
class TestCommandsSingleDevices(SingleDeviceTestCase):

    @marks.testrail_id(6295)
    @marks.high
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
        chat_sender_message = chat.chat_element_by_text('↑ Outgoing transaction')
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount, confirmations=15)
        if chat_sender_message.transaction_status.text != 'Confirmed':
            self.errors.append('Wrong state is shown for outgoing transaction to ENS: "Confirmed" is expected, '
                               'in fact %s ' % chat_sender_message.transaction_status.text)

        self.errors.verify_no_errors()
