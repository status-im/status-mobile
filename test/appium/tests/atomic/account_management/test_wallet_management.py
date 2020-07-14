import random
import string

from tests import marks, camera_access_error_text, common_password
from tests.base_test_case import SingleDeviceTestCase
from tests.users import wallet_users, transaction_senders, basic_user, ens_user
from views.sign_in_view import SignInView
import time


@marks.all
@marks.account
class TestWalletManagement(SingleDeviceTestCase):

    @marks.testrail_id(5335)
    @marks.high
    def test_wallet_set_up(self):
        sign_in = SignInView(self.driver)
        sign_in.recover_access(transaction_senders['A']['passphrase'])
        wallet = sign_in.wallet_button.click()
        texts = ['This is your signing phrase',
                 'You should see these 3 words before signing each transaction',
                 'If you see a different combination, cancel the transaction and sign out']
        wallet.just_fyi('Check tests in set up wallet popup')
        for text in texts:
            if not wallet.element_by_text_part(text).is_element_displayed():
                self.errors.append("'%s' text is not displayed" % text)
        phrase = wallet.sign_in_phrase.list
        if len(phrase) != 3:
            self.errors.append('Transaction phrase length is %s' % len(phrase))

        wallet.just_fyi('Check popup will reappear if tap on "Remind me later"')
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
        self.errors.verify_no_errors()

    @marks.testrail_id(5384)
    @marks.critical
    def test_open_transaction_on_etherscan(self):
        user = wallet_users['D']
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
        user = wallet_users['D']
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
        self.errors.verify_no_errors()

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
        wallet.wait_balance_is_changed()
        if not wallet.backup_recovery_phrase_warning_text.is_element_present(30):
            self.driver.fail("'Back up your seed phrase' warning is not shown on Wallet with funds")
        profile = wallet.get_profile_view()
        wallet.backup_recovery_phrase_warning_text.click_until_presence_of_element(profile.ok_continue_button)
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
        assets = ['CryptoKitties', 'CryptoStrikers']
        for asset in assets:
            wallet.select_asset(asset)
        wallet.accounts_status_account.click()
        wallet.collectibles_button.click()
        for asset in assets:
            if not wallet.element_by_text(asset).is_element_displayed():
                self.errors.append('Assets are not shown in Collectibles after adding')
        wallet.transaction_history_button.click()
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.select_asset_button.click()
        for asset in assets:
            if send_transaction.asset_by_name(asset).is_element_displayed():
                self.errors.append('Collectibles can be sent from wallet')
        self.errors.verify_no_errors()

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
        self.errors.verify_no_errors()

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


    @marks.testrail_id(6224)
    @marks.critical
    def test_add_account_to_multiaccount_instance_generate_new(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.add_account_button.click()
        wallet_view.generate_an_account_button.click()
        wallet_view.add_account_generate_account_button.click()
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        wallet_view.account_name_input.send_keys(account_name)
        wallet_view.account_color_button.select_color_by_position(1)
        if wallet_view.get_account_options_by_name(account_name).is_element_displayed():
            self.driver.fail('Account is added without password')
        wallet_view.enter_your_password_input.send_keys('000000')
        wallet_view.add_account_generate_account_button.click()
        if not wallet_view.element_by_text_part('Password seems to be incorrect').is_element_displayed():
             self.driver.fail("Incorrect password validation is not performed")
        wallet_view.enter_your_password_input.clear()
        wallet_view.enter_your_password_input.send_keys(common_password)
        wallet_view.add_account_generate_account_button.click()
        account_button = wallet_view.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.driver.fail('Account was not added')

        if not account_button.color_matches('multi_account_color.png'):
            self.driver.fail('Account color does not match expected')

    @marks.testrail_id(6244)
    @marks.high
    def test_add_and_delete_watch_only_account_to_multiaccount_instance(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()

        wallet_view.just_fyi('Add watch-only account')
        wallet_view.add_account_button.click()
        wallet_view.add_watch_only_address_button.click()
        wallet_view.enter_address_input.send_keys(basic_user['address'])
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        wallet_view.account_name_input.send_keys(account_name)
        wallet_view.add_account_generate_account_button.click()
        account_button = wallet_view.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.driver.fail('Account was not added')

        wallet_view.just_fyi('Check that overall balance is changed after adding watch-only account')
        for asset in ('ETH', 'ADI', 'STT'):
            wallet_view.wait_balance_is_changed(asset)

        wallet_view.just_fyi('Check individual watch-only account view, settings and receive option')
        wallet_view.get_account_by_name(account_name).click()
        if wallet_view.send_transaction_button.is_element_displayed():
            self.errors.append('Send button is shown on watch-only wallet')
        if not wallet_view.element_by_text('Watch-only').is_element_displayed():
            self.errors.append('No "Watch-only" label is shown on watch-only wallet')
        wallet_view.receive_transaction_button.click()
        if wallet_view.address_text.text[2:] != basic_user['address']:
            self.errors.append('Wrong address %s is shown in "Receive" popup for watch-only account ' % wallet_view.address_text.text)
        wallet_view.close_share_popup()
        wallet_view.get_account_options_by_name(account_name).click()
        wallet_view.account_settings_button.click()
        if not wallet_view.element_by_text('Watch-only').is_element_displayed():
            self.errors.append('"Watch-only" type is not shown in account settings')

        wallet_view.just_fyi('Delete watch-only account')
        wallet_view.delete_account_button.click()
        wallet_view.yes_button.click()
        if account_button.is_element_displayed():
            self.driver.fail('Account was not deleted')
        for asset in ('ETH', 'ADI', 'STT'):
            balance = wallet_view.get_asset_amount_by_name(asset)
            if balance != 0:
                self.errors.append("Balance for %s is not updated after deleting watch-only account" % asset)

        self.errors.verify_no_errors()

    @marks.testrail_id(6271)
    @marks.high
    def test_add_account_to_multiaccount_instance_seed_phrase(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()

        wallet_view.just_fyi('Add account from seed phrase')
        wallet_view.add_account_button.click()
        wallet_view.enter_a_seed_phrase_button.click()
        wallet_view.enter_your_password_input.send_keys(common_password)

        wallet_view.enter_seed_phrase_input.set_value('')
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        wallet_view.account_name_input.send_keys(account_name)
        wallet_view.add_account_generate_account_button.click()
        if wallet_view.get_account_options_by_name(account_name).is_element_displayed():
            self.driver.fail('Account is added without seed phrase')
        wallet_view.enter_seed_phrase_input.set_value(str(wallet_users['C']['passphrase']).upper())
        wallet_view.add_account_generate_account_button.click()

        account_button = wallet_view.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.driver.fail('Account was not added')

        wallet_view.just_fyi('Check that overall balance is changed after adding account')
        for asset in ('ETH', 'ADI'):
            wallet_view.wait_balance_is_changed(asset)

        wallet_view.just_fyi('Check account view and send option')
        wallet_view.get_account_by_name(account_name).click()
        if not wallet_view.send_transaction_button.is_element_displayed():
            self.errors.append('Send button is not shown on account added with seed phrase')
        wallet_view.receive_transaction_button.click()
        if wallet_view.address_text.text[2:] != wallet_users['C']['address']:
            self.errors.append(
                'Wrong address %s is shown in "Receive" popup ' % wallet_view.address_text.text)
        self.errors.verify_no_errors()

    @marks.testrail_id(6272)
    @marks.high
    def test_add_account_to_multiaccount_instance_private_key(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()

        wallet_view.just_fyi('Add account from private key')
        wallet_view.add_account_button.click()
        wallet_view.enter_a_private_key_button.click()
        wallet_view.enter_your_password_input.send_keys(common_password)

        wallet_view.enter_a_private_key_input.set_value(wallet_users['C']['private_key'][0:9])
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        wallet_view.account_name_input.send_keys(account_name)
        wallet_view.add_account_generate_account_button.click()
        if wallet_view.get_account_options_by_name(account_name).is_element_displayed():
            self.driver.fail('Account is added with wrong private key')
        wallet_view.enter_a_private_key_input.set_value(wallet_users['C']['private_key'])
        wallet_view.add_account_generate_account_button.click()

        account_button = wallet_view.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.driver.fail('Account was not added')

        wallet_view.just_fyi('Check that overall balance is changed after adding account')
        for asset in ('ETH', 'ADI'):
            wallet_view.wait_balance_is_changed(asset)

        wallet_view.just_fyi('Check individual account view, receive option')
        wallet_view.get_account_by_name(account_name).click()
        if not wallet_view.send_transaction_button.is_element_displayed():
            self.errors.append('Send button is not shown on account added with private key')
        wallet_view.receive_transaction_button.click()
        if wallet_view.address_text.text[2:] != wallet_users['C']['address']:
            self.errors.append(
                'Wrong address %s is shown in "Receive" popup account ' % wallet_view.address_text.text)
        self.errors.verify_no_errors()


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
        formatted_ens_user_address = send_transaction.get_formatted_recipient_address(ens_user['address'])

        if send_transaction.enter_recipient_address_text.text != formatted_ens_user_address:
            self.errors.append('ENS address on stateofus.eth is not resolved as recipient')

        wallet.just_fyi('checking that ".eth" name will be resolved as recipient')
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(ens_user['ens_another_domain'])
        send_transaction.done_button.click()

        if send_transaction.enter_recipient_address_text.text != formatted_ens_user_address:
            self.errors.append('ENS address on another domain is not resolved as recipient')

        wallet.just_fyi('checking that "stateofus.eth" name without domain will be resolved as recipient')
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(ens_user['ens'])
        send_transaction.done_button.click()

        if send_transaction.enter_recipient_address_text.text != formatted_ens_user_address:
            self.errors.append('ENS address "stateofus.eth" without domain is not resolved as recipient')

        self.errors.verify_no_errors()

    @marks.testrail_id(6269)
    @marks.medium
    def test_search_asset_and_currency(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        profile = home.profile_button.click()
        profile.switch_network('Mainnet with upstream RPC')
        search_list_assets = {
            'ad': ['AdEx', 'Open Trading Network', 'TrueCAD'],
            'zs': ['ZSC']
        }
        wallet = home.wallet_button.click()

        home.just_fyi('Searching for asset by name and symbol')
        wallet.set_up_wallet()
        wallet.multiaccount_more_options.click()
        wallet.manage_assets_button.click()
        for keyword in search_list_assets:
            home.search_by_keyword(keyword)
            # TODO: remove time sleep after 10957 is closed
            time.sleep(5)
            if keyword == 'ad':
                search_elements = wallet.all_assets_full_names.find_elements()
            else:
                search_elements = wallet.all_assets_symbols.find_elements()
            if not search_elements:
                self.errors.append('No search results after searching by %s keyword' % keyword)
            search_results = [element.text for element in search_elements]
            if search_results != search_list_assets[keyword]:
                self.errors.append("'%s' is shown on the home screen after searching by '%s' keyword" %
                                                                    (', '.join(search_results), keyword))
            home.cancel_button.click()
        wallet.back_button.click()

        home.just_fyi('Searching for currency')
        search_list_currencies = {
            'aF': ['Afghanistan Afghani (AFN)', 'South Africa Rand (ZAR)'],
            'bolívi': ['Bolivia Bolíviano (BOB)']
        }
        wallet.multiaccount_more_options.click_until_presence_of_element(wallet.set_currency_button)
        wallet.set_currency_button.click()
        for keyword in search_list_currencies:
            home.search_by_keyword(keyword)
            search_elements = wallet.currency_item_text.find_elements()
            if not search_elements:
                self.errors.append('No search results after searching by %s keyword' % keyword)
            search_results = [element.text for element in search_elements]
            if search_results != search_list_currencies[keyword]:
                 self.errors.append("'%s' is shown on the home screen after searching by '%s' keyword" %
                                                                     (', '.join(search_results), keyword))
            home.cancel_button.click()

        self.errors.verify_no_errors()
