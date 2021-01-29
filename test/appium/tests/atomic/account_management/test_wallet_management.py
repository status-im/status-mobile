import random
import string

from tests import marks, common_password
from tests.base_test_case import SingleDeviceTestCase
from tests.users import wallet_users, transaction_senders, basic_user
from views.sign_in_view import SignInView


class TestWalletManagement(SingleDeviceTestCase):

    @marks.testrail_id(5335)
    @marks.high
    def test_wallet_set_up(self):
        sign_in = SignInView(self.driver)
        sign_in.recover_access(transaction_senders['A']['passphrase'])
        wallet = sign_in.wallet_button.click()
        texts = list(map(sign_in.get_translation_by_key,
                         ["this-is-you-signing", "three-words-description", "three-words-description-2"]))
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
        send_transaction.set_recipient_address('0x%s' % basic_user['address'])
        send_transaction.next_button.click_until_presence_of_element(send_transaction.sign_transaction_button)
        send_transaction.sign_transaction_button.click()
        for text in texts:
            if not wallet.element_by_text_part(text).is_element_displayed():
                self.errors.append("'%s' text is not displayed" % text)
        phrase_1 = wallet.sign_in_phrase.list
        if phrase_1 != phrase:
            self.errors.append("Transaction phrase '%s' doesn't match expected '%s'" % (phrase_1, phrase))
        wallet.ok_got_it_button.click()
        wallet.cancel_button.click()
        wallet.home_button.click()
        wallet.wallet_button.click()
        for text in texts:
            if wallet.element_by_text_part(text).is_element_displayed():
                self.errors.append('Signing phrase pop up appears after wallet set up')
                break
        self.errors.verify_no_errors()

    @marks.testrail_id(5384)
    @marks.critical
    def test_open_transaction_on_etherscan_copy_tx_hash(self):
        user = wallet_users['D']
        home = SignInView(self.driver).recover_access(user['passphrase'])
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        wallet.accounts_status_account.click()

        wallet.just_fyi("Open transaction on etherscan")
        transactions = wallet.transaction_history_button.click()
        transaction_details = transactions.transactions_table.transaction_by_index(0).click()
        transaction_hash = transaction_details.get_transaction_hash()
        transaction_details.options_button.click()
        transaction_details.open_transaction_on_etherscan_button.click()
        web_page = wallet.get_base_web_view()
        web_page.open_in_webview()
        web_page.element_by_text_part(transaction_hash).wait_for_visibility_of_element(30)

        wallet.just_fyi("Copy transaction hash")
        web_page.click_system_back_button()
        transaction_details.options_button.click()
        transaction_details.copy_transaction_hash_button.click()
        wallet.home_button.click()
        public_chat = home.join_public_chat('testchat')
        public_chat.chat_message_input.paste_text_from_clipboard()
        if public_chat.chat_message_input.text != transaction_hash:
            self.driver.fail('Transaction hash was not copied')

    @marks.testrail_id(5346)
    @marks.high
    def test_collectible_from_wallet(self):
        passphrase = wallet_users['F']['passphrase']
        home = SignInView(self.driver).recover_access(passphrase=passphrase)
        profile = home.profile_button.click()
        profile.switch_network()
        wallet = profile.wallet_button.click()
        wallet.set_up_wallet()
        wallet.scan_tokens()
        wallet.accounts_status_account.click()
        wallet.collectibles_button.click()

        wallet.just_fyi('Check collectibles amount in wallet')
        wallet.cryptokitties_in_collectibles_number.wait_for_visibility_of_element(30)
        if wallet.cryptokitties_in_collectibles_number.text != '1':
            self.errors.append(
                'Wrong number is shown on CK assets: %s' % wallet.cryptokitties_in_collectibles_number.text)

        wallet.just_fyi('Check that collectibles are not shown when sending assets from wallet')
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.select_asset_button.click()
        if send_transaction.asset_by_name("CryptoKitties").is_element_displayed():
            self.errors.append('Collectibles can be sent from wallet')
        wallet.back_button.click(2)

        wallet.just_fyi('Check "Open in OpenSea"')
        wallet.element_by_translation_id("check-on-opensea").click()
        wallet.element_by_text('Sign In').click()
        if not wallet.allow_button.is_element_displayed(40):
            self.errors.append('Can not sign in in OpenSea dapp')
        self.errors.verify_no_errors()

    @marks.testrail_id(5341)
    @marks.critical
    def test_manage_assets(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        asset = "MDS"

        sign_in.just_fyi("Enabling 0 asset on wallet and check it is shown")
        wallet.select_asset(asset)
        wallet.asset_by_name(asset).scroll_to_element()
        if not wallet.asset_by_name(asset).is_element_displayed():
            self.errors.append('%s asset is not shown in wallet' % asset)

        sign_in.just_fyi("Check that 0 asset is not disappearing after relogin")
        profile = wallet.profile_button.click()
        profile.relogin()
        sign_in.wallet_button.click()
        if not wallet.asset_by_name(asset).is_element_displayed():
            self.errors.append('%s asset is not shown in wallet after relogin' % asset)

        sign_in.just_fyi("Deselecting asset")
        wallet.select_asset(asset)
        if wallet.asset_by_name(asset).is_element_displayed():
            self.errors.append('%s asset is shown in wallet but was deselected' % asset)
        self.errors.verify_no_errors()

    @marks.testrail_id(5358)
    @marks.medium
    @marks.transaction
    def test_backup_recovery_phrase_warning_from_wallet(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()
        if wallet.backup_recovery_phrase_warning_text.is_element_present():
            self.driver.fail("'Back up your seed phrase' warning is shown on Wallet while no funds are present")
        address = wallet.get_wallet_address()
        self.network_api.get_donate(address[2:], external_faucet=True, wait_time=200)
        wallet.back_button.click()
        wallet.wait_balance_is_changed()
        if not wallet.backup_recovery_phrase_warning_text.is_element_present(30):
            self.driver.fail("'Back up your seed phrase' warning is not shown on Wallet with funds")
        profile = wallet.get_profile_view()
        wallet.backup_recovery_phrase_warning_text.click()
        profile.backup_recovery_phrase()

    @marks.testrail_id(5381)
    @marks.high
    def test_user_can_see_all_own_assets_after_account_recovering(self):
        home = SignInView(self.driver).recover_access(wallet_users['E']['passphrase'])
        profile = home.profile_button.click()
        profile.switch_network('Rinkeby with upstream RPC')
        profile = home.profile_button.click()
        wallet = profile.wallet_button.click()
        wallet.set_up_wallet()
        wallet.scan_tokens()
        wallet.accounts_status_account.click()
        wallet.collectibles_button.click()
        if not wallet.element_by_text('KDO').is_element_displayed():
            self.driver.fail('User collectibles token name in not shown')
        if not wallet.element_by_text('1').is_element_displayed():
            self.driver.fail('User collectibles amount does not match')

    @marks.testrail_id(6224)
    @marks.critical
    def test_add_account_to_multiaccount_instance_generate_new(self):
        home = SignInView(self.driver).create_user()
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        wallet.add_account_button.click()
        wallet.generate_an_account_button.click()
        wallet.add_account_generate_account_button.click()
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        wallet.account_name_input.send_keys(account_name)
        wallet.account_color_button.select_color_by_position(1)
        if wallet.get_account_by_name(account_name).is_element_displayed():
            self.driver.fail('Account is added without password')
        wallet.enter_your_password_input.send_keys('000000')
        wallet.add_account_generate_account_button.click()
        if not wallet.element_by_text_part('Password seems to be incorrect').is_element_displayed():
             self.driver.fail("Incorrect password validation is not performed")
        wallet.enter_your_password_input.clear()
        wallet.enter_your_password_input.send_keys(common_password)
        wallet.add_account_generate_account_button.click()
        account_button = wallet.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.driver.fail('Account was not added')
        if not account_button.color_matches('multi_account_color.png'):
            self.driver.fail('Account color does not match expected')

    @marks.testrail_id(6244)
    @marks.high
    def test_add_and_delete_watch_only_account_to_multiaccount_instance(self):
        home = SignInView(self.driver).create_user()
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()

        wallet.just_fyi('Add watch-only account')
        wallet.add_account_button.click()
        wallet.add_watch_only_address_button.click()
        wallet.enter_address_input.send_keys(basic_user['address'])
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        wallet.account_name_input.send_keys(account_name)
        wallet.add_account_generate_account_button.click()
        account_button = wallet.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.driver.fail('Account was not added')

        wallet.just_fyi('Check that overall balance is changed after adding watch-only account')
        for asset in ('ETH', 'ADI', 'STT'):
            wallet.wait_balance_is_changed(asset)

        wallet.just_fyi('Check individual watch-only account view, settings and receive option')
        wallet.get_account_by_name(account_name).click()
        if wallet.send_transaction_button.is_element_displayed():
            self.errors.append('Send button is shown on watch-only wallet')
        if not wallet.element_by_text('Watch-only').is_element_displayed():
            self.errors.append('No "Watch-only" label is shown on watch-only wallet')
        wallet.receive_transaction_button.click_until_presence_of_element(wallet.address_text)
        if wallet.address_text.text[2:] != basic_user['address']:
            self.errors.append('Wrong address %s is shown in "Receive" popup for watch-only account ' % wallet.address_text.text)
        wallet.close_share_popup()
        wallet.get_account_options_by_name(account_name).click()
        wallet.account_settings_button.click()
        if not wallet.element_by_text('Watch-only').is_element_displayed():
            self.errors.append('"Watch-only" type is not shown in account settings')

        wallet.just_fyi('Delete watch-only account')
        wallet.delete_account_button.click()
        wallet.yes_button.click()
        if account_button.is_element_displayed():
            self.driver.fail('Account was not deleted')
        for asset in ('ETH', 'ADI', 'STT'):
            wallet.wait_balance_is_equal_expected_amount(asset, 0)

        self.errors.verify_no_errors()

    @marks.testrail_id(6272)
    @marks.high
    def test_add_account_to_wallet_private_key_and_seed_phrase(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        wallet.set_up_wallet()

        wallet.just_fyi('Add account from private key')
        wallet.add_account_button.click()
        wallet.enter_a_private_key_button.click()
        wallet.enter_your_password_input.send_keys(common_password)
        wallet.enter_a_private_key_input.set_value(wallet_users['C']['private_key'][0:9])
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        wallet.account_name_input.send_keys(account_name)
        wallet.add_account_generate_account_button.click()
        if wallet.get_account_by_name(account_name).is_element_displayed():
            self.driver.fail('Account is added with wrong private key')
        wallet.enter_a_private_key_input.set_value(wallet_users['C']['private_key'])
        wallet.add_account_generate_account_button.click()
        account_button = wallet.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.driver.fail('Account from private key was not added')

        wallet.just_fyi('Check that overall balance is changed after adding account from private key')
        for asset in ('ETH', 'ADI', 'LXS', 'STT'):
            wallet.wait_balance_is_changed(asset)
        initial_STT = wallet.get_asset_amount_by_name('STT')

        wallet.just_fyi('Check individual account view (imported from private key), receive option')
        wallet.get_account_by_name(account_name).scroll_and_click(direction="up")
        if not wallet.send_transaction_button.is_element_displayed():
            self.errors.append('Send button is not shown on account added with private key')
        wallet.receive_transaction_button.click()
        if wallet.address_text.text[2:] != wallet_users['C']['address']:
            self.errors.append('Wrong address %s is shown in "Receive" popup account ' % wallet.address_text.text)
        wallet.wallet_button.double_click()

        wallet.just_fyi('Adding account from seed phrase')
        wallet.add_account_button.scroll_to_element(direction='left')
        wallet.add_account_button.click()
        wallet.enter_a_seed_phrase_button.click()
        wallet.enter_your_password_input.send_keys(common_password)
        wallet.enter_seed_phrase_input.set_value('')
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        wallet.account_name_input.send_keys(account_name)
        wallet.add_account_generate_account_button.click()
        if wallet.get_account_by_name(account_name).is_element_displayed():
            self.driver.fail('Account is added without seed phrase')
        wallet.enter_seed_phrase_input.set_value(str(wallet_users['C']['passphrase']).upper())
        wallet.add_account_generate_account_button.click()
        if wallet.get_account_by_name(account_name).is_element_displayed():
            self.driver.fail('Same account was added twice')
        wallet.enter_seed_phrase_input.set_value(str(wallet_users['D']['passphrase']).upper())
        wallet.add_account_generate_account_button.click()
        account_button = wallet.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.driver.fail('Account was not added')

        wallet.just_fyi('Check that overall balance is changed after adding account from seed phrase')
        wallet.wait_balance_is_changed('STT', initial_balance=initial_STT)
        wallet.wait_balance_is_changed('MDS')

        wallet.just_fyi('Check account view and send option (imported from seed phrase)')
        wallet.get_account_by_name(account_name).scroll_and_click(direction="up")
        if not wallet.send_transaction_button.is_element_displayed():
            self.errors.append('Send button is not shown on account added with seed phrase')
        wallet.receive_transaction_button.click()
        if wallet.address_text.text[2:] != wallet_users['D']['address']:
            self.errors.append('Wrong address %s is shown in "Receive" popup ' % wallet.address_text.text)
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
