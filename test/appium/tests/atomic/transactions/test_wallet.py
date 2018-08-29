import random

import pytest

from tests import transaction_users, transaction_users_wallet, marks, common_password, unique_password
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from views.sign_in_view import SignInView


@marks.transaction
class TestTransactionWalletSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(766)
    @marks.smoke_1
    def test_send_eth_from_wallet_to_contact(self):
        recipient = transaction_users['F_USER']
        sender = transaction_users['E_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        transaction_amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.recent_recipients_button.click()
        recent_recipient = send_transaction.element_by_text(recipient['username'])
        send_transaction.recent_recipients_button.click_until_presence_of_element(recent_recipient)
        recent_recipient.click()
        send_transaction.sign_transaction(sender['password'])
        self.network_api.find_transaction_by_unique_amount(sender['address'], transaction_amount)

    @marks.testrail_id(767)
    @marks.smoke_1
    def test_send_eth_from_wallet_to_address(self):
        recipient = transaction_users['E_USER']
        sender = transaction_users['F_USER']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'], sender['password'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        transaction_amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction(sender['password'])
        self.network_api.find_transaction_by_unique_amount(sender['address'], transaction_amount)

    @marks.testrail_id(1430)
    @marks.smoke_1
    def test_send_stt_from_wallet(self):
        sender = transaction_users_wallet['A_USER']
        recipient = transaction_users_wallet['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        stt_button = send_transaction.asset_by_name('STT')
        send_transaction.select_asset_button.click_until_presence_of_element(stt_button)
        stt_button.click()
        send_transaction.amount_edit_box.click()
        amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction(sender['password'])
        self.network_api.find_transaction_by_unique_amount(recipient['address'], amount, token=True)

    @marks.testrail_id(2164)
    def test_transaction_wrong_password_wallet(self):
        recipient = transaction_users['E_USER']
        sender = transaction_users['F_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        transaction_amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.click()
        send_transaction.enter_password_input.send_keys('wrong_password')
        send_transaction.sign_transaction_button.click()
        send_transaction.find_full_text('Wrong password', 20)

    @marks.testrail_id(1452)
    def test_transaction_appears_in_history(self):
        recipient = transaction_users['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        home_view = sign_in_view.get_home_view()
        transaction_amount = home_view.get_unique_amount()
        sender_public_key = home_view.get_public_key()
        sender_address = home_view.public_key_to_address(sender_public_key)
        home_view.home_button.click()
        self.network_api.get_donate(sender_address)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.wait_balance_changed_on_wallet_screen()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction(common_password)
        self.network_api.find_transaction_by_unique_amount(recipient['address'], transaction_amount)
        transactions_view = wallet_view.transaction_history_button.click()
        transactions_view.transactions_table.find_transaction(amount=transaction_amount)

    @marks.testrail_id(2163)
    def test_send_eth_from_wallet_incorrect_address(self):
        recipient = transaction_users['E_USER']
        sender = transaction_users['F_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        transaction_amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['public_key'])
        send_transaction.done_button.click()
        send_transaction.find_text_part('Invalid address:', 20)

    @marks.logcat
    @marks.testrail_id(3770)
    def test_logcat_send_transaction_from_wallet(self):
        sender = transaction_users['E_USER']
        recipient = transaction_users['F_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], unique_password)
        home_view = sign_in_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        transaction_amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction(unique_password)
        send_transaction.check_no_values_in_logcat(password=unique_password)

    @marks.testrail_id(3746)
    @marks.smoke_1
    def test_send_token_with_7_decimals(self):
        sender = transaction_users_wallet['A_USER']
        recipient = transaction_users_wallet['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        adi_button = send_transaction.asset_by_name('ADI')
        send_transaction.select_asset_button.click_until_presence_of_element(adi_button)
        adi_button.click()
        send_transaction.amount_edit_box.click()
        amount = '0.0%s' % str(random.randint(100000, 999999)).strip('0')
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction(sender['password'])
        self.network_api.find_transaction_by_unique_amount(sender['address'], amount, token=True, decimals=7)

    @marks.testrail_id(3747)
    @marks.smoke_1
    def test_token_with_more_than_allowed_decimals(self):
        sender = transaction_users_wallet['A_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        adi_button = send_transaction.asset_by_name('ADI')
        send_transaction.select_asset_button.click_until_presence_of_element(adi_button)
        adi_button.click()
        send_transaction.amount_edit_box.click()
        amount = '0.0%s' % str(random.randint(1000000, 9999999)).strip('0')
        send_transaction.amount_edit_box.set_value(amount)
        error_text = 'Amount is too precise. Max number of decimals is 7.'
        if not send_transaction.element_by_text(error_text).is_element_displayed():
            self.errors.append('Warning about too precise amount is not shown when sending a transaction')
        send_transaction.back_button.click()
        wallet_view.receive_transaction_button.click()
        wallet_view.send_transaction_request.click()
        send_transaction.select_asset_button.click_until_presence_of_element(adi_button)
        adi_button.click()
        send_transaction.amount_edit_box.set_value(amount)
        error_text = 'Amount is too precise. Max number of decimals is 7.'
        if not send_transaction.element_by_text(error_text).is_element_displayed():
            self.errors.append('Warning about too precise amount is not shown when requesting a transaction')
        self.verify_no_errors()

    @marks.testrail_id(1405)
    def test_send_valid_amount_after_insufficient_funds_error(self):
        sender = transaction_users['G_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        bigger_amount = wallet_view.get_eth_value() + 1
        send_transaction = wallet_view.send_transaction_button.click()
        amount_edit_box = send_transaction.amount_edit_box
        amount_edit_box.click()
        amount_edit_box.set_value(bigger_amount)
        send_transaction.element_by_text('Insufficient funds').wait_for_visibility_of_element(5)

        valid_amount = send_transaction.get_unique_amount()
        amount_edit_box.clear()
        amount_edit_box.set_value(valid_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(transaction_users['H_USER']['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction(sender['password'])
        self.network_api.find_transaction_by_unique_amount(sender['address'], valid_amount)

    @marks.testrail_id(3764)
    def test_insufficient_funds_wallet_0_balance(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.set_value(1)
        error_text = send_transaction.element_by_text('Insufficient funds')
        if not error_text.is_element_displayed():
            self.errors.append("'Insufficient funds' error is now shown when sending 1 ETH from wallet with balance 0")
        send_transaction.select_asset_button.click()
        send_transaction.asset_by_name('STT').click()
        send_transaction.amount_edit_box.set_value(1)
        if not error_text.is_element_displayed():
            self.errors.append("'Insufficient funds' error is now shown when sending 1 STT from wallet with balance 0")
        self.verify_no_errors()

    @marks.testrail_id(3792)
    def test_insufficient_funds_wallet_positive_balance(self):
        sender = transaction_users_wallet['A_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        eth_value = wallet_view.get_eth_value()
        stt_value = wallet_view.get_stt_value()
        if eth_value == 0 or stt_value == 0:
            pytest.fail('No funds!')
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.set_value(round(eth_value + 1))
        error_text = send_transaction.element_by_text('Insufficient funds')
        if not error_text.is_element_displayed():
            self.errors.append(
                "'Insufficient funds' error is now shown when sending %s ETH from wallet with balance %s" % (
                    round(eth_value + 1), eth_value))
        send_transaction.select_asset_button.click()
        send_transaction.asset_by_name('STT').scroll_to_element()
        send_transaction.asset_by_name('STT').click()
        send_transaction.amount_edit_box.set_value(round(stt_value + 1))
        if not error_text.is_element_displayed():
            self.errors.append(
                "'Insufficient funds' error is now shown when sending %s STT from wallet with balance %s" % (
                    round(stt_value + 1), stt_value))
        self.verify_no_errors()

    @marks.testrail_id(3728)
    def test_modify_transaction_fee_values(self):
        sender = transaction_users['H_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()

        amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        recipient_address = transaction_users['G_USER']['address']
        send_transaction.enter_recipient_address_input.set_value(recipient_address)
        send_transaction.done_button.click()
        send_transaction.advanced_button.click()
        send_transaction.transaction_fee_button.click()
        send_transaction.gas_limit_input.clear()
        send_transaction.gas_limit_input.set_value('1')
        send_transaction.gas_price_input.clear()
        send_transaction.gas_price_input.set_value('1')
        send_transaction.total_fee_input.click()
        send_transaction.done_button.click()
        send_transaction.sign_transaction_button.click_until_presence_of_element(send_transaction.enter_password_input)
        send_transaction.enter_password_input.send_keys(sender['password'])
        send_transaction.sign_transaction_button.click()
        send_transaction.element_by_text('intrinsic gas too low').wait_for_visibility_of_element()
        send_transaction.ok_button.click()

        wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient_address)
        send_transaction.done_button.click()

        send_transaction.advanced_button.click()
        send_transaction.transaction_fee_button.click()
        send_transaction.gas_limit_input.clear()
        gas_limit = '1005000'
        send_transaction.gas_limit_input.set_value(gas_limit)
        send_transaction.gas_price_input.clear()
        gas_price = '24'
        send_transaction.gas_price_input.set_value(gas_price)
        send_transaction.total_fee_input.click()
        send_transaction.done_button.click()
        send_transaction.sign_transaction(sender['password'])
        self.network_api.find_transaction_by_unique_amount(sender['address'], amount)


@marks.transaction
class TestTransactionWalletMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(3761)
    @marks.smoke_1
    def test_transaction_message_sending_from_wallet(self):
        recipient = transaction_users['D_USER']
        sender = transaction_users['C_USER']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1 = device_1.recover_access(passphrase=sender['passphrase'], password=sender['password'])
        home_2 = device_2.recover_access(passphrase=recipient['passphrase'], password=recipient['password'])

        chat_1 = home_1.add_contact(recipient['public_key'])
        chat_1.get_back_to_home_view()

        wallet_1 = home_1.wallet_button.click()
        wallet_1.set_up_wallet()
        send_transaction = wallet_1.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.recent_recipients_button.click()
        send_transaction.element_by_text_part(recipient['username']).click()
        send_transaction.sign_transaction(sender['password'])

        wallet_1.home_button.click()
        home_1.get_chat_with_user(recipient['username']).click()
        if not chat_1.chat_element_by_text(amount).is_element_displayed():
            self.errors.append('Transaction message is not shown in 1-1 chat for the sender')
        chat_2 = home_2.get_chat_with_user(sender['username']).click()
        if not chat_2.chat_element_by_text(amount).is_element_displayed():
            self.errors.append('Transaction message is not shown in 1-1 chat for the recipient')
        self.verify_no_errors()
