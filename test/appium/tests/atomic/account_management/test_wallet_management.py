import pytest
import random
import string

from tests import marks, camera_access_error_text, common_password
from tests.base_test_case import SingleDeviceTestCase
from tests.users import wallet_users, transaction_senders, basic_user, ens_user, ens_user_other_domain
from views.sign_in_view import SignInView


@marks.all
@marks.account
class TestWalletManagement(SingleDeviceTestCase):

    @marks.testrail_id(5335)
    @marks.high
    def test_wallet_set_up(self):
        sign_in = SignInView(self.driver)
        sign_in.recover_access(transaction_senders['A']['passphrase'])
        wallet = sign_in.wallet_button.click()
        texts = ['This is your signing phrase', 'You should see these 3 words before signing each transaction',
                 'If you see a different combo, cancel the transaction and logout.']
        for text in texts:
            if not wallet.element_by_text_part(text).is_element_displayed():
                self.errors.append("'%s' text is not displayed" % text)
        phrase = wallet.sign_in_phrase.list
        if len(phrase) != 3:
            self.errors.append('Transaction phrase length is %s' % len(phrase))
        wallet.remind_me_later_button.click()
        wallet.accounts_status_account.click()
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.amount_edit_box.set_value('0')
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(basic_user['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction_button.click()
        for text in texts:
            if not wallet.element_by_text_part(text).is_element_displayed():
                self.errors.append("'%s' text is not displayed" % text)
        phrase_1 = wallet.sign_in_phrase.list
        if phrase_1 != phrase:
            self.errors.append("Transaction phrase '%s' doesn't match expected '%s'" % (phrase_1, phrase))
        wallet.ok_got_it_button.click()
        wallet.back_button.click(times_to_click=2)
        wallet.home_button.click()
        wallet.wallet_button.click()
        for text in texts:
            if wallet.element_by_text_part(text).is_element_displayed():
                self.errors.append('Signing phrase pop up appears after wallet set up')
                break
        self.verify_no_errors()

    @marks.testrail_id(5384)
    @marks.critical
    def test_open_transaction_on_etherscan(self):
        user = wallet_users['A']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(user['passphrase'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
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
        wallet_view.accounts_status_account.click()
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
            self.driver.fail('Transaction hash was not copied')

    @marks.testrail_id(5341)
    @marks.critical
    def test_manage_assets(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        asset = "MDS"
        wallet.select_asset(asset)
        wallet.asset_by_name(asset).scroll_to_element()
        if not wallet.asset_by_name(asset).is_element_displayed():
            self.errors.append('%s asset is not shown in wallet' % asset)
        wallet.select_asset(asset)
        if wallet.asset_by_name(asset).is_element_displayed():
            self.errors.append('%s asset is shown in wallet but was deselected' % asset)
        self.verify_no_errors()

    @marks.testrail_id(5358)
    @marks.medium
    def test_backup_recovery_phrase_warning_from_wallet(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        if wallet.backup_recovery_phrase_warning_text.is_element_present():
            self.driver.fail("'Back up your seed phrase' warning is shown on Wallet while no funds are present")
        address = wallet.get_wallet_address()
        self.network_api.get_donate(address[2:])
        wallet.back_button.click()
        if not wallet.backup_recovery_phrase_warning_text.is_element_present(30):
            self.driver.fail("'Back up your seed phrase' warning is not shown on Wallet with funds")
        wallet.backup_recovery_phrase_warning_text.click()
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
        asset_name = 'CryptoKitties'
        wallet.select_asset(asset_name)
        wallet.accounts_status_account.click()
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.select_asset_button.click()
        if send_transaction.asset_by_name(asset_name).is_element_displayed():
            self.driver.fail('Collectibles can be sent from wallet')

    @marks.testrail_id(5467)
    @marks.medium
    def test_deny_camera_access_scanning_wallet_adders(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        wallet.accounts_status_account.click()
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
    @marks.skip
    # TODO: e2e blocker: 9225 (should be updated and enabled)
    def test_filter_transactions_history(self):
        user = wallet_users['C']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(passphrase=user['passphrase'])
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
        transaction_history = wallet_view.transaction_history_button.click()
        transaction_history.filters_button.click()
        for filter_name in 'Outgoing', 'Pending', 'Failed':
            transaction_history.filter_checkbox(filter_name).click()
        wallet_view.done_button.click()
        for i in range(transaction_history.transactions_table.get_transactions_number()):
            details = transaction_history.transactions_table.transaction_by_index(i).click()
            if details.get_recipient_address() != '0x' + user['address'] \
                    or details.element_by_text('Failed').is_element_displayed():
                self.driver.fail('Incoming transactions are not filtered')
            details.back_button.click()

        transaction_history.filters_button.click()
        for filter_name in 'Outgoing', 'Incoming':
            transaction_history.filter_checkbox(filter_name).click()
        wallet_view.done_button.click()
        for i in range(transaction_history.transactions_table.get_transactions_number()):
            details = transaction_history.transactions_table.transaction_by_index(i).click()
            if details.get_sender_address() != '0x' + user['address'] \
                    or details.element_by_text('Failed').is_element_displayed():
                self.driver.fail('Outgoing transactions are not filtered')
            details.back_button.click()

        transaction_history.filters_button.click()
        for filter_name in 'Outgoing', 'Failed':
            transaction_history.filter_checkbox(filter_name).click()
        wallet_view.done_button.click()
        for i in range(transaction_history.transactions_table.get_transactions_number()):
            details = transaction_history.transactions_table.transaction_by_index(i).click()
            if not details.element_by_text('Failed').is_element_displayed():
                self.driver.fail('Failed transactions are not filtered')
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
        wallet_view.accounts_status_account.click()
        wallet_view.collectibles_button.click()
        if not wallet_view.element_by_text('KDO').is_element_displayed():
            self.driver.fail('User collectibles token name in not shown')
        if not wallet_view.element_by_text('1').is_element_displayed():
            self.driver.fail('User collectibles amount does not match')

    @marks.testrail_id(5346)
    @marks.high
    def test_collectible_from_wallet_opens_in_browser_view(self):
        passphrase = wallet_users['F']['passphrase']
        signin_view = SignInView(self.driver)
        home_view = signin_view.recover_access(passphrase=passphrase)
        profile = home_view.profile_button.click()
        profile.switch_network('Mainnet with upstream RPC')
        wallet_view = profile.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.accounts_status_account.click()
        wallet_view.collectibles_button.click()
        wallet_view.cryptokitties_in_collectibles_button.click()
        web_view = wallet_view.view_in_cryptokitties_button.click()
        web_view.element_by_text('cryptokitties.co').click()
        cryptokitty_link = 'https://www.cryptokitties.co/kitty/1338226'
        if not web_view.element_by_text(cryptokitty_link).is_element_displayed():
            self.driver.fail('Cryptokitty detail page not opened')

    @marks.testrail_id(6208)
    @marks.high
    def test_add_custom_token(self):
        contract_address = '0x25B1bD06fBfC2CbDbFc174e10f1B78b1c91cc77B'
        name = 'SNTMiniMeToken'
        symbol = 'SNT'
        decimals = '18'
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.multiaccount_more_options.click()
        wallet_view.manage_assets_button.click()
        token_view = wallet_view.add_custom_token_button.click()
        token_view.contract_address_input.send_keys(contract_address)
        token_view.progress_bar.wait_for_invisibility_of_element(30)
        if token_view.name_input.text != name:
            self.errors.append('Name for custom token was not set')
        if token_view.symbol_input.text != symbol:
            self.errors.append('Symbol for custom token was not set')
        if token_view.decimals_input.text != decimals:
            self.errors.append('Decimals for custom token was not set')
        token_view.add_button.click()
        token_view.back_button.click()
        if not wallet_view.asset_by_name(symbol).is_element_displayed():
            self.errors.append('Custom token is not shown on Wallet view')
        wallet_view.accounts_status_account.click()
        send_transaction = wallet_view.send_transaction_button.click()
        token_element = send_transaction.asset_by_name(symbol)
        send_transaction.select_asset_button.click_until_presence_of_element(token_element)
        if not token_element.is_element_displayed():
            self.errors.append('Custom token is not shown on Send Transaction view')
        self.verify_no_errors()

    @marks.testrail_id(6224)
    @marks.critical
    def test_add_account_to_multiaccount_instance(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.add_account_button.click()
        wallet_view.add_an_account_button.click()
        wallet_view.generate_new_account_button.click()
        wallet_view.generate_account_button.click()
        if wallet_view.element_by_text('Account added').is_element_displayed():
            self.driver.fail('Account is added without password')
        wallet_view.enter_your_password_input.send_keys('000000')
        wallet_view.generate_account_button.click()
        if not wallet_view.element_by_text_part('Password seems to be incorrect').is_element_displayed():
            self.driver.fail("Incorrect password validation is not performed")
        wallet_view.enter_your_password_input.clear()
        wallet_view.enter_your_password_input.send_keys(common_password)
        wallet_view.generate_account_button.click()
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        wallet_view.account_name_input.send_keys(account_name)
        wallet_view.account_color_button.select_color_by_position(1)
        wallet_view.finish_button.click()
        account_button = wallet_view.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.driver.fail('Account was not added')
        if not account_button.color_matches('multi_account_color.png'):
            self.driver.fail('Account color does not match expected')

    @marks.testrail_id(5406)
    @marks.critical
    def test_ens_username_recipient(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()

        sign_in.just_fyi('switching to mainnet')
        profile = sign_in.profile_button.click()
        profile.switch_network('Mainnet with upstream RPC')
        wallet = profile.wallet_button.click()

        wallet.just_fyi('checking that "stateofus.eth" name will be resolved as recipient')
        wallet.set_up_wallet()
        wallet.accounts_status_account.click()
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value('%s.stateofus.eth' % ens_user['ens'])
        send_transaction.done_button.click()
        if send_transaction.enter_recipient_address_text.text != ens_user['address']:
            self.errors.append('ENS address on stateofus.eth is not resolved as recipient')

        wallet.just_fyi('checking that ".eth" name will be resolved as recipient')
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(ens_user_other_domain['ens'])
        send_transaction.done_button.click()

        if send_transaction.enter_recipient_address_text.text != ens_user_other_domain['address']:
            self.errors.append('ENS address on another domain is not resolved as recipient')

        self.verify_no_errors()
