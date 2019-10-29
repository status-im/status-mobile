import random
import string

from support.utilities import get_merged_txs_list
from tests import marks, unique_password, common_password
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from tests.users import transaction_senders, basic_user, wallet_users, transaction_recipients
from views.sign_in_view import SignInView


@marks.transaction
class TestTransactionWalletSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(5307)
    @marks.critical
    @marks.skip
    # TODO: temporary skipped due to 8601
    def test_send_eth_from_wallet_to_contact(self):
        recipient = basic_user
        sender = transaction_senders['N']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'])
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
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
        send_transaction.sign_transaction_button.click()
        send_transaction.sign_transaction()
        self.network_api.find_transaction_by_unique_amount(sender['address'], transaction_amount)

    @marks.testrail_id(5308)
    @marks.critical
    def test_send_eth_from_wallet_to_address(self):
        recipient = basic_user
        sender = transaction_senders['P']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
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
        send_transaction.sign_transaction()
        self.network_api.find_transaction_by_unique_amount(sender['address'], transaction_amount)

    @marks.testrail_id(5325)
    @marks.critical
    def test_send_stt_from_wallet(self):
        recipient = transaction_recipients['F']
        sender = transaction_senders['Q']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'])
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
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
        send_transaction.sign_transaction_button.click()
        send_transaction.sign_transaction()
        self.network_api.find_transaction_by_unique_amount(recipient['address'], amount, token=True)

    @marks.testrail_id(5408)
    @marks.high
    def test_transaction_wrong_password_wallet(self):
        recipient = basic_user
        sender = wallet_users['A']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'])
        home_view = sign_in_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
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
        send_transaction.sign_with_password.click_until_presence_of_element(send_transaction.enter_password_input)
        send_transaction.enter_password_input.click()
        send_transaction.enter_password_input.send_keys('wrong_password')
        send_transaction.sign_button.click()
        if send_transaction.element_by_text_part('Transaction sent').is_element_displayed():
            self.driver.fail('Transaction was sent with a wrong password')

    @marks.testrail_id(6236)
    @marks.medium
    def test_transaction_appears_in_history(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        address = wallet_view.get_wallet_address()[2:]
        self.network_api.get_donate(address)
        recipient = "0x"+basic_user['address']
        sending_amount = "0.08"
        wallet_view.send_transaction(asset_name='ETHro', amount=sending_amount, recipient=recipient, sign_transaction=True)
        transactions_view = wallet_view.transaction_history_button.click()
        transactions_view.transactions_table.find_transaction(amount=sending_amount)
        transactions_view.transactions_table.find_transaction(amount="0.1")

    @marks.testrail_id(5461)
    @marks.medium
    def test_send_eth_from_wallet_incorrect_address(self):
        recipient = basic_user
        sender = wallet_users['B']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'])
        home_view = sign_in_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        transaction_amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['public_key'])
        send_transaction.done_button.click()
        if not send_transaction.find_text_part('Invalid address'):
            self.driver.fail("Invalid address accepted for input as recipient!")
        send_transaction.ok_button.click()

    @marks.logcat
    @marks.testrail_id(5416)
    @marks.critical
    def test_logcat_send_transaction_from_wallet(self):
        sender = transaction_senders['R']
        recipient = basic_user
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], unique_password)
        home_view = sign_in_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
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
        send_transaction.sign_transaction(unique_password)
        values_in_logcat = send_transaction.find_values_in_logcat(password=unique_password)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)

    @marks.testrail_id(5350)
    @marks.critical
    def test_send_token_with_7_decimals(self):
        sender = transaction_senders['S']
        recipient = basic_user
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'])
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
        send_transaction = wallet_view.send_transaction_button.click()
        adi_button = send_transaction.asset_by_name('ADI')
        send_transaction.select_asset_button.click_until_presence_of_element(adi_button)
        adi_button.click()
        send_transaction.amount_edit_box.click()
        amount = '0.0%s' % str(random.randint(10000, 99999)) + '1'
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.sign_transaction()
        self.network_api.find_transaction_by_unique_amount(recipient['address'], amount, token=True, decimals=7)

    @marks.testrail_id(5350)
    @marks.high
    def test_token_with_more_than_allowed_decimals(self):
        sender = wallet_users['C']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'])
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
        send_transaction = wallet_view.send_transaction_button.click()
        adi_button = send_transaction.asset_by_name('ADI')
        send_transaction.select_asset_button.click_until_presence_of_element(adi_button)
        adi_button.click()
        send_transaction.amount_edit_box.click()
        amount = '0.0%s' % str(random.randint(100000, 999999)) + '1'
        send_transaction.amount_edit_box.set_value(amount)
        error_text = 'Amount is too precise. Max number of decimals is 7.'
        if not send_transaction.element_by_text(error_text).is_element_displayed():
            self.errors.append('Warning about too precise amount is not shown when sending a transaction')
        send_transaction.back_button.click()
        wallet_view.receive_transaction_button.click()
        # temporary skipped due to 8601
        # wallet_view.send_transaction_request.click()
        # send_transaction.select_asset_button.click_until_presence_of_element(adi_button)
        # adi_button.click()
        # send_transaction.amount_edit_box.set_value(amount)
        # error_text = 'Amount is too precise. Max number of decimals is 7.'
        # if not send_transaction.element_by_text(error_text).is_element_displayed():
        #     self.errors.append('Warning about too precise amount is not shown when requesting a transaction')
        self.errors.verify_no_errors()

    @marks.testrail_id(5423)
    @marks.medium
    def test_send_valid_amount_after_insufficient_funds_error(self):
        sender = transaction_senders['T']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'])
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
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
        send_transaction.enter_recipient_address_input.set_value(basic_user['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.sign_transaction()
        self.network_api.find_transaction_by_unique_amount(sender['address'], valid_amount)

    @marks.testrail_id(5471)
    @marks.medium
    def test_insufficient_funds_wallet_0_balance(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.select_asset("STT")
        wallet_view.accounts_status_account.click()
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
        self.errors.verify_no_errors()

    @marks.testrail_id(5412)
    @marks.high
    def test_insufficient_funds_wallet_positive_balance(self):
        sender = wallet_users['A']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'])
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        eth_value = wallet_view.get_eth_value()
        stt_value = wallet_view.get_stt_value()
        if eth_value == 0 or stt_value == 0:
            self.driver.fail('No funds!')
        wallet_view.accounts_status_account.click()
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
        self.errors.verify_no_errors()

    @marks.testrail_id(5359)
    @marks.critical
    def test_modify_transaction_fee_values(self):
        sender = transaction_senders['U']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'])
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
        send_transaction = wallet_view.send_transaction_button.click()

        amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        recipient_address = basic_user['address']
        send_transaction.enter_recipient_address_input.set_value(recipient_address)
        send_transaction.done_button.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.network_fee_button.click()
        send_transaction.gas_limit_input.clear()
        send_transaction.gas_limit_input.set_value('1')
        send_transaction.gas_price_input.clear()
        send_transaction.gas_price_input.send_keys('1')
        send_transaction.update_fee_button.click()
        send_transaction.sign_with_password.click_until_presence_of_element(send_transaction.enter_password_input)
        send_transaction.enter_password_input.send_keys(common_password)
        send_transaction.sign_button.click()
        send_transaction.element_by_text('intrinsic gas too low', 'text').wait_for_visibility_of_element(40)
        send_transaction.ok_button.click()

        send_transaction.sign_transaction_button.click()
        send_transaction.network_fee_button.click()
        send_transaction.gas_limit_input.clear()
        gas_limit = '1005000'
        send_transaction.gas_limit_input.set_value(gas_limit)
        send_transaction.gas_price_input.clear()
        gas_price = str(round(float(send_transaction.gas_price_input.text)) + 10)
        send_transaction.gas_price_input.send_keys(gas_price)
        send_transaction.update_fee_button.click()
        send_transaction.sign_transaction()
        self.network_api.find_transaction_by_unique_amount(sender['address'], amount)

    @marks.testrail_id(5314)
    def test_can_see_all_transactions_in_history(self):
        address = wallet_users['D']['address']
        passphrase = wallet_users['D']['passphrase']

        ropsten_txs = self.network_api.get_transactions(address)
        ropsten_tokens = self.network_api.get_token_transactions(address)
        expected_txs_list = get_merged_txs_list(ropsten_txs, ropsten_tokens)
        signin_view = SignInView(self.driver)
        home_view = signin_view.recover_access(passphrase=passphrase)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
        transaction_view = wallet_view.transaction_history_button.click()

        status_tx_number = transaction_view.transactions_table.get_transactions_number()
        if status_tx_number < 1:
            self.driver.fail('No transactions found')

        for n in range(status_tx_number):
            transactions_details = transaction_view.transactions_table.transaction_by_index(n).click()
            tx_hash = transactions_details.get_transaction_hash()
            tx_from = transactions_details.get_sender_address()
            tx_to = transactions_details.get_recipient_address()
            if tx_from != expected_txs_list[tx_hash]['from']:
                self.errors.append('Transactions senders do not match!')
            if tx_to != expected_txs_list[tx_hash]['to']:
                self.errors.append('Transactions recipients do not match!')
            transactions_details.back_button.click()

        self.errors.verify_no_errors()

    @marks.testrail_id(5429)
    @marks.medium
    def test_set_currency(self):
        sign_in_view = SignInView(self.driver)
        user_currency = 'Euro (EUR)'
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_currency(user_currency)
        if not wallet_view.find_text_part('EUR'):
            self.driver.fail('EUR currency is not displayed')

    @marks.testrail_id(5407)
    @marks.medium
    def test_cant_send_transaction_in_offline_mode(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.chose_recipient_button.click()
        send_transaction.accounts_button.click()
        send_transaction.element_by_text("Status account").click()
        send_transaction.amount_edit_box.click()
        send_transaction.amount_edit_box.set_value("0")
        send_transaction.confirm()
        send_transaction.sign_transaction_button.click()
        send_transaction.cancel_button.click()
        send_transaction.toggle_airplane_mode()
        send_transaction.sign_transaction_button.click()
        if send_transaction.sign_with_password.is_element_displayed():
            self.driver.fail("Sign transaction button is active in offline mode")

    @marks.testrail_id(6225)
    @marks.high
    def test_send_funds_between_accounts_in_multiaccount_instance(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_account_address = wallet_view.get_wallet_address()[2:]
        wallet_view.back_button.click()
        self.network_api.get_donate(status_account_address)
        account_name = 'subaccount'
        wallet_view.add_account(account_name)

        wallet_view.just_fyi("Send transaction to new account")
        wallet_view.accounts_status_account.click()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        transaction_amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.accounts_button.click()
        send_transaction.element_by_text(account_name).click()
        send_transaction.sign_transaction_button.click()
        send_transaction.sign_transaction()
        self.network_api.wait_for_confirmation_of_transaction(status_account_address, transaction_amount)
        self.network_api.verify_balance_is_updated('0.1', status_account_address)

        wallet_view.just_fyi("Verifying previously sent transaction in new account")
        wallet_view.back_button.click()
        wallet_view.get_account_by_name(account_name).click()
        wallet_view.send_transaction_button.click()
        wallet_view.back_button.click()
        balance_after_receiving_tx = float(wallet_view.eth_asset_value.text)
        expected_balance = self.network_api.get_rounded_balance(balance_after_receiving_tx, transaction_amount)
        if balance_after_receiving_tx != expected_balance:
            self.driver.fail('New account balance %s does not match expected %s after receiving a transaction' % (
                balance_after_receiving_tx, transaction_amount))

        wallet_view.just_fyi("Sending eth from new account to main account")
        updated_balance = self.network_api.get_balance(status_account_address)
        wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        transaction_amount_1 = round(float(transaction_amount) * 0.05, 11)
        send_transaction.amount_edit_box.set_value(str(transaction_amount_1))
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.accounts_button.click()
        send_transaction.element_by_text('Status account').click()
        send_transaction.sign_transaction_button.click()
        total_fee = send_transaction.get_transaction_fee_total()
        send_transaction.sign_transaction()
        send_transaction.back_button.click()
        sub_account_address = wallet_view.get_wallet_address(account_name)[2:]
        self.network_api.wait_for_confirmation_of_transaction(status_account_address, transaction_amount)
        self.network_api.verify_balance_is_updated(updated_balance, status_account_address)

        wallet_view.just_fyi("Verify total ETH on main wallet view")
        self.network_api.wait_for_confirmation_of_transaction(status_account_address, transaction_amount_1)
        self.network_api.verify_balance_is_updated((updated_balance + transaction_amount_1), status_account_address)
        send_transaction.back_button.click()
        balance_of_sub_account = float(self.network_api.get_balance(sub_account_address)) / 1000000000000000000
        balance_of_status_account = float(self.network_api.get_balance(status_account_address)) / 1000000000000000000
        total_eth_from_two_accounts = float(wallet_view.eth_asset_value.text)
        expected_balance = self.network_api.get_rounded_balance(total_eth_from_two_accounts,
                                                                (balance_of_status_account + balance_of_sub_account))

        if total_eth_from_two_accounts != expected_balance:
            self.driver.fail('Total wallet balance %s != of Status account (%s) + SubAccount (%s)' % (
                total_eth_from_two_accounts, balance_of_status_account, balance_of_sub_account))

    @marks.testrail_id(6235)
    @marks.medium
    def test_can_change_account_settings(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_account_address = wallet_view.get_wallet_address()
        wallet_view.account_options_button.click()

        wallet_view.just_fyi('open Account Settings screen and check that all elements are shown')
        wallet_view.account_settings_button.click()
        for text in 'On Status tree', status_account_address, "m/44'/60'/0'/0/0":
            if not wallet_view.element_by_text(text).is_element_displayed():
                self.errors.append("'%s' text is not shown on Account Settings screen!" % text)

        wallet_view.just_fyi('change account name/color and verified applied changes')
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        wallet_view.account_name_input.send_keys(account_name)
        wallet_view.account_color_button.select_color_by_position(1)
        wallet_view.apply_settings_button.click()
        wallet_view.element_by_text('This device').scroll_to_element()
        wallet_view.back_button.click()
        wallet_view.back_button.click()
        account_button = wallet_view.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.driver.fail('Account name was not changed')
        if not account_button.color_matches('multi_account_color.png'):
            self.driver.fail('Account color does not match expected')

        self.errors.verify_no_errors()


@marks.transaction
class TestTransactionWalletMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(5378)
    @marks.skip
    @marks.high
    # TODO: temporary skipped due to 8601
    def test_transaction_message_sending_from_wallet(self):
        recipient = transaction_recipients['E']
        sender = transaction_senders['V']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1 = device_1.recover_access(passphrase=sender['passphrase'])
        home_2 = device_2.recover_access(passphrase=recipient['passphrase'])

        chat_1 = home_1.add_contact(recipient['public_key'])
        chat_1.get_back_to_home_view()

        wallet_1 = home_1.wallet_button.click()
        wallet_1.set_up_wallet()
        wallet_1.accounts_status_account.click()
        send_transaction = wallet_1.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.recent_recipients_button.click()
        send_transaction.element_by_text_part(recipient['username']).click()
        send_transaction.sign_transaction_button.click()
        send_transaction.sign_transaction()

        wallet_1.home_button.click()
        home_1.get_chat_with_user(recipient['username']).click()
        if not chat_1.chat_element_by_text(amount).is_element_displayed():
            self.errors.append('Transaction message is not shown in 1-1 chat for the sender')
        chat_2 = home_2.get_chat_with_user(sender['username']).click()
        if not chat_2.chat_element_by_text(amount).is_element_displayed():
            self.errors.append('Transaction message is not shown in 1-1 chat for the recipient')
        self.errors.verify_no_errors()
