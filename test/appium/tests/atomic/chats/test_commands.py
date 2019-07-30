import pytest
from _pytest.outcomes import Failed
from decimal import Decimal as d
from selenium.common.exceptions import TimeoutException

from tests import marks, unique_password
from tests.users import transaction_senders, basic_user, transaction_recipients
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.chat
@marks.transaction
class TestCommandsMultipleDevices(MultipleDeviceTestCase):

    @marks.testrail_id(5334)
    @marks.critical
    @marks.skip
    # temporary skipped due to 8601
    def test_network_mismatch_for_send_request_commands(self):
        sender = transaction_senders['D']
        self.create_drivers(2)
        device_1_sign_in, device_2_sign_in = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_sign_in.recover_access(passphrase=sender['passphrase'])
        device_2_sign_in.create_user()
        device_1_home, device_2_home = device_1_sign_in.get_home_view(), device_2_sign_in.get_home_view()

        device_1_wallet_view = device_1_home.wallet_button.click()
        device_1_wallet_view.set_up_wallet()
        device_1_wallet_view.home_button.click()

        public_key = device_2_home.get_public_key()
        device_2_profile = device_2_home.get_profile_view()
        device_2_profile.switch_network('Mainnet with upstream RPC')

        device_1_chat = device_1_home.add_contact(public_key)
        amount_1 = device_1_chat.get_unique_amount()
        device_1_chat.send_transaction_in_1_1_chat('ETHro', amount_1, wallet_set_up=False)
        device_1_chat.chat_element_by_text(amount_1).progress_bar.wait_for_invisibility_of_element()
        status_text_1 = device_1_chat.chat_element_by_text(amount_1).status.text
        if status_text_1 != 'Sent':
            self.errors.append("Message about sent funds has status '%s' instead of 'Sent'" % status_text_1)

        device_2_chat = device_2_home.get_chat_with_user(sender['username']).click()
        chat_element_1 = device_2_chat.chat_element_by_text(amount_1)
        try:
            chat_element_1.wait_for_visibility_of_element(120)
            chat_element_1.progress_bar.wait_for_invisibility_of_element()
            if chat_element_1.status.text != 'Network mismatch':
                self.errors.append("'Network mismatch' warning is not shown for send transaction message")
            if not chat_element_1.contains_text('testnet'):
                self.errors.append("Sent transaction message doesn't contain text 'testnet'")
        except TimeoutException:
            self.errors.append('Sent transaction message was not received')
        device_2_chat.get_back_to_home_view()

        amount_2 = device_1_chat.get_unique_amount()
        device_1_chat.request_transaction_in_1_1_chat('ETHro', amount_2)
        status_text_2 = device_1_chat.chat_element_by_text(amount_2).status.text
        if status_text_2 != 'Sent':
            self.errors.append("Request funds message has status '%s' instead of 'Sent'" % status_text_2)

        device_2_home.get_chat_with_user(sender['username']).click()
        chat_element_2 = device_2_chat.chat_element_by_text(amount_2)
        try:
            chat_element_2.wait_for_visibility_of_element(120)
            chat_element_2.progress_bar.wait_for_invisibility_of_element()
            if chat_element_2.status.text != 'Network mismatch':
                self.errors.append("'Network mismatch' warning is not shown for request funds message")
            if not chat_element_2.contains_text('On testnet'):
                self.errors.append("Request funds message doesn't contain text 'testnet'")
            if not chat_element_2.contains_text('Transaction Request'):
                self.errors.append("Request funds message doesn't contain text 'Transaction Request'")
        except TimeoutException:
            self.errors.append('Request funds message was not received')
        self.verify_no_errors()

    @marks.testrail_id(5306)
    @marks.critical
    @marks.skip
    # temporary skipped due to 8601
    def test_send_eth_in_1_1_chat(self):
        recipient = transaction_recipients['A']
        sender = transaction_senders['A']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1 = device_1.recover_access(passphrase=sender['passphrase'])
        home_2 = device_2.recover_access(passphrase=recipient['passphrase'])
        wallet_1, wallet_2 = home_1.wallet_button.click(), home_2.wallet_button.click()
        wallet_1.set_up_wallet()
        wallet_1.home_button.click()
        wallet_2.set_up_wallet()
        init_balance = wallet_2.get_eth_value()
        wallet_2.home_button.click()

        chat_1 = home_1.add_contact(recipient['public_key'])
        amount = chat_1.get_unique_amount()
        chat_1.commands_button.click()
        chat_1.send_command.click()
        chat_1.asset_by_name('ETHro').click()
        chat_1.send_as_keyevent(amount)
        send_transaction_view = chat_1.get_send_transaction_view()
        chat_1.send_message_button.click_until_presence_of_element(send_transaction_view.sign_with_password)

        send_transaction_view.network_fee_button.click()
        gas_limit = '25000'
        send_transaction_view.gas_limit_input.clear()
        send_transaction_view.gas_limit_input.set_value(gas_limit)
        gas_price = str(round(float(send_transaction_view.gas_price_input.text)) + 10)
        send_transaction_view.gas_price_input.clear()
        send_transaction_view.gas_price_input.set_value(gas_price)
        if send_transaction_view.total_fee_input.text != '%s ETHro' % (d(gas_limit) * d(gas_price) / d(1000000000)):
            self.errors.append('Gas limit and/or gas price fields were not edited')
        send_transaction_view.update_fee_button.click()
        send_transaction_view.sign_transaction()

        if not chat_1.chat_element_by_text(amount).is_element_displayed():
            self.errors.append('Message with the sent amount is not shown for the sender')
        chat_2 = home_2.get_chat_with_user(sender['username']).click()
        if not chat_2.chat_element_by_text(amount).is_element_displayed():
            self.errors.append('Message with the sent amount is not shown for the recipient')

        chat_2.get_back_to_home_view()
        home_2.wallet_button.click()
        try:
            wallet_2.wait_balance_changed_on_wallet_screen(expected_balance=init_balance + float(amount))
            self.network_api.find_transaction_by_unique_amount(recipient['address'], amount)
        except Failed as e:
            self.errors.append(e.msg)
        self.verify_no_errors()

    @marks.testrail_id(5318)
    @marks.critical
    @marks.skip
    # temporary skipped due to 8601
    def test_request_and_receive_eth_in_1_1_chat(self):
        recipient = transaction_recipients['B']
        sender = transaction_senders['J']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1 = device_1.recover_access(passphrase=sender['passphrase'])
        home_2 = device_2.recover_access(passphrase=recipient['passphrase'])
        wallet_1, wallet_2 = home_1.wallet_button.click(), home_2.wallet_button.click()
        wallet_1.set_up_wallet()
        wallet_1.home_button.click()
        wallet_2.set_up_wallet()
        init_balance = wallet_2.get_eth_value()
        wallet_2.home_button.click()

        chat_2 = home_2.add_contact(sender['public_key'])
        amount = chat_2.get_unique_amount()
        chat_2.request_transaction_in_1_1_chat('ETHro', amount)

        chat_1 = home_1.get_chat_with_user(recipient['username']).click()
        chat_1.send_funds_to_request(amount=amount)

        if not chat_1.chat_element_by_text(amount).is_element_displayed():
            self.errors.append('Message with the sent amount is not shown for the sender')
        if not chat_2.chat_element_by_text(amount).is_element_displayed():
            self.errors.append('Message with the sent amount is not shown for the recipient')

        chat_2.get_back_to_home_view()
        home_2.wallet_button.click()
        try:
            wallet_2.wait_balance_changed_on_wallet_screen(expected_balance=init_balance + float(amount))
            self.network_api.find_transaction_by_unique_amount(recipient['address'], amount)
        except Failed as e:
            self.errors.append(e.msg)
        self.verify_no_errors()

    @marks.testrail_id(5324)
    @marks.critical
    def test_request_eth_in_wallet(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        sender = transaction_senders['O']

        home_1 = device_1.create_user()
        profile_1 = home_1.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        home_1 = profile_1.get_back_to_home_view()
        home_2 = device_2.recover_access(passphrase=sender['passphrase'])

        home_1.add_contact(sender['public_key'])
        home_1.get_back_to_home_view()
        wallet_1 = home_1.wallet_button.click()
        wallet_1.set_up_wallet()

        wallet_1.accounts_status_account.click()
        send_transaction_device_1 = wallet_1.receive_transaction_button.click_until_presence_of_element(
            wallet_1.send_transaction_request)
        wallet_1.send_transaction_request.click()
        send_transaction_device_1.amount_edit_box.scroll_to_element()
        amount = home_1.get_unique_amount()
        send_transaction_device_1.amount_edit_box.set_value(amount)
        send_transaction_device_1.confirm()
        send_transaction_device_1.chose_recipient_button.click()
        sender_button = send_transaction_device_1.element_by_text(sender['username'])
        send_transaction_device_1.recent_recipients_button.click_until_presence_of_element(sender_button)
        sender_button.click()
        wallet_1.send_request_button.click()

        chat_2 = home_2.get_chat_with_user(default_username_1).click()
        chat_element = chat_2.chat_element_by_text(amount)
        try:
            chat_element.wait_for_visibility_of_element(120)
            if not chat_element.contains_text('Transaction Request'):
                self.errors.append("Request funds message doesn't contain text 'Transaction Request'")
            if not chat_element.send_request_button.is_element_displayed():
                self.errors.append("Request funds message doesn't contain 'Send' button")
        except TimeoutException:
            self.errors.append('Request funds message was not received')
        self.verify_no_errors()

    @marks.testrail_id(5383)
    @marks.high
    def test_contact_profile_send_transaction(self):
        self.create_drivers(1)
        recipient = basic_user
        sign_in_view = SignInView(self.drivers[0])
        sign_in_view.create_user()
        home_view = sign_in_view.get_home_view()
        sender_public_key = home_view.get_public_key()
        sender_address = home_view.public_key_to_address(sender_public_key)
        home_view.home_button.click()
        self.network_api.get_donate(sender_address)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        home_view.home_button.click()
        home_view.add_contact(recipient['public_key'])
        chat_view = home_view.get_chat_view()
        chat_view.chat_options.click_until_presence_of_element(chat_view.view_profile_button)
        chat_view.view_profile_button.click()
        chat_view.profile_send_transaction.click()
        chat_view.chat_message_input.click()
        chat_view.asset_by_name('ETHro').click()
        amount = chat_view.get_unique_amount()
        chat_view.send_as_keyevent(amount)
        chat_view.send_message_button.click()
        send_transaction_view = chat_view.get_send_transaction_view()
        send_transaction_view.sign_transaction()
        self.network_api.find_transaction_by_unique_amount(recipient['address'], amount)

    @marks.testrail_id(5348)
    @marks.critical
    @marks.skip
    # temporary skipped due to 8601
    def test_send_tokens_in_1_1_chat(self):
        recipient = transaction_recipients['C']
        sender = transaction_senders['C']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1 = device_1.recover_access(passphrase=sender['passphrase'])
        home_2 = device_2.recover_access(passphrase=recipient['passphrase'])
        wallet_1 = home_1.wallet_button.click()
        wallet_1.set_up_wallet()
        wallet_1.home_button.click()

        chat_1 = home_1.add_contact(recipient['public_key'])
        amount = chat_1.get_unique_amount()
        chat_1.send_transaction_in_1_1_chat('STT', amount)

        message_1 = chat_1.chat_element_by_text(amount)
        if not message_1.is_element_displayed() or not message_1.contains_text('STT'):
            self.errors.append('Message with the sent amount is not shown for the sender')
        chat_2 = home_2.get_chat_with_user(sender['username']).click()
        message_2 = chat_2.chat_element_by_text(amount)
        if not message_2.is_element_displayed() or not message_2.contains_text('STT'):
            self.errors.append('Message with the sent amount is not shown for the recipient')

        try:
            self.network_api.find_transaction_by_unique_amount(recipient['address'], amount, token=True)
        except Failed as e:
            self.errors.append(e.msg)
        self.verify_no_errors()

    @marks.testrail_id(5352)
    @marks.critical
    @marks.skip
    # temporary skipped due to 8601
    def test_request_and_receive_tokens_in_1_1_chat(self):
        recipient = transaction_recipients['D']
        sender = transaction_senders['B']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1 = device_1.recover_access(passphrase=sender['passphrase'])
        home_2 = device_2.recover_access(passphrase=recipient['passphrase'])
        wallet_1, wallet_2 = home_1.wallet_button.click(), home_2.wallet_button.click()
        wallet_1.set_up_wallet()
        wallet_1.home_button.click()
        wallet_2.set_up_wallet()
        wallet_2.home_button.click()

        chat_2 = home_2.add_contact(sender['public_key'])
        amount = chat_2.get_unique_amount()
        chat_2.request_transaction_in_1_1_chat('STT', amount)

        chat_1 = home_1.get_chat_with_user(recipient['username']).click()
        chat_1.send_funds_to_request(amount=amount)

        message_1 = chat_1.chat_element_by_text(amount)
        if not message_1.is_element_displayed() or not message_1.contains_text('STT'):
            self.errors.append('Message with the sent amount is not shown for the sender')
        message_2 = chat_2.chat_element_by_text(amount)
        if not message_2.is_element_displayed() or not message_2.contains_text('STT'):
            self.errors.append('Message with the sent amount is not shown for the recipient')

        try:
            self.network_api.find_transaction_by_unique_amount(recipient['address'], amount, token=True)
        except Failed as e:
            self.errors.append(e.msg)
        self.verify_no_errors()

    @marks.testrail_id(5376)
    @marks.high
    def test_transaction_confirmed_on_recipient_side(self):
        recipient = transaction_recipients['E']
        sender = transaction_senders['E']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1 = device_1.recover_access(passphrase=sender['passphrase'])
        home_2 = device_2.recover_access(passphrase=recipient['passphrase'])
        wallet_1 = home_1.wallet_button.click()
        wallet_1.set_up_wallet()
        wallet_1.home_button.click()

        chat_1 = home_1.add_contact(recipient['public_key'])
        amount = chat_1.get_unique_amount()
        chat_1.send_transaction_in_1_1_chat('ETHro', amount)

        chat_2 = home_2.get_chat_with_user(sender['username']).click()
        self.network_api.wait_for_confirmation_of_transaction(recipient['address'], amount)
        if not chat_2.chat_element_by_text(amount).contains_text('Confirmed', 60):
            chat_2.driver.fail('Status "Confirmed" is not shown under transaction for the recipient')


@marks.chat
@marks.transaction
class TestCommandsSingleDevices(SingleDeviceTestCase):

    @marks.testrail_id(5349)
    @marks.high
    def test_send_request_not_enabled_tokens(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat = home.add_contact(basic_user['public_key'])
        chat.commands_button.click()
        chat.send_command.click()
        if chat.asset_by_name('MDS').is_element_displayed():
            self.errors.append('Token which is not enabled in wallet can be sent in 1-1 chat')
        chat.chat_message_input.clear()
        chat.commands_button.click()
        chat.request_command.click()
        if chat.asset_by_name('MDS').is_element_displayed():
            self.errors.append('Token which is not enabled in wallet can be requested in 1-1 chat')
        self.verify_no_errors()

    @marks.logcat
    @marks.testrail_id(5417)
    @marks.critical
    @marks.skip
    # temporary skipped due to 8601
    def test_logcat_send_transaction_in_1_1_chat(self):
        sender = transaction_senders['F']
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(passphrase=sender['passphrase'], password=unique_password)
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        wallet.home_button.click()
        chat = home.add_contact(basic_user['public_key'])
        amount = chat.get_unique_amount()
        chat.send_transaction_in_1_1_chat('ETHro', amount, unique_password)
        chat.check_no_values_in_logcat(password=unique_password)

    @marks.testrail_id(5347)
    @marks.high
    def test_send_transaction_details_in_1_1_chat(self):
        recipient = basic_user
        sender = transaction_senders['G']
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(passphrase=sender['passphrase'])
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        wallet.home_button.click()

        chat = home.add_contact(recipient['public_key'])
        amount = chat.get_unique_amount()
        chat.commands_button.click()
        chat.send_command.click()
        chat.asset_by_name('ETHro').click()
        chat.send_as_keyevent(amount)
        send_transaction_view = chat.get_send_transaction_view()
        chat.send_message_button.click_until_presence_of_element(send_transaction_view.sign_with_password)

        # if not send_transaction_view.element_by_text(recipient['username']).is_element_displayed():
        #     self.errors.append('Recipient name is not shown')
        if not send_transaction_view.element_by_text_part('ETHro').is_element_displayed():
            self.errors.append("Asset field doesn't contain 'ETHro' text")
        if not send_transaction_view.element_by_text_part(amount).is_element_displayed():
            self.errors.append('Amount is not visible')
        self.verify_no_errors()

    @marks.testrail_id(5377)
    @marks.high
    def test_transaction_confirmed_on_sender_side(self):
        sender = transaction_senders['H']
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(passphrase=sender['passphrase'])
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        wallet.home_button.click()
        chat = home.add_contact(basic_user['public_key'])
        amount = chat.get_unique_amount()
        chat.send_transaction_in_1_1_chat('ETHro', amount)
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount)
        if not chat.chat_element_by_text(amount).contains_text('Confirmed', wait_time=90):
            pytest.fail('Status "Confirmed" is not shown under transaction for the sender')

    @marks.testrail_id(5410)
    @marks.high
    def test_insufficient_funds_1_1_chat_0_balance(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        home_view = wallet_view.home_button.click()
        chat_view = home_view.add_contact(basic_user['public_key'])
        chat_view.commands_button.click()
        chat_view.send_command.click()
        chat_view.asset_by_name('ETHro').click()
        chat_view.send_as_keyevent('1')
        chat_view.send_message_button.click()
        send_transaction = chat_view.get_send_transaction_view()
        error_text = send_transaction.element_by_text('Insufficient funds')
        if not error_text.is_element_displayed():
            self.errors.append("'Insufficient funds' error is now shown when sending 1 ETH from chat with balance 0")
        send_transaction.cancel_button.click()
        chat_view.commands_button.click()

        # enable STT in wallet
        chat_view.wallet_button.click()
        wallet_view.select_asset("STT")
        wallet_view.home_button.click()

        chat_view.send_command.click()
        chat_view.asset_by_name('STT').click()
        chat_view.send_as_keyevent('1')
        chat_view.send_message_button.click()
        if not error_text.is_element_displayed():
            self.errors.append("'Insufficient funds' error is now shown when sending 1 STT from chat with balance 0")
        self.verify_no_errors()

    @marks.testrail_id(5473)
    @marks.medium
    def test_insufficient_funds_1_1_chat_positive_balance(self):
        sender = transaction_senders['I']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'])
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
        eth_value = wallet_view.get_eth_value()
        stt_value = wallet_view.get_stt_value()
        if eth_value == 0 or stt_value == 0:
            pytest.fail('No funds!')
        home_view = wallet_view.home_button.click()
        chat_view = home_view.add_contact(basic_user['public_key'])
        chat_view.commands_button.click()
        chat_view.send_command.click()
        chat_view.asset_by_name('ETHro').click()
        chat_view.send_as_keyevent(str(round(eth_value + 1)))
        chat_view.send_message_button.click()
        send_transaction = chat_view.get_send_transaction_view()
        error_text = send_transaction.element_by_text('Insufficient funds')
        if not error_text.is_element_displayed():
            self.errors.append(
                "'Insufficient funds' error is now shown when sending %s ETHro from chat with balance %s" % (
                    round(eth_value + 1), eth_value))
        send_transaction.cancel_button.click_until_presence_of_element(chat_view.commands_button)
        chat_view.commands_button.click()
        chat_view.send_command.click()
        chat_view.asset_by_name('STT').scroll_to_element()
        chat_view.asset_by_name('STT').click()
        chat_view.send_as_keyevent(str(round(stt_value + 1)))
        chat_view.send_message_button.click()
        if not error_text.is_element_displayed():
            self.errors.append(
                "'Insufficient funds' error is now shown when sending %s STT from chat with balance %s" % (
                    round(stt_value + 1), stt_value))
        self.verify_no_errors()
