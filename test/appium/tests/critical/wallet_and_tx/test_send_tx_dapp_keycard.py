import random

import pytest
from support.utilities import get_merged_txs_list

from tests import marks, common_password, pin, puk, pair_code
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from tests.users import transaction_senders, basic_user, wallet_users, ens_user_message_sender, ens_user
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="one_1")
@marks.critical
class TestSendTxDeviceMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.user = transaction_senders['ETH_STT_4']
        self.recipient_address = '0x%s' % transaction_senders['ETH_ADI_STT_3']['address']
        self.drivers, self.loop = create_shared_drivers(1)
        [self.amount_adi, self.amount_eth, self.amount_stt] = ['0.000%s' % str(random.randint(100, 999)) + '1' for _ in
                                                               range(3)]
        self.sign_in = SignInView(self.drivers[0])
        self.home = self.sign_in.recover_access(self.user['passphrase'])
        self.wallet = self.home.wallet_button.click()
        self.assets = ('ETH', 'YEENUS', 'STT')
        self.token_8_dec = 'YEENUS'
        [self.wallet.wait_balance_is_changed(asset) for asset in self.assets]
        self.initial_balances = dict()
        for asset in self.assets:
            self.initial_balances[asset] = self.wallet.get_asset_amount_by_name(asset)
        self.wallet.send_transaction(amount=self.amount_eth, recipient=self.recipient_address)
        self.wallet.send_transaction(amount=self.amount_adi, recipient=self.recipient_address, asset_name=self.token_8_dec)

    @marks.testrail_id(700763)
    def test_send_tx_eth_check_logcat(self):
        self.wallet.just_fyi('Check that transaction is appeared in tx history')
        self.wallet.find_transaction_in_history(amount=self.amount_eth)
        self.wallet.wallet_button.double_click()
        self.network_api.wait_for_confirmation_of_transaction(self.user['address'], self.amount_eth)
        self.wallet.wait_balance_is_changed('ETH', initial_balance=self.initial_balances['ETH'])

        self.wallet.just_fyi('Check logcat for sensitive data')
        values_in_logcat = self.wallet.find_values_in_logcat(password=common_password)
        if values_in_logcat:
            self.wallet.driver.fail(values_in_logcat)

    @marks.testrail_id(700764)
    def test_send_tx_token_8_decimals(self):
        asset = self.token_8_dec
        self.wallet.just_fyi("Checking tx with 7 decimals")
        transaction_adi = self.wallet.find_transaction_in_history(amount=self.amount_adi, asset=asset, return_hash=True)
        self.wallet.wallet_button.double_click()
        self.network_api.find_transaction_by_hash(transaction_adi)
        self.wallet.wait_balance_is_changed(asset, initial_balance=self.initial_balances[asset])

    @marks.testrail_id(5342)
    def test_send_tx_sign_message_2tx_in_batch_tx_filters_request_stt_testdapp(self):
        self.wallet.home_button.click()
        status_test_dapp = self.home.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()

        self.wallet.just_fyi("Checking request STT")
        status_test_dapp.assets_button.click()
        status_test_dapp.request_stt_button.wait_for_element(60)
        send_transaction = status_test_dapp.request_stt_button.click()
        send_transaction.sign_transaction()

        self.wallet.just_fyi("Checking signing message")
        status_test_dapp.transactions_button.click()
        send_transaction = status_test_dapp.sign_message_button.click()
        send_transaction.enter_password_input.send_keys(common_password)
        send_transaction.sign_button.click()
        if not status_test_dapp.element_by_text_part('Signed message').is_element_displayed():
            self.errors.append('Message was not signed')

        send_transaction.just_fyi('Check logcat for sensitive data')
        values_in_logcat = send_transaction.find_values_in_logcat(password=common_password)
        if values_in_logcat:
            self.errors.append("When signing message from dapp: %s" % values_in_logcat)

        self.wallet.just_fyi("Checking send 2 txs in batch")
        status_test_dapp.send_two_tx_in_batch_button.scroll_to_element()
        send_transaction = status_test_dapp.send_two_tx_in_batch_button.click()
        send_transaction.sign_transaction()
        if not send_transaction.sign_with_password.is_element_displayed(10):
            self.errors.append('Second send transaction screen did not appear!')
        send_transaction.sign_transaction()

        self.wallet.just_fyi("Checking send 2 txs one after another")
        status_test_dapp.send_two_tx_one_by_one_button.scroll_to_element()
        send_transaction = status_test_dapp.send_two_tx_one_by_one_button.click()
        send_transaction.sign_transaction()
        if not send_transaction.sign_with_password.is_element_displayed(20):
            self.errors.append('Second send transaction screen did not appear!')
        send_transaction.sign_transaction()

        self.wallet.just_fyi("Checking test filters")
        status_test_dapp.test_filters_button.scroll_and_click()
        for element in status_test_dapp.element_by_text('eth_uninstallFilter'), status_test_dapp.ok_button:
            if element.is_element_displayed(10):
                self.errors.append("'Test filters' button produced an error")
        self.errors.verify_no_errors()

    @marks.testrail_id(700765)
    def test_send_tx_custom_token_18_decimals_invalid_password(self):
        contract_address, name, symbol, decimals = '0xaFF4481D10270F50f203E0763e2597776068CBc5', 'Weenus 💪', 'WEENUS', '18'
        self.home.wallet_button.double_click()

        self.wallet.just_fyi("Check that can add custom token")
        self.wallet.multiaccount_more_options.click()
        self.wallet.manage_assets_button.click()
        token_view = self.wallet.add_custom_token_button.click()
        token_view.contract_address_input.send_keys(contract_address)
        if token_view.name_input.text != name:
            self.errors.append('Name for custom token was not set')
        if token_view.symbol_input.text != symbol:
            self.errors.append('Symbol for custom token was not set')
        if token_view.decimals_input.text != decimals:
            self.errors.append('Decimals for custom token was not set')
        token_view.add_button.click()
        token_view.close_button.click()
        self.wallet.asset_by_name(symbol).scroll_to_element()
        if not self.wallet.asset_by_name(symbol).is_element_displayed():
            self.errors.append('Custom token is not shown on Wallet view')
        send_tx = self.wallet.send_transaction_from_main_screen.click()
        send_tx.select_asset_button.click()
        asset_button = send_tx.asset_by_name(symbol)
        send_tx.select_asset_button.click_until_presence_of_element(
            send_tx.eth_asset_in_select_asset_bottom_sheet_button)
        asset_button.click()
        send_tx.amount_edit_box.click()
        send_tx.amount_edit_box.send_keys(self.amount_eth)
        send_tx.set_recipient_address(self.recipient_address)
        send_tx.sign_transaction_button.click()
        if self.wallet.sign_in_phrase.is_element_displayed():
            self.wallet.set_up_wallet_when_sending_tx()

        send_tx.just_fyi('Check that can not sign tx with invalid password')
        self.wallet.next_button.click_if_shown()
        self.wallet.ok_got_it_button.click_if_shown()
        send_tx.sign_with_password.click_until_presence_of_element(send_tx.enter_password_input)
        send_tx.enter_password_input.click()
        send_tx.enter_password_input.send_keys('wrong_password')
        send_tx.sign_button.click()
        if send_tx.element_by_text_part('Transaction sent').is_element_displayed():
            self.errors.append('Transaction was sent with a wrong password')

        self.wallet.just_fyi("Check that can send tx with custom token")
        send_tx.enter_password_input.click()
        send_tx.enter_password_input.clear()
        send_tx.enter_password_input.send_keys(common_password)
        send_tx.sign_button.click_until_absense_of_element(send_tx.sign_button)
        send_tx.ok_button.wait_for_element(120)
        if not self.wallet.element_by_translation_id("transaction-sent").is_element_displayed():
            self.errors.append("Tx is not sent!")
        send_tx.ok_button.click()

        # TODO: disabled due to 10838 (rechecked 04.10.22, valid)
        # transactions_view = wallet.transaction_history_button.click()
        # transactions_view.transactions_table.find_transaction(amount=amount, asset=symbol)
        self.errors.verify_no_errors()

    @marks.testrail_id(700757)
    def test_send_tx_set_recipient_options(self):
        nickname = 'my_some_nickname'
        account_name = 'my_acc_name'
        account_address = '0x8c2E3Cd844848E79cFd4671cE45C12F210b630d7'
        recent_add_to_fav_name = 'my_Recent_STT'
        recent_add_to_fav_address = '0xcf2272205cc0cf96cfbb9dd740bd681d1e86901e'
        ens_status, ens_other = ens_user_message_sender, ens_user

        basic_add_to_fav_name = 'my_basic_address'
        self.drivers[0].reset()
        self.home = self.sign_in.recover_access(wallet_users['D']['passphrase'])

        self.home.just_fyi('Add new account and new ENS contact for recipient')
        chat = self.home.add_contact(ens_status['ens'])
        chat.chat_options.click()
        chat.view_profile_button.click_until_presence_of_element(chat.remove_from_contacts)
        chat.set_nickname(nickname)
        wallet = self.home.wallet_button.click()
        wallet.add_account(account_name=account_name)
        wallet.accounts_status_account.click()
        send_tr = wallet.send_transaction_button.click()

        wallet.just_fyi("Check that can't send to invalid address")
        send_tr.amount_edit_box.click()
        send_tr.amount_edit_box.send_keys(send_tr.get_unique_amount())
        send_tr.chose_recipient_button.click()
        for address in (basic_user['public_key'], '0xDE709F2102306220921060314715629080E2fB77'):
            send_tr.enter_recipient_address_input.send_keys(address)
            send_tr.enter_recipient_address_input.click()
            send_tr.done_button.click()
            if send_tr.set_max_button.is_element_displayed():
                self.errors.append('Can proceed with wrong address %s in recipient' % address)

        send_tr.just_fyi('Set one of my accounts')
        send_tr.chose_recipient_button.click_if_shown()
        send_tr.element_by_translation_id("my-accounts").scroll_and_click()
        send_tr.element_by_text(account_name).click()
        if send_tr.enter_recipient_address_text.text != send_tr.get_formatted_recipient_address(account_address):
            self.errors.append('Added account is not resolved as recipient')

        send_tr.just_fyi('Set contract address from recent and check smart contract error')
        send_tr.chose_recipient_button.click()
        send_tr.element_by_translation_id("recent").click()
        send_tr.element_by_text('↑ 0.02 ETHgo').scroll_and_click()
        if not send_tr.element_by_translation_id("warning-sending-to-contract-descr").is_element_displayed():
            self.driver.fail('No warning is shown at attempt to set as recipient smart contract')
        send_tr.ok_button.click()
        send_tr.element_by_text('↓ 2 STT').scroll_and_click()
        send_tr.add_to_favorites(recent_add_to_fav_name)
        wallet.element_by_translation_id("recent").click()

        send_tr.just_fyi('Scan invalid QR')
        send_tr.scan_qr_code_button.click()
        send_tr.allow_button.click(1)
        wallet.enter_qr_edit_box.scan_qr('something%s' % basic_user['address'])
        if not send_tr.element_by_text_part('Invalid address').is_element_displayed(10):
            self.driver.fail('No error is shown at attempt to scan invalid address')
        wallet.ok_button.click()

        send_tr.just_fyi('Scan code, add it to favorites and recheck that it is preserved')
        send_tr.scan_qr_code_button.click()
        wallet.enter_qr_edit_box.scan_qr(basic_user['address'])
        send_tr.add_to_favorites(basic_add_to_fav_name)
        send_tr.element_by_translation_id("favourites").scroll_and_click()
        for name in (recent_add_to_fav_name, basic_add_to_fav_name):
            wallet.element_by_text(name).scroll_to_element()

        send_tr.element_by_text(recent_add_to_fav_name).scroll_and_click()
        if str(send_tr.enter_recipient_address_text.text).lower() != send_tr.get_formatted_recipient_address(
                recent_add_to_fav_address):
            self.errors.append('Recent address that was added to favourites was not resolved correctly')

        send_tr.just_fyi('Set contact')
        send_tr.chose_recipient_button.click()
        send_tr.element_by_translation_id("contacts").scroll_and_click()
        send_tr.element_by_text(nickname).scroll_and_click()
        send_tr.recipient_done.click()
        if send_tr.enter_recipient_address_text.text != send_tr.get_formatted_recipient_address(ens_status['address']):
            self.errors.append('ENS from contact is not resolved as recipient')

        send_tr.just_fyi('Set different ENS options')
        send_tr.set_recipient_address(ens_other['ens'])
        if send_tr.enter_recipient_address_text.text != send_tr.get_formatted_recipient_address(ens_other['address']):
            self.errors.append('ENS address on another domain is not resolved as recipient')
        send_tr.set_recipient_address('%s.stateofus.eth' % ens_status['ens'])
        if send_tr.enter_recipient_address_text.text != send_tr.get_formatted_recipient_address(ens_status['address']):
            self.errors.append('ENS address on stateofus.eth is not resolved as recipient')

        send_tr.just_fyi('Check search and set address from search')
        send_tr.chose_recipient_button.click()
        send_tr.search_by_keyword(ens_status['ens'][:2])
        if not send_tr.element_by_text('@' + ens_status['ens']).is_element_displayed():
            self.errors.append('ENS address from contacts is not shown in search')
        send_tr.cancel_button.click()
        send_tr.search_by_keyword('my')
        for name in (nickname, account_name, recent_add_to_fav_name, basic_add_to_fav_name):
            if not send_tr.element_by_text(name).is_element_displayed():
                self.errors.append('%s is not shown in search when searching by namepart' % name)
        send_tr.element_by_text(basic_add_to_fav_name).click()
        if send_tr.enter_recipient_address_text.text != send_tr.get_formatted_recipient_address(
                '0x' + basic_user['address']):
            self.errors.append('QR scanned address that was added to favourites was not resolved correctly')
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="two_1")
@marks.critical
class TestKeycardTxOneDeviceMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.user = transaction_senders['ETH_STT_ADI_1']
        self.address = '0x%s' % transaction_senders['ETH_7']['address']
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])

        self.home = self.sign_in.recover_access(passphrase=self.user['passphrase'], keycard=True)
        self.wallet = self.home.wallet_button.click()
        self.assets = ('ETH', 'YEENUS', 'STT')
        [self.wallet.wait_balance_is_changed(asset) for asset in self.assets]
        self.initial_balances = dict()
        for asset in self.assets:
            self.initial_balances[asset] = self.wallet.get_asset_amount_by_name(asset)

    @marks.testrail_id(700767)
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
    def test_keycard_relogin_after_restore(self):
        self.sign_in.just_fyi('Check that username and public key match expected')
        public_key, default_username = self.sign_in.get_public_key()
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
    def test_keycard_send_tx_sign_message_request_stt_testdapp(self):
        self.home.home_button.double_click()
        status_test_dapp = self.home.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()

        self.wallet.just_fyi("Requesting STT in dapp")
        status_test_dapp.assets_button.click()
        send_tx = status_test_dapp.request_stt_button.click()
        send_tx.sign_transaction(keycard=True)

        send_tx = self.home.get_send_transaction_view()
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
        keycard.pair_code_input.send_keys(pair_code)
        self.sign_in.pair_to_this_device_button.click()
        keycard.enter_default_pin()
        self.sign_in.maybe_later_button.click_until_presence_of_element(self.sign_in.start_button)
        self.sign_in.start_button.click_until_absense_of_element(self.sign_in.start_button)
        self.sign_in.home_button.wait_for_visibility_of_element(30)

        self.sign_in.just_fyi("Check balance will be restored after going back online")
        self.sign_in.toggle_airplane_mode()
        wallet = self.home.wallet_button.click()
        [wallet.wait_balance_is_changed(asset) for asset in ("ETH", "STT")]

        self.wallet.just_fyi("Checking whole tx history after backing from offline")
        self.wallet.accounts_status_account.click()
        address = user['address']
        eth_txs = self.network_api.get_transactions(address)
        token_txs = self.network_api.get_token_transactions(address)
        expected_txs_list = get_merged_txs_list(eth_txs, token_txs)
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

    @marks.testrail_id(5689)
    def test_keycard_create_account_unlock_same_seed(self):
        self.sign_in.driver.reset()
        self.sign_in.just_fyi("Create keycard account and save seed phrase")
        self.sign_in.accept_tos_checkbox.enable()
        self.sign_in.get_started_button.click()
        self.sign_in.generate_key_button.click_until_presence_of_element(self.sign_in.next_button)
        self.sign_in.next_button.click_until_absense_of_element(
            self.sign_in.element_by_translation_id("intro-wizard-title2"))
        keycard_flow = self.sign_in.keycard_storage_button.click()
        keycard_flow.confirm_pin_and_proceed()
        seed_phrase = keycard_flow.backup_seed_phrase()
        self.sign_in.maybe_later_button.wait_for_visibility_of_element(30)
        self.sign_in.maybe_later_button.click_until_presence_of_element(self.sign_in.start_button)
        self.sign_in.start_button.click_until_absense_of_element(self.sign_in.start_button)
        self.sign_in.profile_button.wait_for_visibility_of_element(30)
        wallet_1 = self.sign_in.wallet_button.click()
        wallet_address = wallet_1.get_wallet_address()
        public_key, default_username = self.sign_in.get_public_key()
        profile_1 = self.sign_in.get_profile_view()
        profile_1.logout()

        profile_1.just_fyi('Check that can re-login with keycard account after account creation')
        self.sign_in.multi_account_on_login_button.wait_for_visibility_of_element(5)
        self.sign_in.multi_account_on_login_button.click()
        if not keycard_flow.element_by_text_part(default_username).is_element_displayed():
            self.errors.append("%s is not found on keycard login screen!" % default_username)
        keycard_flow.enter_default_pin()
        if not self.sign_in.home_button.is_element_displayed(10):
            self.errors.append('Keycard user is not logged in')

        self.sign_in.just_fyi('Unlock keycard multiaccount at attempt to restore same multiaccount from seed')
        self.sign_in.profile_button.click()
        profile_1.logout()
        self.sign_in.access_key_button.click()
        self.sign_in.enter_seed_phrase_button.click()
        self.sign_in.seedphrase_input.click()
        self.sign_in.seedphrase_input.send_keys(seed_phrase)
        self.sign_in.next_button.click()
        self.sign_in.element_by_translation_id("unlock", uppercase=True).click()
        keycard_flow.enter_default_pin()
        device_1_home = self.sign_in.home_button.click()
        device_1_home.plus_button.click()
        if not device_1_home.start_new_chat_button.is_element_displayed():
            self.errors.append("Can't proceed using account after it's re-recover twice.")

        self.sign_in.just_fyi("Restore same multiaccount from backed up seed phrase on another device")
        self.sign_in.driver.reset()
        self.sign_in.recover_access(seed_phrase)

        self.sign_in.just_fyi("Check username and wallet address on restored account")
        wallet_2 = self.sign_in.wallet_button.click()
        wallet_address_2 = wallet_2.get_wallet_address()
        wallet_2.wallet_button.double_click()
        if wallet_address != wallet_address_2:
            self.errors.append('Wallet address on restored multiaccount is not equal to created keycard multiaccount')
        public_key_2, default_username_2 = self.sign_in.get_public_key()
        if public_key != public_key_2:
            self.errors.append('Public key on restored multiaccount is not equal to created keycard multiaccount')
        if default_username_2 != default_username:
            self.errors.append('Username on restored multiaccount is not equal to created keycard multiaccount')
        self.errors.verify_no_errors()
