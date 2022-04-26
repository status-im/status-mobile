import random
import string
import pytest

from tests import marks, common_password
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from tests.users import wallet_users, basic_user
from views.sign_in_view import SignInView
from support.utilities import get_merged_txs_list


@pytest.mark.xdist_group(name="wallet_management_1")
@marks.critical
class TestWalletManagementDeviceMerged(MultipleSharedDeviceTestCase):

    @classmethod
    def setup_class(cls):
        cls.user = wallet_users['D']
        cls.account_seed_collectibles = 'acc_collectibles'
        cls.drivers, cls.loop = create_shared_drivers(1)
        cls.sign_in = SignInView(cls.drivers[0])
        cls.sign_in.switch_to_mobile(before_login=True)
        cls.home = cls.sign_in.recover_access(cls.user['passphrase'])
        cls.wallet = cls.home.wallet_button.click()
        [cls.wallet.wait_balance_is_changed(asset) for asset in ('ETH', 'MDS', 'STT')]
        cls.initial_balances = {'ETH': cls.wallet.get_asset_amount_by_name('ETH'),
                                'ADI': 0,
                                'STT': cls.wallet.get_asset_amount_by_name('STT')}

    @marks.testrail_id(700756)
    def test_wallet_tx_history_copy_tx_hash_on_cellular(self):
        self.wallet.accounts_status_account.click()
        address = wallet_users['D']['address']
        ropsten_txs = self.network_api.get_transactions(address)
        ropsten_tokens = self.network_api.get_token_transactions(address)
        expected_txs_list = get_merged_txs_list(ropsten_txs, ropsten_tokens)

        self.wallet.just_fyi("Checking empty tx history and pull-to-refresh for update")
        transactions = self.wallet.transaction_history_button.click()
        if not self.wallet.element_by_translation_id("transactions-history-empty").is_element_displayed():
            self.errors.append("Transaction history was loaded automatically on mobila data!")
        self.wallet.pull_to_refresh()
        if self.wallet.element_by_translation_id("transactions-history-empty").is_element_displayed():
            self.wallet.pull_to_refresh()
        status_tx_number = transactions.transactions_table.get_transactions_number()
        if status_tx_number < 1:
            self.errors.append('No transactions found')

        self.wallet.just_fyi("Checking whole tx history")
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

        self.wallet.just_fyi("Open transaction on etherscan")
        transaction_details = transactions.transactions_table.transaction_by_index(0).click()
        transaction_hash = transaction_details.get_transaction_hash()
        transaction_details.options_button.click()
        transaction_details.open_transaction_on_etherscan_button.click()
        web_page = self.wallet.get_base_web_view()
        web_page.open_in_webview()
        web_page.element_by_text_part(transaction_hash).wait_for_visibility_of_element(30)

        self.wallet.just_fyi("Copy transaction hash")
        web_page.click_system_back_button()
        transaction_details.options_button.click()
        transaction_details.copy_transaction_hash_button.click()
        self.wallet.home_button.click()
        public_chat = self.home.join_public_chat('testchat')
        public_chat.chat_message_input.paste_text_from_clipboard()
        if public_chat.chat_message_input.text != transaction_hash:
            self.errors.append('Transaction hash was not copied')
        self.errors.verify_no_errors()

    @marks.testrail_id(700759)
    def test_wallet_add_account_generate_new(self):
        self.wallet.just_fyi("Switching off LTE mode and navigating to home view")
        self.wallet.driver.set_network_connection(6)
        self.wallet.wallet_button.double_click()

        self.wallet.add_account_button.click_until_presence_of_element(self.wallet.generate_an_account_button)
        self.wallet.generate_an_account_button.click()
        self.wallet.add_account_generate_account_button.click()
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        self.wallet.account_name_input.send_keys(account_name)
        self.wallet.account_color_button.select_color_by_position(1)

        self.wallet.just_fyi("Checking basic validation when adding multiaccount")
        if self.wallet.get_account_by_name(account_name).is_element_displayed():
            self.drivers[0].fail('Account is added without password')
        self.wallet.enter_your_password_input.send_keys('000000')
        self.wallet.add_account_generate_account_button.click()
        if not self.wallet.element_by_text_part('Password seems to be incorrect').is_element_displayed():
            self.drivers[0].fail("Incorrect password validation is not performed")
        self.wallet.enter_your_password_input.clear()
        self.wallet.enter_your_password_input.send_keys(common_password)
        self.wallet.add_account_generate_account_button.click()
        account_button = self.wallet.get_account_by_name(account_name)

        self.wallet.just_fyi("Checking that selected color is applied")
        if not account_button.is_element_displayed():
            self.wallet.accounts_status_account.swipe_left_on_element()
        if not account_button.color_matches('multi_account_color.png'):
            self.drivers[0].fail('Account color does not match expected')
        self.wallet.get_account_by_name(account_name).click()
        self.wallet.get_account_options_by_name(account_name).click()
        self.wallet.account_settings_button.click()
        self.wallet.swipe_up()
        if self.wallet.delete_account_button.is_element_displayed(10):
            self.drivers[0].fail('Delete account option is shown on added account "On Status Tree"!')

    @marks.testrail_id(700758)
    def test_wallet_manage_assets(self):
        asset = "HND"
        self.sign_in.just_fyi("Getting back to main wallet view")
        self.wallet.get_back_to_home_view()

        self.sign_in.just_fyi("Enabling 0 asset on wallet and check it is shown")
        self.wallet.select_asset(asset)
        self.wallet.asset_by_name(asset).scroll_to_element()
        if not self.wallet.asset_by_name(asset).is_element_displayed():
            self.errors.append('%s asset is not shown in wallet' % asset)

        self.sign_in.just_fyi("Check that 0 asset is not disappearing after relogin")
        profile = self.wallet.profile_button.click()
        profile.relogin()
        self.sign_in.wallet_button.click()
        if not self.wallet.asset_by_name(asset).is_element_displayed():
            self.errors.append('%s asset is not shown in wallet after relogin' % asset)

        self.sign_in.just_fyi("Deselecting asset")
        self.wallet.multiaccount_more_options.click()
        self.wallet.manage_assets_button.click()
        self.wallet.asset_checkbox_by_name(asset).click()
        self.wallet.cross_icon.click()
        if self.wallet.asset_by_name(asset).is_element_displayed():
            self.errors.append('%s asset is shown in wallet but was deselected' % asset)
        self.errors.verify_no_errors()

    @marks.testrail_id(700760)
    def test_wallet_add_delete_watch_only_account(self):
        self.wallet.get_back_to_home_view()
        self.wallet.accounts_status_account.swipe_left_on_element()

        self.wallet.just_fyi('Add watch-only account')
        if not self.wallet.add_account_button.is_element_displayed(3):
            self.wallet.accounts_status_account.swipe_left_on_element()
        self.wallet.add_account_button.click()
        self.wallet.add_watch_only_address_button.click()
        self.wallet.enter_address_input.send_keys(basic_user['address'])
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        self.wallet.account_name_input.send_keys(account_name)
        self.wallet.add_account_generate_account_button.click()
        account_button = self.wallet.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.drivers[0].fail('Account was not added')

        self.wallet.just_fyi('Check that overall balance is changed after adding watch-only account')
        for asset in self.initial_balances:
            self.wallet.wait_balance_is_changed(asset=asset, initial_balance=self.initial_balances[asset])

        self.wallet.just_fyi('Check individual watch-only account view, settings and receive option')
        self.wallet.get_account_by_name(account_name).click()
        if self.wallet.send_transaction_button.is_element_displayed():
            self.errors.append('Send button is shown on watch-only wallet')
        if not self.wallet.element_by_text('Watch-only').is_element_displayed():
            self.errors.append('No "Watch-only" label is shown on watch-only wallet')
        self.wallet.receive_transaction_button.click_until_presence_of_element(self.wallet.address_text)
        if self.wallet.address_text.text[2:] != basic_user['address']:
            self.errors.append(
                'Wrong address %s is shown in "Receive" popup for watch-only account ' % self.wallet.address_text.text)
        self.wallet.close_share_popup()
        self.wallet.get_account_options_by_name(account_name).click()
        self.wallet.account_settings_button.click()
        if not self.wallet.element_by_text('Watch-only').is_element_displayed():
            self.errors.append('"Watch-only" type is not shown in account settings')

        self.wallet.just_fyi('Delete watch-only account')
        self.wallet.delete_account_button.click()
        self.wallet.yes_button.click()
        if account_button.is_element_displayed():
            self.driver.fail('Account was not deleted')
        for asset in self.initial_balances:
            self.wallet.wait_balance_is_equal_expected_amount(asset, self.initial_balances[asset])

        self.errors.verify_no_errors()

    @marks.testrail_id(700761)
    def test_wallet_add_hide_unhide_account_private_key(self):
        self.wallet.get_back_to_home_view()
        if not self.wallet.add_account_button.is_element_displayed(3):
            self.wallet.accounts_status_account.swipe_left_on_element()
        self.wallet.add_account_button.click()
        self.wallet.enter_a_private_key_button.click()
        self.wallet.enter_your_password_input.send_keys(common_password)
        self.wallet.enter_a_private_key_input.set_value(wallet_users['C']['private_key'][0:9])
        account_name_private = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        self.wallet.account_name_input.send_keys(account_name_private)
        self.wallet.add_account_generate_account_button.click()
        if self.wallet.get_account_by_name(account_name_private).is_element_displayed():
            self.driver.fail('Account is added with wrong private key')
        self.wallet.enter_a_private_key_input.set_value(wallet_users['C']['private_key'])
        self.wallet.add_account_generate_account_button.click()
        account_button = self.wallet.get_account_by_name(account_name_private)
        if not account_button.is_element_displayed():
            self.driver.fail('Account from private key was not added')

        self.wallet.just_fyi('Check that overall balance is changed after adding account from private key')
        for asset in self.initial_balances:
            self.wallet.wait_balance_is_changed(asset=asset, initial_balance=self.initial_balances[asset])

        self.wallet.just_fyi('Check individual account view (imported from private key), receive option')
        self.wallet.get_account_by_name(account_name_private).scroll_and_click(direction="up")
        if not self.wallet.send_transaction_button.is_element_displayed():
            self.errors.append('Send button is not shown on account added with private key')
        self.wallet.receive_transaction_button.click()
        if self.wallet.address_text.text[2:] != wallet_users['C']['address']:
            self.errors.append('Wrong address %s is shown in "Receive" popup account ' % self.wallet.address_text.text)
        self.wallet.wallet_button.double_click()

        self.wallet.just_fyi("Hide account and check balance according to hidden account")
        self.wallet.get_account_options_from_main_screen(account_name_private).click()
        self.wallet.hide_account_button.click()
        if self.wallet.get_account_by_name(account_name_private).is_element_displayed():
            self.errors.append("Hidden %s is shown on main wallet view" % account_name_private)
        for asset in self.initial_balances:
            self.wallet.wait_balance_is_equal_expected_amount(asset, self.initial_balances[asset])
        self.wallet.multiaccount_more_options.click()
        self.wallet.manage_accounts_button.click()
        if not self.wallet.hidden_account_by_name_button(account_name_private).is_element_displayed():
            self.errors.append("Hidden icon is not shown for hidden account")

        self.wallet.just_fyi("Unhide account and check balance according to hidden account")
        self.wallet.element_by_text(account_name_private).click()
        if not self.wallet.show_account_by_name_button(account_name_private).is_element_displayed():
            self.errors.append("'Show icon' is not shown for not hidden account")
        self.wallet.get_back_to_home_view()
        if not self.wallet.get_account_by_name(account_name_private).is_element_displayed():
            self.wallet.accounts_status_account.swipe_left_on_element()

        if not self.wallet.get_account_by_name(account_name_private).is_element_displayed():
            self.errors.append("Unhidden %s is shown on main wallet view after hiding via 'Show icon'" % account_name_private)
        for asset in self.initial_balances:
            self.wallet.wait_balance_is_changed(asset=asset, initial_balance=self.initial_balances[asset])

        self.errors.verify_no_errors()

    @marks.testrail_id(700762)
    # TODO: skipped due to #13016
    def test_wallet_add_account_seed_phrase_collectibles_mainnet_rinkeby(self):
        user = wallet_users['E']
        account_seed_collectibles = self.account_seed_collectibles
        self.wallet.get_back_to_home_view()
        if not self.wallet.add_account_button.is_element_displayed(3):
            self.wallet.accounts_status_account.swipe_left_on_element()
        self.wallet.add_account_button.click()
        self.wallet.enter_a_seed_phrase_button.click()

        self.home.just_fyi('Check basic validation when adding account from seed phrase')
        self.wallet.enter_your_password_input.send_keys(common_password)
        self.wallet.enter_seed_phrase_input.set_value('')
        self.wallet.account_name_input.send_keys(account_seed_collectibles)
        self.wallet.add_account_generate_account_button.click()
        if self.wallet.get_account_by_name(account_seed_collectibles).is_element_displayed():
            self.driver.fail('Account is added without seed phrase')
        self.wallet.enter_seed_phrase_input.set_value(str(wallet_users['D']['passphrase']).upper())
        self.wallet.add_account_generate_account_button.click()
        if self.wallet.get_account_by_name(account_seed_collectibles).is_element_displayed():
            self.driver.fail('Same account was added twice')

        self.wallet.enter_your_password_input.send_keys(common_password)
        self.wallet.enter_seed_phrase_input.set_value(str(user['passphrase']).upper())
        self.wallet.account_name_input.send_keys(account_seed_collectibles)
        self.wallet.add_account_generate_account_button.click()
        account_button = self.wallet.get_account_by_name(account_seed_collectibles)
        if not account_button.is_element_displayed():
            self.wallet.accounts_status_account.swipe_left_on_element()

        # self.home.just_fyi('Check that collectibles are not shown on Ropsten')
        # account_button.click()
        # self.wallet.collectibles_button.click()
        # self.wallet.element_by_translation_id("display-collectibles").scroll_and_click()
        # if not self.wallet.element_by_translation_id("no-collectibles").is_element_displayed():
        #     self.errors.append("Collectibles are shown on Ropsten network!")

        self.wallet.just_fyi('Check collectibles amount in wallet')
        profile = self.home.profile_button.click()
        profile.switch_network()
        profile.wallet_button.click()
        if not account_button.is_element_displayed():
            self.wallet.accounts_status_account.swipe_left_on_element()
        account_button.click()
        self.wallet.collectibles_button.click()
        self.wallet.element_by_translation_id("display-collectibles").scroll_and_click()
        collectible_amount = self.wallet.get_collectibles_amount()
        collectible_amount.wait_for_visibility_of_element(30)
        if collectible_amount.text != '1':
            self.errors.append('Wrong number is shown on CK assets: %s' % collectible_amount.text)

        self.wallet.just_fyi('Check that you can open collectible to view')
        collectible_amount.click()
        if not self.wallet.nft_asset_button.is_element_displayed(60):
            self.driver.fail("Kitty is not shown after opening it from collectibles!")
        self.wallet.nft_asset_button.click()
        self.wallet.set_collectible_as_profile_photo_button.scroll_and_click()

        self.wallet.just_fyi('Check that you can set collectible as profile photo')
        web_view = self.wallet.get_base_web_view()
        self.wallet.view_collectible_on_opensea_button.click_until_presence_of_element(
            web_view.browser_previous_page_button)
        web_view.wait_for_d_aap_to_load()
        if not web_view.element_by_text('Princess Gunklater').is_element_displayed(30):
            self.errors.append("Collectible can't be opened when tapping 'View on OpenSea' via NFT page")
        self.wallet.wallet_button.click()

        self.wallet.just_fyi('Check that collectibles are not shown when sending assets from wallet')
        send_transaction = self.wallet.send_transaction_button.click()
        send_transaction.select_asset_button.click()
        if send_transaction.asset_by_name("CryptoKitties").is_element_displayed():
            self.errors.append('Collectibles can be sent from wallet')
        self.wallet.close_send_transaction_view_button.double_click()

        self.wallet.just_fyi('Check "Open in OpenSea" (that user is signed in)')
        self.wallet.element_by_translation_id("check-on-opensea").click_until_presence_of_element(
            web_view.browser_previous_page_button)
        web_view.wait_for_d_aap_to_load(10)
        self.wallet.element_by_text('e2ecryptokitty').wait_for_element(60)

        self.wallet.just_fyi("Check that custom image from collectible is set as profile photo")
        self.wallet.profile_button.double_click()
        if not profile.profile_picture.is_element_image_similar_to_template('collectible_pic.png'):
            self.errors.append("Collectible image is not set as profile image")

        self.home.just_fyi('Check that collectibles amount is shown on Rinkeby')
        profile = self.home.profile_button.click()
        profile.switch_network('Rinkeby with upstream RPC')
        profile = self.home.profile_button.click()
        profile.wallet_button.click()
        if not account_button.is_element_displayed():
            self.wallet.accounts_status_account.swipe_left_on_element()
        self.wallet.scan_tokens()
        account_button.click()
        self.wallet.transaction_history_button.click()
        self.wallet.collectibles_button.click()
        self.wallet.swipe_up()
        if self.wallet.element_by_translation_id("display-collectibles").is_element_displayed:
            self.wallet.element_by_translation_id("display-collectibles").click()
        for asset in user['collectibles']:
            self.wallet.get_collectibles_amount(asset).scroll_to_element()
            if self.wallet.get_collectibles_amount(asset).text != user['collectibles'][asset]:
                self.errors.append(
                    '%s %s is not shown in Collectibles for Rinkeby!' % (user['collectibles'][asset], asset))
        self.errors.verify_no_errors()

    @marks.testrail_id(700766)
    def test_wallet_fetching_balance_after_offline_insufficient_funds_errors(self):
        self.sign_in.driver.reset()
        sender = wallet_users['E']

        self.sign_in.just_fyi('Checking if balance will be restored after going back online')
        self.sign_in.toggle_airplane_mode()
        home = self.sign_in.recover_access(sender['passphrase'])
        self.sign_in.toggle_airplane_mode()
        wallet = home.wallet_button.click()
        [wallet.wait_balance_is_changed(asset) for asset in ("ETH", "STT")]
        [eth_value, stt_value] = [wallet.get_asset_amount_by_name(asset) for asset in ("ETH", "STT")]

        self.sign_in.just_fyi('Checking insufficient_balance errors')
        wallet.accounts_status_account.click()
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.amount_edit_box.set_value(round(eth_value + 1))
        error_text = send_transaction.element_by_text('Insufficient funds')
        if not error_text.is_element_displayed():
            self.errors.append(
                "'Insufficient funds' error is not shown when sending %s ETH from wallet with balance %s" % (
                    round(eth_value + 1), eth_value))
        send_transaction.select_asset_button.click()
        send_transaction.asset_by_name('STT').scroll_to_element()
        send_transaction.asset_by_name('STT').click()
        send_transaction.amount_edit_box.set_value(round(stt_value + 1))
        if not error_text.is_element_displayed():
            self.errors.append(
                "'Insufficient funds' error is not shown when sending %s STT from wallet with balance %s" % (
                    round(stt_value + 1), stt_value))
        self.errors.verify_no_errors()