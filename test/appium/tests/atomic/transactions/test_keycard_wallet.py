import pytest
from support.utilities import get_merged_txs_list
from tests import marks, pin, puk, pair_code
from tests.base_test_case import SingleDeviceTestCase, MultipleSharedDeviceTestCase, create_shared_drivers
from tests.users import transaction_senders, basic_user, wallet_users
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="keycard_tx_1")
class TestKeycardTxOneDeviceMerged(MultipleSharedDeviceTestCase):
    @classmethod
    def setup_class(cls):
        cls.user = transaction_senders['P']
        cls.address = '0x%s' % basic_user['address']
        cls.drivers, cls.loop = create_shared_drivers(1)
        cls.sign_in = SignInView(cls.drivers[0])

        cls.home = cls.sign_in.recover_access(passphrase=cls.user['passphrase'], keycard=True)
        cls.wallet = cls.home.wallet_button.click()
        cls.assets = ('ETH', 'ADI', 'STT')
        [cls.wallet.wait_balance_is_changed(asset) for asset in cls.assets]
        cls.initial_balances = dict()
        for asset in cls.assets:
            cls.initial_balances[asset] = cls.wallet.get_asset_amount_by_name(asset)

    @marks.testrail_id(700767)
    @marks.critical
    def test_keycard_send_tx_eth(self):
        wallet = self.home.wallet_button.click()
        transaction_amount = wallet.get_unique_amount()
        wallet.send_transaction(amount=transaction_amount, sign_transaction=True, keycard=True,
                                recipient=self.address)

        wallet.just_fyi('Check that transaction is appeared in transaction history')
        transaction = wallet.find_transaction_in_history(amount=transaction_amount, return_hash=True)
        self.wallet.wallet_button.double_click()
        self.network_api.find_transaction_by_hash(transaction)
        self.network_api.wait_for_confirmation_of_transaction(self.user['address'], transaction_amount)
        self.wallet.wait_balance_is_changed('ETH', initial_balance=self.initial_balances['ETH'])

    @marks.testrail_id(700768)
    @marks.critical
    def test_keycard_relogin_after_restore(self):
        self.sign_in.just_fyi('Check that username and public key match expected')
        public_key, default_username = self.sign_in.get_public_key_and_username(return_username=True)
        profile = self.sign_in.get_profile_view()
        if public_key != self.user['public_key']:
            self.errors.append('Public key %s does not match expected' % public_key)
        if default_username != self.user['username']:
            self.errors.append('Default username %s does not match expected' % default_username)
        profile.logout()

        self.sign_in.just_fyi('Check that can login with restored from mnemonic keycard account')
        self.sign_in.sign_in(keycard=True)
        if not self.sign_in.home_button.is_element_displayed(10):
            self.sign_in.driver.fail('Keycard user is not logged in')

        self.errors.verify_no_errors()

    @marks.testrail_id(700769)
    @marks.critical
    @marks.transaction
    def test_keycard_send_tx_sign_message_request_stt_testdapp(self):
        self.home.home_button.double_click()
        status_test_dapp = self.home.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()

        self.wallet.just_fyi("Requesting STT in dapp")
        status_test_dapp.assets_button.click()
        send_tx = status_test_dapp.request_stt_button.click()
        send_tx.sign_transaction(keycard=True)

        self.wallet.just_fyi("Checking signing message")
        status_test_dapp.transactions_button.click()
        status_test_dapp.sign_message_button.click()
        if not send_tx.element_by_text("Test message").is_element_displayed():
            self.errors.append("No message shown when signing!")
        keycard = send_tx.sign_with_keycard_button.click()
        keycard.enter_default_pin()
        if not keycard.element_by_text_part('Signed message').is_element_displayed():
            self.errors.append('Message was not signed')

        keycard.just_fyi('Check logcat for sensitive data')
        values_in_logcat = send_tx.find_values_in_logcat(pin=pin, puk=puk, password=pair_code)
        if values_in_logcat:
            self.sign_in.driver.fail("After signing message: %s" % values_in_logcat)

        self.wallet.just_fyi("Check send 2 txs in batch")
        status_test_dapp.send_two_tx_in_batch_button.scroll_to_element()
        send_tx = status_test_dapp.send_two_tx_in_batch_button.click()
        send_tx.sign_transaction(keycard=True)
        if not send_tx.sign_with_keycard_button.is_element_displayed(10):
            self.sign_in.driver.fail('Second send transaction screen did not appear!')
        send_tx.sign_transaction(keycard=True)

        self.wallet.just_fyi("Checking send 2 txs one after another")
        status_test_dapp.send_two_tx_one_by_one_button.scroll_to_element()
        send_tx = status_test_dapp.send_two_tx_one_by_one_button.click()
        send_tx.sign_transaction(keycard=True)
        if not send_tx.sign_with_keycard_button.is_element_displayed(20):
            self.sign_in.driver.fail('Second send transaction screen did not appear!')
        send_tx.sign_transaction(keycard=True)

        self.wallet.just_fyi('Verify that wallet balance is updated after receiving money from faucet')
        self.home.wallet_button.click()
        self.wallet.wait_balance_is_changed('STT', initial_balance=self.initial_balances['STT'])
        self.errors.verify_no_errors()

    @marks.testrail_id(700770)
    @marks.critical
    def test_keycard_wallet_recover_pairing_check_balance_after_offline_tx_history(self):
        user = transaction_senders['A']
        self.sign_in.toggle_airplane_mode()
        self.sign_in.driver.reset()

        self.sign_in.just_fyi('Keycard: recover multiaccount with pairing code ')
        self.sign_in.accept_tos_checkbox.enable()
        self.sign_in.get_started_button.click_until_presence_of_element(self.sign_in.access_key_button)
        self.sign_in.access_key_button.click()
        self.sign_in.recover_with_keycard_button.click()
        keycard = self.sign_in.begin_recovery_button.click()
        keycard.connect_pairing_card_button.click()
        keycard.pair_code_input.set_value(pair_code)
        self.sign_in.pair_to_this_device_button.click()
        keycard.enter_default_pin()
        self.sign_in.maybe_later_button.click_until_presence_of_element(self.sign_in.lets_go_button)
        self.sign_in.lets_go_button.click_until_absense_of_element(self.sign_in.lets_go_button)
        self.sign_in.home_button.wait_for_visibility_of_element(30)

        self.sign_in.just_fyi("Check balance will be restored after going back online")
        self.sign_in.toggle_airplane_mode()
        wallet = self.home.wallet_button.click()
        [wallet.wait_balance_is_changed(asset) for asset in ("ETH", "LXS")]

        self.wallet.just_fyi("Checking whole tx history after backing from offline")
        self.wallet.accounts_status_account.click()
        address = user['address']
        ropsten_txs = self.network_api.get_transactions(address)
        ropsten_tokens = self.network_api.get_token_transactions(address)
        expected_txs_list = get_merged_txs_list(ropsten_txs, ropsten_tokens)
        transactions = self.wallet.transaction_history_button.click()
        if self.wallet.element_by_translation_id("transactions-history-empty").is_element_displayed():
            self.wallet.pull_to_refresh()
        status_tx_number = transactions.transactions_table.get_transactions_number()
        if status_tx_number < 1:
            self.errors.append('No transactions found')
        for n in range(status_tx_number):
            transactions_details = transactions.transactions_table.transaction_by_index(n).click()
            tx_hash = transactions_details.get_transaction_hash()
            tx_from = transactions_details.get_sender_address()
            tx_to = transactions_details.get_recipient_address()
            if tx_from != expected_txs_list[tx_hash]['from']:
                self.errors.append('Transactions senders do not match!')
            if tx_to != expected_txs_list[tx_hash]['to']:
                self.errors.append('Transactions recipients do not match!')
            transactions_details.close_button.click()
        self.errors.verify_no_errors()


