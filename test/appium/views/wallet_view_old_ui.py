import time

from tests import common_password
from views.base_element import Button, Text, EditBox, SilentButton, CheckBox
from views.base_view import BaseView


class TransactionHistoryButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="History-item-button")

    def navigate(self):
        from views.transactions_view import TransactionsView
        return TransactionsView(self.driver)


class AssetCheckBox(CheckBox):
    def __init__(self, driver, asset_name):
        super().__init__(driver, xpath="//*[@text='%s']" % asset_name)

    def enable(self):
        self.scroll_to_element(12)
        super().enable()


class BackupRecoveryPhrase(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="wallet-backup-recovery-title")

    def navigate(self):
        from views.profile_view import ProfileView
        return ProfileView(self.driver)


class AccountElementButton(SilentButton):
    def __init__(self, driver, account_name):
        super().__init__(driver, xpath="//*[@content-desc='accountcard%s']" % account_name)

    def color_matches(self, expected_color_image_name: str):
        amount_text = Text(self.driver, xpath="%s//*[@content-desc='account-total-value']" % self.locator)
        amount_text.wait_for_element_text('...', 60)
        return not amount_text.is_element_differs_from_template(expected_color_image_name)


class SendTransactionButton(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="wallet-send")

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)


class SendTransactionFromMainButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="send-transaction-button")

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)


class ReceiveTransactionButton(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="receive")

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)


class AddCustomTokenButton(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="add-custom-token")

    def navigate(self):
        from views.add_custom_token_view import AddCustomTokenView
        return AddCustomTokenView(self.driver)


class AccountColorButton(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="account-color", suffix="/following-sibling::android.view.ViewGroup[1]")

    def select_color_by_position(self, position: int):
        self.click()
        self.driver.find_element_by_xpath(
            "((//android.widget.ScrollView)[1]/*/*)[%s]" % str(position + 1)).click()


