from support.utilities import get_merged_txs_list
from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from tests.users import transaction_senders, basic_user, wallet_users
from views.sign_in_view import SignInView


class TestTransactionWalletSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(6289)
    @marks.critical
    @marks.transaction
    def test_keycard_send_eth_from_wallet_to_address(self):
        recipient = basic_user
        sender = transaction_senders['P']
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(sender['passphrase'], keycard=True)
        wallet = home.wallet_button.click()
        wallet.wait_balance_is_changed()
        transaction_amount = wallet.get_unique_amount()
        wallet.send_transaction(amount=transaction_amount, sign_transaction=True, keycard=True, recipient='0x%s' % recipient['address'])

        wallet.just_fyi('Check that transaction is appeared in transaction history')
        transaction = wallet.find_transaction_in_history(amount=transaction_amount, return_hash=True)
        self.network_api.find_transaction_by_hash(transaction)

    @marks.testrail_id(6291)
    @marks.critical
    @marks.transaction
    def test_keycard_can_see_all_transactions_in_history(self):
        address = wallet_users['D']['address']
        passphrase = wallet_users['D']['passphrase']

        ropsten_txs = self.network_api.get_transactions(address)
        ropsten_tokens = self.network_api.get_token_transactions(address)
        expected_txs_list = get_merged_txs_list(ropsten_txs, ropsten_tokens)
        signin_view = SignInView(self.driver)
        home_view = signin_view.recover_access(passphrase=passphrase, keycard=True)
        wallet_view = home_view.wallet_button.click()
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
            transactions_details.close_button.click_until_presence_of_element(wallet_view.send_transaction_button)

        self.errors.verify_no_errors()

    @marks.testrail_id(6292)
    @marks.transaction
    @marks.medium
    def test_keycard_send_funds_between_accounts_set_max_in_multiaccount_instance(self):
        sign_in_view = SignInView(self.driver).create_user(keycard=True)
        wallet = sign_in_view.wallet_button.click()
        status_account_address = wallet.get_wallet_address()[2:]
        self.network_api.get_donate(status_account_address, external_faucet=True)
        wallet.wait_balance_is_changed()
        account_name = 'subaccount'
        wallet.add_account(account_name, keycard=True)
        wallet.get_account_by_name(account_name).click()
        wallet.get_account_options_by_name(account_name).click()
        wallet.account_settings_button.click()
        wallet.swipe_up()

        wallet.just_fyi("Checking that delete account and importing account are not available on keycard")
        if wallet.delete_account_button.is_element_displayed(10):
            self.errors.append('Delete account option is shown on added account "On Status Tree"!')
        wallet.wallet_button.double_click()
        wallet.add_account_button.click()
        if wallet.enter_a_seed_phrase_button.is_element_displayed():
            self.errors.append('Importing account option is available on keycard!')
        wallet.click_system_back_button()

        wallet.just_fyi("Send transaction to new account")
        transaction_amount = '0.004'
        initial_balance = self.network_api.get_balance(status_account_address)
        wallet.send_transaction(account_name=account_name, amount=transaction_amount, keycard=True)
        self.network_api.wait_for_confirmation_of_transaction(status_account_address, transaction_amount)
        self.network_api.verify_balance_is_updated(str(initial_balance), status_account_address)

        wallet.just_fyi("Verifying previously sent transaction in new account")
        wallet.get_account_by_name(account_name).click()
        wallet.send_transaction_button.click()
        wallet.close_send_transaction_view_button.click()
        balance_after_receiving_tx = float(wallet.get_asset_amount_by_name('ETH'))
        expected_balance = self.network_api.get_rounded_balance(balance_after_receiving_tx, transaction_amount)
        if balance_after_receiving_tx != expected_balance:
            self.driver.fail('New account balance %s does not match expected %s after receiving a transaction' % (
                balance_after_receiving_tx, transaction_amount))

        wallet.just_fyi("Sending eth from new account to main account")
        updated_balance = self.network_api.get_balance(status_account_address)
        transaction_amount_1 = round(float(transaction_amount) * 0.2, 11)
        wallet.wait_balance_is_changed()
        wallet.get_account_by_name(account_name).click()
        send_transaction = wallet.send_transaction(from_main_wallet=False, account_name=wallet.status_account_name,
                                                   amount=transaction_amount_1, keycard=True)
        wallet.close_button.click()
        sub_account_address = wallet.get_wallet_address(account_name)[2:]
        self.network_api.wait_for_confirmation_of_transaction(sub_account_address, transaction_amount_1)
        wallet.find_transaction_in_history(amount=format(float(transaction_amount_1), '.11f').rstrip('0'))

        wallet.just_fyi("Check transactions on subaccount")
        self.network_api.verify_balance_is_updated(updated_balance, status_account_address)

        wallet.just_fyi("Verify total ETH on main wallet view")
        self.network_api.wait_for_confirmation_of_transaction(status_account_address, transaction_amount_1)
        self.network_api.verify_balance_is_updated((updated_balance + transaction_amount_1), status_account_address)
        wallet.close_button.click()
        balance_of_sub_account = float(self.network_api.get_balance(sub_account_address)) / 1000000000000000000
        balance_of_status_account = float(self.network_api.get_balance(status_account_address)) / 1000000000000000000
        wallet.scan_tokens()
        total_eth_from_two_accounts = float(wallet.get_asset_amount_by_name('ETH'))
        expected_balance = self.network_api.get_rounded_balance(total_eth_from_two_accounts,
                                                                (balance_of_status_account + balance_of_sub_account))

        if total_eth_from_two_accounts != expected_balance:
            self.driver.fail('Total wallet balance %s != of Status account (%s) + SubAccount (%s)' % (
                total_eth_from_two_accounts, balance_of_status_account, balance_of_sub_account))

        wallet.just_fyi("Check that can set max and send transaction with max amount from subaccount")
        wallet.get_account_by_name(account_name).click()
        wallet.send_transaction_button.click()
        send_transaction.set_max_button.click()
        set_amount = float(send_transaction.amount_edit_box.text)
        if set_amount == 0.0 or set_amount >= balance_of_sub_account:
            self.driver.fail('Value after setting up max amount is set to %s' % str(set_amount))
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.accounts_button.click()
        send_transaction.element_by_text(wallet.status_account_name).click()
        send_transaction.sign_transaction_button.click()
        send_transaction.sign_transaction(keycard=True)
        wallet.element_by_text('Assets').click()
        wallet.wait_balance_is_equal_expected_amount(asset='ETH', expected_balance=0)