class TestTransactionWalletSingleDevice(SingleDeviceTestCase):

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
        wallet.wait_balance_is_equal_expected_amount(asset='ETH', expected_balance=0, main_screen=False)

    @marks.testrail_id(6310)
    @marks.medium
    @marks.transaction
    def test_keycard_sign_typed_message_deploy_simple_contract(self):
        sender = transaction_senders['W']
        home = SignInView(self.driver).recover_access(sender['passphrase'], keycard=True)
        wallet = home.wallet_button.click()
        status_test_dapp = home.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click_until_presence_of_element(status_test_dapp.sign_typed_message_button)

        wallet.just_fyi("Checking sign typed message")
        send_transaction = status_test_dapp.sign_typed_message_button.click()
        send_transaction.sign_with_keycard_button.click()
        keycard_view = send_transaction.sign_with_keycard_button.click()
        keycard_view.enter_default_pin()
        if not keycard_view.element_by_text_part('0xde3048417').is_element_displayed():
            self.errors.append('Typed message was not signed')

        wallet.just_fyi("Checking deploy simple contract")
        send_transaction_view = status_test_dapp.deploy_contract_button.click()
        send_transaction_view.sign_transaction(keycard=True)
        if not status_test_dapp.element_by_text('Contract deployed at: ').is_element_displayed(300):
            self.driver.fail('Contract was not created or tx taking too long')
        for text in ['Call contract get function',
                     'Call contract set function', 'Call function 2 times in a row']:
            status_test_dapp.element_by_text(text).scroll_to_element()
        self.errors.verify_no_errors()
