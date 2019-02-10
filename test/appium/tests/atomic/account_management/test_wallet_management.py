import pytest

from tests import marks, camera_access_error_text
from tests.base_test_case import SingleDeviceTestCase
from tests.users import wallet_users
from views.sign_in_view import SignInView


@marks.all
@marks.account
class TestWalletManagement(SingleDeviceTestCase):

    @marks.testrail_id(5335)
    @marks.critical
    def test_wallet_set_up(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        text = 'Simple and secure cryptocurrency wallet'
        if not wallet.element_by_text(text).is_element_displayed():
            self.errors.append("'%s' is not displayed" % text)
        wallet.set_up_button.click()
        texts = ['Super-safe transactions', 'You should see these three words before signing each transaction',
                 'If you see a different combo, cancel the transaction and logout.']
        for text in texts:
            if not wallet.element_by_text_part(text).is_element_displayed():
                self.errors.append("'%s' text is not displayed" % text)
        phrase_length = len(wallet.sign_in_phrase.list)
        if phrase_length != 3:
            self.errors.append('Transaction phrase length is %s' % phrase_length)
        wallet.done_button.click()
        for text in ['Remember this!', "You'll need to recognize this to ensure your "
                                       "transactions are safe. This combo is not stored in your account."]:
            if not wallet.element_by_text(text).is_element_displayed():
                self.errors.append("'%s' text is not displayed" % text)
        wallet.yes_button.click()
        for element in [wallet.send_transaction_button, wallet.receive_transaction_button,
                        wallet.transaction_history_button]:
            if not element.is_element_displayed():
                self.errors.append('%s button is not shown after wallet setup' % element.name)
        self.verify_no_errors()

    @marks.testrail_id(5384)
    @marks.high
    def test_open_transaction_on_etherscan(self):
        user = wallet_users['A']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(user['passphrase'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        transactions_view = wallet_view.transaction_history_button.click()
        transaction_details = transactions_view.transactions_table.transaction_by_index(0).click()
        transaction_hash = transaction_details.get_transaction_hash()
        transaction_details.options_button.click()
        transaction_details.open_transaction_on_etherscan_button.click()
        base_web_view = wallet_view.get_base_web_view()
        base_web_view.open_in_webview()
        base_web_view.find_text_part(transaction_hash)

    @marks.testrail_id(5427)
    @marks.medium
    def test_copy_transaction_hash(self):
        user = wallet_users['A']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(user['passphrase'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        transactions_view = wallet_view.transaction_history_button.click()
        transaction_details = transactions_view.transactions_table.transaction_by_index(0).click()
        transaction_hash = transaction_details.get_transaction_hash()
        transaction_details.options_button.click()
        transaction_details.copy_transaction_hash_button.click()
        transaction_details.get_back_to_home_view()
        wallet_view.home_button.click()
        public_chat = home_view.join_public_chat('testchat')
        public_chat.chat_message_input.paste_text_from_clipboard()
        if public_chat.chat_message_input.text != transaction_hash:
            pytest.fail('Transaction hash was not copied')

    @marks.testrail_id(5341)
    @marks.critical
    def test_manage_assets(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        wallet.options_button.click()
        wallet.manage_assets_button.click()
        select_asset = 'MDS'
        deselect_asset = 'STT'
        wallet.asset_checkbox_by_name(select_asset).click()
        wallet.asset_checkbox_by_name(deselect_asset).click()
        wallet.done_button.click()
        wallet.asset_by_name(select_asset).scroll_to_element()
        if not wallet.asset_by_name(select_asset).is_element_displayed():
            self.errors.append('%s asset is not shown in wallet' % select_asset)
        if wallet.asset_by_name(deselect_asset).is_element_displayed():
            self.errors.append('%s asset is shown in wallet but was deselected' % deselect_asset)
        self.verify_no_errors()

    @marks.testrail_id(5358)
    @marks.critical
    def test_backup_recovery_phrase_warning_from_wallet(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        if wallet.backup_recovery_phrase.is_element_present():
            pytest.fail("'Backup your Recovery phrase' option is shown on Wallet for an account with no funds")
        wallet.receive_transaction_button.click()
        address = wallet.address_text.text[2:]
        wallet.get_back_to_home_view()
        home = wallet.home_button.click()
        self.network_api.get_donate(address)
        home.wallet_button.click()
        if not wallet.backup_recovery_phrase.is_element_present():
            pytest.fail("'Backup your Recovery phrase' option is not shown on Wallet for an account with funds")
        profile = wallet.get_profile_view()
        profile.backup_recovery_phrase()

    @marks.testrail_id(5440)
    @marks.medium
    def test_no_collectibles_to_send_from_wallet(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        profile = sign_in.profile_button.click()
        profile.switch_network('Mainnet with upstream RPC')
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        wallet.options_button.click()
        wallet.manage_assets_button.click()
        asset_name = 'CryptoKitties'
        wallet.asset_checkbox_by_name(asset_name).click()
        wallet.done_button.click()
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.select_asset_button.click()
        if send_transaction.asset_by_name(asset_name).is_element_displayed():
            pytest.fail('Collectibles can be sent from wallet')

    @marks.testrail_id(5467)
    @marks.medium
    def test_deny_camera_access_scanning_wallet_adders(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.chose_recipient_button.click()
        send_transaction.scan_qr_code_button.click()
        send_transaction.deny_button.click()
        send_transaction.element_by_text(camera_access_error_text).wait_for_visibility_of_element(3)
        send_transaction.ok_button.click()
        send_transaction.chose_recipient_button.click()
        send_transaction.scan_qr_code_button.click()
        send_transaction.deny_button.wait_for_visibility_of_element(2)

    @marks.testrail_id(5435)
    @marks.medium
    def test_filter_transactions_history(self):
        user = wallet_users['C']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(passphrase=user['passphrase'])
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()

        transaction_history = wallet_view.transaction_history_button.click()
        transaction_history.filters_button.click()
        for filter_name in 'Outgoing', 'Pending', 'Failed':
            transaction_history.filter_checkbox(filter_name).click()
        wallet_view.done_button.click()
        for i in range(transaction_history.transactions_table.get_transactions_number()):
            details = transaction_history.transactions_table.transaction_by_index(i).click()
            if details.get_recipient_address() != '0x' + user['address'] \
                    or details.element_by_text('Failed').is_element_displayed():
                pytest.fail('Incoming transactions are not filtered')
            details.back_button.click()

        transaction_history.filters_button.click()
        for filter_name in 'Outgoing', 'Incoming':
            transaction_history.filter_checkbox(filter_name).click()
        wallet_view.done_button.click()
        for i in range(transaction_history.transactions_table.get_transactions_number()):
            details = transaction_history.transactions_table.transaction_by_index(i).click()
            if details.get_sender_address() != '0x' + user['address'] \
                    or details.element_by_text('Failed').is_element_displayed():
                pytest.fail('Outgoing transactions are not filtered')
            details.back_button.click()

        transaction_history.filters_button.click()
        for filter_name in 'Outgoing', 'Failed':
            transaction_history.filter_checkbox(filter_name).click()
        wallet_view.done_button.click()
        for i in range(transaction_history.transactions_table.get_transactions_number()):
            details = transaction_history.transactions_table.transaction_by_index(i).click()
            if not details.element_by_text('Failed').is_element_displayed():
                pytest.fail('Failed transactions are not filtered')
            details.back_button.click()
        self.verify_no_errors()

    @marks.testrail_id(5381)
    @marks.high
    def test_user_can_see_all_own_assets_after_account_recovering(self):
        passphrase = wallet_users['E']['passphrase']
        signin_view = SignInView(self.driver)
        home_view = signin_view.recover_access(passphrase=passphrase)
        profile = home_view.profile_button.click()
        profile.switch_network('Rinkeby with upstream RPC')
        profile = home_view.profile_button.click()
        wallet_view = profile.wallet_button.click()
        wallet_view.set_up_wallet()
        if wallet_view.collectible_amount_by_name('kdo') != '1':
            self.driver.fail('User collectibles amount does not match!')
