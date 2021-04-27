from support.utilities import get_merged_txs_list
from tests import marks, pin, puk, pair_code
from tests.base_test_case import SingleDeviceTestCase
from tests.users import transaction_senders, basic_user, wallet_users
from views.sign_in_view import SignInView


@marks.transaction
class TestTransactionWalletSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(6289)
    @marks.critical
    def test_keycard_send_eth_from_wallet_to_address(self):
        recipient = basic_user
        sender = transaction_senders['P']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'], keycard=True)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
        transaction_amount = wallet_view.get_unique_amount()
        wallet_view.send_transaction(amount=transaction_amount,
                                     sign_transaction=True,
                                     keycard=True,
                                     recipient='0x%s'%recipient['address'])
        self.network_api.find_transaction_by_unique_amount(sender['address'], transaction_amount)

        wallet_view.just_fyi('Check that transaction is appeared in transaction history')
        wallet_view.find_transaction_in_history(amount=transaction_amount)

        wallet_view.just_fyi('Check logcat for sensitive data')
        values_in_logcat = wallet_view.find_values_in_logcat(pin=pin, puk=puk, password=pair_code)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)


    @marks.testrail_id(6290)
    @marks.high
    def test_keycard_fetching_balance_after_offline(self):
        sender = wallet_users['A']
        sign_in_view = SignInView(self.driver)

        sign_in_view.just_fyi('Restore account with funds offline')
        sign_in_view.toggle_airplane_mode()
        sign_in_view.recover_access(sender['passphrase'], keycard=True)
        home_view = sign_in_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()

        sign_in_view.just_fyi('Go back to online and check that balance is updated')
        sign_in_view.toggle_airplane_mode()
        wallet_view.wait_balance_is_changed('ETH')
        wallet_view.scan_tokens('STT')

        sign_in_view.just_fyi('Send some tokens to other account')
        recipient = "0x" + basic_user['address']
        sending_amount = wallet_view.get_unique_amount()
        asset = 'STT'
        wallet_view.accounts_status_account.click_until_presence_of_element(wallet_view.send_transaction_button)
        initial_amount_STT = wallet_view.get_asset_amount_by_name('STT')
        wallet_view.send_transaction(asset_name=asset, amount=sending_amount, recipient=recipient,
                                     sign_transaction=True, keycard=True)
        sign_in_view.toggle_airplane_mode()
        self.network_api.wait_for_confirmation_of_transaction(basic_user['address'], sending_amount, confirmations=6, token=True)

        sign_in_view.just_fyi('Change that balance is updated and transaction is appeared in history')

        sign_in_view.toggle_airplane_mode()
        wallet_view.wait_balance_is_changed('STT', initial_amount_STT)
        wallet_view.find_transaction_in_history(amount=sending_amount, asset='STT')

    @marks.testrail_id(6291)
    @marks.critical
    def test_keycard_can_see_all_transactions_in_history(self):
        address = wallet_users['D']['address']
        passphrase = wallet_users['D']['passphrase']

        ropsten_txs = self.network_api.get_transactions(address)
        ropsten_tokens = self.network_api.get_token_transactions(address)
        expected_txs_list = get_merged_txs_list(ropsten_txs, ropsten_tokens)
        signin_view = SignInView(self.driver)
        home_view = signin_view.recover_access(passphrase=passphrase, keycard=True)
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
            transactions_details.back_button.click_until_presence_of_element(wallet_view.send_transaction_button)

        self.errors.verify_no_errors()

    @marks.testrail_id(6292)
    @marks.transaction
    @marks.medium
    def test_keycard_send_funds_between_accounts_in_multiaccount_instance(self):
        sign_in_view = SignInView(self.driver).create_user(keycard=True)
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_account_address = wallet_view.get_wallet_address()[2:]
        self.network_api.get_donate(status_account_address, external_faucet=True)
        wallet_view.wait_balance_is_changed()
        account_name = 'subaccount'
        wallet_view.add_account(account_name, keycard=True)

        wallet_view.just_fyi("Send transaction to new account")
        wallet_view.accounts_status_account.click()
        transaction_amount = '0.002'
        initial_balance = self.network_api.get_balance(status_account_address)
        send_transaction = wallet_view.send_transaction(account_name=account_name,
                                     amount=transaction_amount,
                                     keycard=True)
        self.network_api.wait_for_confirmation_of_transaction(status_account_address, transaction_amount)
        self.network_api.verify_balance_is_updated(str(initial_balance), status_account_address)

        wallet_view.just_fyi("Verifying previously sent transaction in new account")
        send_transaction.back_button.click()
        wallet_view.get_account_by_name(account_name).click()
        wallet_view.send_transaction_button.click()
        wallet_view.back_button.click()
        balance_after_receiving_tx = float(wallet_view.get_asset_amount_by_name('ETH'))
        expected_balance = self.network_api.get_rounded_balance(balance_after_receiving_tx, transaction_amount)
        if balance_after_receiving_tx != expected_balance:
            self.driver.fail('New account balance %s does not match expected %s after receiving a transaction' % (
                balance_after_receiving_tx, transaction_amount))

        wallet_view.just_fyi("Sending eth from new account to main account")
        updated_balance = self.network_api.get_balance(status_account_address)
        transaction_amount_1 = round(float(transaction_amount) * 0.1, 11)
        wallet_view.wait_balance_is_changed()
        wallet_view.get_account_by_name(account_name).click()
        send_transaction = wallet_view.send_transaction(account_name=wallet_view.status_account_name,
                                                        amount=transaction_amount_1,
                                                        keycard=True,
                                                        default_gas_price=True)
        send_transaction.back_button.click()
        sub_account_address = wallet_view.get_wallet_address(account_name)[2:]
        self.network_api.wait_for_confirmation_of_transaction(sub_account_address, transaction_amount_1)
        wallet_view.find_transaction_in_history(amount=transaction_amount)
        wallet_view.find_transaction_in_history(amount=format(float(transaction_amount_1),'.11f').rstrip('0'))
        #transactions_view = wallet_view.transaction_history_button.click()

        wallet_view.just_fyi("Check transactions on subaccount")
        #transactions_view.transactions_table.find_transaction(amount=transaction_amount)
        #transactions_view.transactions_table.find_transaction(amount=format(float(transaction_amount_1),'.11f').rstrip('0'))
        self.network_api.verify_balance_is_updated(updated_balance, status_account_address)

        wallet_view.just_fyi("Verify total ETH on main wallet view")
        self.network_api.wait_for_confirmation_of_transaction(status_account_address, transaction_amount_1, 3)
        self.network_api.verify_balance_is_updated((updated_balance + transaction_amount_1), status_account_address)
        send_transaction.back_button.click()
        balance_of_sub_account = float(self.network_api.get_balance(sub_account_address)) / 1000000000000000000
        balance_of_status_account = float(self.network_api.get_balance(status_account_address)) / 1000000000000000000
        wallet_view.scan_tokens()
        total_eth_from_two_accounts = float(wallet_view.get_asset_amount_by_name('ETH'))
        expected_balance = self.network_api.get_rounded_balance(total_eth_from_two_accounts,
                                                                (balance_of_status_account + balance_of_sub_account))

        if total_eth_from_two_accounts != expected_balance:
            self.driver.fail('Total wallet balance %s != of Status account (%s) + SubAccount (%s)' % (
                total_eth_from_two_accounts, balance_of_status_account, balance_of_sub_account))

        wallet_view.just_fyi("Check that can set max and send transaction with max amount from subaccount")
        wallet_view.get_account_by_name(account_name).click()
        wallet_view.send_transaction_button.click()
        send_transaction.set_max_button.click()
        set_amount = float(send_transaction.amount_edit_box.text)
        if set_amount == 0.0 or set_amount >= balance_of_sub_account:
            self.driver.fail('Value after setting up max amount is set to %s' % str(set_amount))
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.accounts_button.click()
        send_transaction.element_by_text(wallet_view.status_account_name).click()
        send_transaction.sign_transaction_button.click()
        send_transaction.sign_transaction(keycard=True)
        wallet_view.element_by_text('Assets').click()
        wallet_view.wait_balance_is_equal_expected_amount(asset='ETH', expected_balance=0)