class WalletView(BaseView):
    def __init__(self, driver):
        super().__init__(driver)

        self.send_transaction_button = SendTransactionButton(self.driver)
        self.send_transaction_from_main_screen = SendTransactionFromMainButton(self.driver)
        self.transaction_history_button = TransactionHistoryButton(self.driver)
        self.usd_total_value = Text(self.driver, accessibility_id="total-amount-value-text")

        self.receive_transaction_button = ReceiveTransactionButton(self.driver)
        self.options_button = Button(self.driver, accessibility_id="options-menu-button")
        self.manage_assets_button = Button(self.driver, accessibility_id="wallet-manage-assets")
        self.manage_accounts_button = Button(self.driver, accessibility_id="wallet-manage-accounts")
        self.scan_tokens_button = Button(self.driver, accessibility_id="wallet-scan-token")
        self.all_assets_full_names = Text(self.driver,
                                          xpath="//*[@content-desc='checkbox-off']/../android.widget.TextView[1]")
        self.all_assets_symbols = Button(self.driver,
                                         xpath="//*[@content-desc='checkbox-off']/../android.widget.TextView[2]")
        self.currency_item_text = Text(self.driver, xpath="//*[@content-desc='currency-item']//android.widget.TextView")

        self.address_text = Text(self.driver, accessibility_id="address-text")

        self.remind_me_later_button = Button(self.driver, translation_id="remind-me-later")

        self.total_amount_text = Text(self.driver, accessibility_id="total-amount-value-text")
        self.currency_text = Text(self.driver, accessibility_id="total-amount-currency-text")
        self.backup_recovery_phrase = BackupRecoveryPhrase(self.driver)
        self.backup_recovery_phrase_warning_text = Text(self.driver,
                                                        accessibility_id="back-up-your-seed-phrase-warning")

        self.add_custom_token_button = AddCustomTokenButton(self.driver)

        # elements for multiaccount
        self.multiaccount_more_options = Button(self.driver, accessibility_id="accounts-more-options")
        self.accounts_status_account = AccountElementButton(self.driver, account_name=self.status_account_name)
        self.set_currency_button = Button(self.driver, translation_id="set-currency")
        self.add_account_button = Button(self.driver, accessibility_id="add-new-account")
        self.generate_an_account_button = Button(self.driver, accessibility_id="add-account-sheet-generate")
        self.add_watch_only_address_button = Button(self.driver, accessibility_id="add-account-sheet-watch")
        self.enter_a_seed_phrase_button = Button(self.driver, accessibility_id="add-account-sheet-seed")
        self.enter_a_private_key_button = Button(self.driver, accessibility_id="add-account-sheet-private-key")
        self.enter_address_input = EditBox(self.driver, accessibility_id="add-account-enter-watch-address")
        self.enter_seed_phrase_input = EditBox(self.driver, accessibility_id="add-account-enter-seed")
        self.enter_a_private_key_input = EditBox(self.driver, accessibility_id="add-account-enter-private-key")
        self.delete_account_button = Button(self.driver, translation_id="delete-account")
        self.enter_your_password_input = EditBox(self.driver, accessibility_id="add-account-enter-password")
        self.account_name_input = EditBox(self.driver, accessibility_id="enter-account-name")
        self.account_color_button = AccountColorButton(self.driver)
        self.add_account_generate_account_button = Button(self.driver,
                                                          accessibility_id="add-account-add-account-button")
        self.status_account_total_usd_value = Text(self.driver, accessibility_id="account-total-value")
        self.scan_qr_button = Button(self.driver, accessibility_id="accounts-qr-code")
        self.close_send_transaction_view_button = Button(self.driver,
                                                         xpath="//androidx.appcompat.widget.LinearLayoutCompat")
        self.hide_account_button = Button(self.driver, accessibility_id="hide-account-button")

        # collectibles
        self.collectibles_button = Button(self.driver, translation_id="wallet-collectibles")
        self.nft_asset_button = Button(self.driver, accessibility_id="nft-asset")
        self.set_collectible_as_profile_photo_button = Button(self.driver, accessibility_id="set-nft-as-pfp")
        self.view_collectible_on_opensea_button = Button(self.driver, translation_id="view-on-opensea")

        # individual account settings
        self.account_settings_button = Button(self.driver, translation_id="account-settings")
        self.apply_settings_button = Button(self.driver, translation_id="apply")
        self.password_delete_account_input = EditBox(self.driver,
                                                     xpath='//*[@text="Password"]/following-sibling::*/android.widget.EditText')
        self.delete_account_confirm_button = Button(self.driver, accessibility_id="delete-account-confirm")

    def wait_balance_is_equal_expected_amount(self, asset='ETH', expected_balance=0.1, wait_time=300, main_screen=True):
        counter = 0
        while True:
            if counter >= wait_time:
                self.driver.fail('**Balance is not changed during %s seconds!**' % wait_time)
            elif self.get_asset_amount_by_name(asset) != expected_balance:
                counter += 10
                time.sleep(10)
                self.swipe_down()
                self.driver.info('Waiting %s seconds for %s balance update to be equal to %s' % (
                    counter, asset, expected_balance))
            else:
                self.driver.info('Balance for %s is equal to %s' % (asset, expected_balance))
                if main_screen:
                    if not self.accounts_status_account.is_element_displayed():
                        self.accounts_status_account.scroll_to_element(direction='up')
                return

    def wait_balance_is_changed(self, asset='ETH', initial_balance=0, wait_time=180, scan_tokens=False, navigate_to_home=True):
        self.driver.info('Waiting %ss for %s updated balance' % (wait_time, asset))
        counter = 0
        while True:
            if counter >= wait_time:
                self.driver.fail(
                    'Balance %s %s is not changed during %s seconds!' % (asset, initial_balance, wait_time))
            elif self.asset_by_name(asset).is_element_displayed() and self.get_asset_amount_by_name(
                    asset) == initial_balance:
                if scan_tokens:
                    self.scan_tokens()
                if (counter / 60).is_integer():
                    self.pull_to_refresh()
                    counter += 20
                counter += 10
                time.sleep(10)
                self.driver.info('Waiting %ss for %s updated balance' % (counter, asset))
            elif not self.asset_by_name(asset).is_element_displayed(10):
                if scan_tokens:
                    self.scan_tokens()
                self.swipe_up()
                counter += 10
                time.sleep(10)
                self.driver.info('Waiting %s seconds for %s to display asset' % (counter, asset))
            else:
                self.driver.info('Initial "%s" is not equal expected balance "%s", it is updated!' % (initial_balance,
                                 self.get_asset_amount_by_name(asset)))
                if navigate_to_home:
                    self.wallet_button.double_click()
                    self.element_by_translation_id("wallet-total-value").scroll_to_element(direction='up')
                return self

    def get_sign_in_phrase(self):
        return ' '.join([element.text for element in self.sign_in_phrase.find_elements()])

    def set_up_wallet_when_sending_tx(self):
        self.driver.info("Setting up wallet")
        phrase = self.sign_in_phrase.text
        self.ok_got_it_button.click()
        return phrase

    def get_wallet_address(self, account_name=''):
        account_name = self.status_account_name if not account_name else account_name
        self.driver.info("Getting wallet address for '%s'" % account_name)
        self.wallet_account_by_name(account_name).click()
        self.receive_transaction_button.click_until_presence_of_element(self.qr_code_image)
        address = self.address_text.text
        self.close_share_popup()
        return address

    def wallet_account_by_name(self, account_name):
        self.driver.info("Getting '%s' wallet account" % account_name)
        return AccountElementButton(self.driver, account_name)

    def get_asset_amount_by_name(self, asset: str):
        self.driver.info("Getting %s amount" % asset)
        asset_value = SilentButton(self.driver, xpath="//android.view.ViewGroup[@content-desc=':%s-asset-value']"
                                                      "//android.widget.TextView[1]" % asset)
        for _ in range(2):
            if not asset_value.is_element_displayed():
                self.element = asset_value.scroll_to_element()
        try:
            value = float(asset_value.text.split()[0])
            self.driver.info("%s value is %s" % (asset, value))
            return value
        except ValueError:
            self.driver.info("No value for %s" % asset)
            return 0.0

    def asset_by_name(self, asset_name):
        self.driver.info("Selecting %s asset" % asset_name)
        return SilentButton(self.driver, xpath="//*[contains(@text,'%s')]" % asset_name)

    def asset_checkbox_by_name(self, asset_name):
        self.driver.info("Selecting %s asset checkbox by name" % asset_name)
        return AssetCheckBox(self.driver, asset_name)

    def get_account_options_by_name(self, account_name=''):
        account_name = self.status_account_name if not account_name else account_name
        self.driver.info("Getting '%s'account options" % account_name)
        return SilentButton(self.driver, xpath="(//*[@text='%s']/../..//*[@content-desc='icon'])[2]" % account_name)

    def get_account_options_from_main_screen(self, account_name=''):
        account_name = self.status_account_name if not account_name else account_name
        self.driver.info("Getting '%s'account options from main wallet screen" % account_name)
        return SilentButton(self.driver,
                            xpath="//*[@content-desc='accountcard%s']//*[@content-desc='icon']" % account_name)

    def hidden_account_by_name_button(self, account_name=''):
        return SilentButton(self.driver,
                            xpath="//*[@text='%s']/following-sibling::*[@content-desc='hide-icon']" % account_name)

    def show_account_by_name_button(self, account_name=''):
        return SilentButton(self.driver,
                            xpath="//*[@text='%s']/following-sibling::*[@content-desc='show-icon']" % account_name)

    def select_asset(self, *args):
        self.driver.info("Selecting asset(s)")
        self.multiaccount_more_options.click()
        self.manage_assets_button.click()
        for asset in args:
            self.element_by_text(asset).scroll_to_element()
            self.element_by_text(asset).scroll_and_click()
        self.cross_icon.click()

    def scan_tokens(self, *args):
        self.driver.info("Scanning tokens")
        self.multiaccount_more_options.click()
        self.scan_tokens_button.click()
        counter = 0
        if args:
            for asset in args:
                while True:
                    if counter >= 20:
                        self.driver.fail('Balance of %s is not changed during 20 seconds!' % asset)
                    elif self.get_asset_amount_by_name(asset) == 0.0:
                        self.multiaccount_more_options.click()
                        self.scan_tokens_button.click()
                        self.driver.info('Trying to scan for tokens one more time and waiting %s seconds for %s '
                                         'to update' % (counter, asset))
                        time.sleep(5)
                        counter += 5
                    else:
                        self.driver.info('Balance of %s is updated!' % asset)
                        return self

    def send_transaction(self, **kwargs):
        self.driver.info("## Sending transaction", device=False)
        send_tx = self.send_transaction_from_main_screen.click() if kwargs.get('from_main_wallet',
                                                                               True) else self.send_transaction_button.click()
        send_tx.select_asset_button.click()
        asset_name = kwargs.get('asset_name', 'ETH').upper()
        asset_button = send_tx.asset_by_name(asset_name)
        send_tx.select_asset_button.click_until_presence_of_element(
            send_tx.eth_asset_in_select_asset_bottom_sheet_button)
        asset_button.click()
        send_tx.amount_edit_box.click()

        transaction_amount = str(kwargs.get('amount', send_tx.get_unique_amount()))

        send_tx.amount_edit_box.send_keys(transaction_amount)
        if kwargs.get('account_name'):
            send_tx.chose_recipient_button.click()
            send_tx.accounts_button.click()
            send_tx.element_by_text(kwargs.get('account_name')).click()
        else:
            send_tx.set_recipient_address(kwargs.get('recipient'))
        if kwargs.get('sign_transaction', True):
            send_tx.sign_transaction_button.click()
            if self.sign_in_phrase.is_element_displayed():
                self.set_up_wallet_when_sending_tx()
            send_tx.sign_transaction(keycard=kwargs.get('keycard', False),
                                     sender_password=kwargs.get('sender_password', common_password))
        return send_tx

    def find_transaction_in_history(self, amount, asset='ETH', account_name=None, return_hash=False):
        if account_name is None:
            account_name = self.status_account_name
        self.driver.info("Finding '%s %s' transaction for '%s'" % (amount, asset, account_name))
        if not self.transaction_history_button.is_element_displayed():
            self.get_account_by_name(account_name).click()
            self.transaction_history_button.wait_for_element()
        transactions_view = self.transaction_history_button.click()
        transaction_element = transactions_view.transactions_table.find_transaction(amount=amount, asset=asset)
        result = transaction_element
        if return_hash:
            transaction_element.click()
            from views.transactions_view import TransactionTable
            result = TransactionTable.TransactionElement.TransactionDetailsView(self.driver).get_transaction_hash()
        return result

    def set_currency(self, desired_currency='EUR'):
        self.driver.info("Setting '%s' currency" % desired_currency)
        self.multiaccount_more_options.click_until_presence_of_element(self.set_currency_button)
        self.set_currency_button.click()
        desired_currency = self.element_by_text_part(desired_currency)
        desired_currency.scroll_to_element()
        desired_currency.click()

    def get_account_by_name(self, account_name: str):
        self.driver.info("Getting account: '%s'" % account_name)
        return AccountElementButton(self.driver, account_name)

    def add_account(self, account_name: str, password: str = common_password, keycard=False):
        self.driver.info("## Add account: '%s'" % account_name, device=False)
        self.add_account_button.click()
        self.generate_an_account_button.click()
        self.account_name_input.send_keys(account_name)
        if keycard:
            from views.keycard_view import KeycardView
            keycard_view = KeycardView(self.driver)
            self.add_account_generate_account_button.click()
            keycard_view.enter_default_pin()
        else:
            self.enter_your_password_input.send_keys(password)
            self.add_account_generate_account_button.click_until_presence_of_element(self.accounts_status_account)
        self.driver.info("## Account is added!", device=False)

    def get_collectibles_amount(self, collectibles='CryptoKitties'):
        self.driver.info("Getting '%s' Collectibles amount" % collectibles)
        return Text(self.driver, xpath="//*[@text='%s']//following-sibling::android.widget.TextView" % collectibles)
