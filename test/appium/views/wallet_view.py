import time

from tests import common_password
from views.base_element import Button, Text, EditBox, SilentButton
from views.base_view import BaseView


class TransactionHistoryButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="History-item-button")

    def navigate(self):
        from views.transactions_view import TransactionsView
        return TransactionsView(self.driver)


class SignInPhraseText(Text):
    def __init__(self, driver):
        super().__init__(driver, translation_id="this-is-you-signing", suffix="//following-sibling::*[2]/android.widget.TextView")

    @property
    def list(self):
        return self.text.split()


class AssetCheckBox(SilentButton):
    def __init__(self, driver, asset_name):
        super().__init__(driver, xpath="//*[@text='%s']" % asset_name)

    def click(self):
        self.scroll_to_element(12).click()

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
        return amount_text.is_element_image_equals_template(expected_color_image_name)


class SendTransactionButton(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="wallet-send")

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
            "((//android.widget.ScrollView)[last()]/*/*)[%s]" % str(position+1)).click()


class WalletView(BaseView):
    def __init__(self, driver):
        super().__init__(driver)

        self.send_transaction_button = SendTransactionButton(self.driver)
        self.transaction_history_button = TransactionHistoryButton(self.driver)
        self.usd_total_value = Text(self.driver, accessibility_id="total-amount-value-text")

        self.receive_transaction_button = ReceiveTransactionButton(self.driver)
        self.options_button = Button(self.driver, accessibility_id="options-menu-button")
        self.manage_assets_button = Button(self.driver, accessibility_id="wallet-manage-assets")
        self.scan_tokens_button = Button(self.driver, accessibility_id="wallet-scan-token")
        self.stt_check_box = Button(self.driver, xpath="//*[@text='STT']/../android.view.ViewGroup[@content-desc='checkbox']")
        self.all_assets_full_names = Text(self.driver, xpath="//*[@content-desc='checkbox']/../android.widget.TextView[1]")
        self.all_assets_symbols = Button(self.driver, xpath="//*[@content-desc='checkbox']/../android.widget.TextView[2]")
        self.currency_item_text = Text(self.driver, xpath="//*[@content-desc='currency-item']//android.widget.TextView")

        self.address_text = Text(self.driver, accessibility_id="address-text")

        self.sign_in_phrase = SignInPhraseText(self.driver)
        self.remind_me_later_button = Button(self.driver, translation_id="remind-me-later")

        self.total_amount_text = Text(self.driver, accessibility_id="total-amount-value-text")
        self.currency_text = Text(self.driver, accessibility_id="total-amount-currency-text")
        self.backup_recovery_phrase = BackupRecoveryPhrase(self.driver)
        self.backup_recovery_phrase_warning_text = Text(self.driver, accessibility_id="back-up-your-seed-phrase-warning")

        self.add_custom_token_button = AddCustomTokenButton(self.driver)

        # elements for multiaccount
        self.multiaccount_more_options = Button(self.driver, accessibility_id="accounts-more-options")
        self.accounts_status_account = AccountElementButton(self.driver, account_name=self.status_account_name)
        self.collectibles_button = Button(self.driver, translation_id="wallet-collectibles")
        self.cryptokitties_in_collectibles_number = Text(self.driver, xpath="//*[@text='CryptoKitties']//following-sibling::android.widget.TextView")
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
        self.add_account_generate_account_button = Button(self.driver, accessibility_id="add-account-add-account-button")
        self.status_account_total_usd_value = Text(self.driver, accessibility_id="account-total-value")
        self.scan_qr_button = Button(self.driver, accessibility_id="accounts-qr-code")

        # individual account settings
        self.account_settings_button = Button(self.driver, translation_id="account-settings")
        self.apply_settings_button = Button(self.driver, translation_id="apply")

    def wait_balance_is_equal_expected_amount(self, asset ='ETH', expected_balance=0.1, wait_time=300):
        counter = 0
        while True:
            if counter >= wait_time:
                self.driver.fail('**Balance is not changed during %s seconds!**' % wait_time)
            elif self.get_asset_amount_by_name(asset) != expected_balance:
                counter += 10
                time.sleep(10)
                self.swipe_down()
                self.driver.info('**Waiting %s seconds for %s balance update to be equal to %s**' % (counter,asset, expected_balance))
            else:
                self.driver.info('**Balance for %s is equal to %s**' % (asset, expected_balance))
                return

    def wait_balance_is_changed(self, asset ='ETH', initial_balance=0, wait_time=400, scan_tokens=False):
        self.driver.info('**Waiting %ss for %s updated balance**' % (wait_time, asset))
        counter = 0
        while True:
            if counter >= wait_time:
                self.driver.fail('Balance %s %s is not changed during %s seconds!' % (asset, initial_balance,wait_time))
            elif self.asset_by_name(asset).is_element_present() and self.get_asset_amount_by_name(asset) == initial_balance:
                if not self.transaction_history_button.is_element_displayed():
                    self.wallet_account_by_name(self.status_account_name).click()
                if (counter/60).is_integer():
                    self.pull_to_refresh()
                    counter+=20
                self.wallet_button.double_click()
                counter += 10
                time.sleep(10)
                self.driver.info('*Waiting %ss for %s updated balance*' % (counter,asset))
            elif not self.asset_by_name(asset).is_element_present(10):
                counter += 10
                time.sleep(10)
                if scan_tokens:
                    self.scan_tokens()
                self.swipe_up()
                self.driver.info('*Waiting %s seconds for %s to display asset*' % (counter, asset))
            else:
                self.driver.info('**Balance is updated!**')
                self.wallet_button.double_click()
                self.element_by_translation_id("wallet-total-value").scroll_to_element(direction='up')
                return self

    def get_sign_in_phrase(self):
        return ' '.join([element.text for element in self.sign_in_phrase.find_elements()])

    def set_up_wallet(self):
        self.driver.info("**Setting up wallet**")
        phrase = self.sign_in_phrase.text
        self.ok_got_it_button.click()
        return phrase

    def get_wallet_address(self, account_name=''):
        account_name = self.status_account_name if not account_name else account_name
        self.driver.info("**Getting wallet address for '%s'**" % account_name)
        self.wallet_account_by_name(account_name).click()
        self.receive_transaction_button.click_until_presence_of_element(self.qr_code_image)
        address = self.address_text.text
        self.close_share_popup()
        return address

    def wallet_account_by_name(self, account_name):
        self.driver.info("*Getting '%s' wallet account*" % account_name)
        return AccountElementButton(self.driver, account_name)


    def get_asset_amount_by_name(self, asset: str):
        self.driver.info("*Getting %s amount*" % asset)
        asset_value = SilentButton(self.driver, xpath="//android.view.ViewGroup[@content-desc=':%s-asset-value']"
                                                   "//android.widget.TextView[1]" % asset)
        asset_value.scroll_to_element()
        try:
            return float(asset_value.text.split()[0])
        except ValueError:
            return 0.0

    def asset_by_name(self, asset_name):
        self.driver.info("*Selecting %s asset*" % asset_name)
        return SilentButton(self.driver, xpath="//*[contains(@text,'%s')]" % asset_name)

    def asset_checkbox_by_name(self, asset_name):
        self.driver.info("*Selecting %s asset checkbox by name*" % asset_name)
        return AssetCheckBox(self.driver, asset_name)

    def get_account_options_by_name(self, account_name=''):
        account_name = self.status_account_name if not account_name else account_name
        self.driver.info("*Getting '%s'account options*" % account_name)
        return SilentButton(self.driver, xpath="(//*[@text='%s']/../..//*[@content-desc='icon'])[2]" % account_name)

    def select_asset(self, *args):
        self.driver.info("**Selecting asset(s)**")
        self.multiaccount_more_options.click()
        self.manage_assets_button.click()
        for asset in args:
            self.asset_checkbox_by_name(asset).click()
        self.cross_icon.click()
        self.driver.info("**Assets are selected!**")

    def scan_tokens(self, *args):
        self.driver.info("**Scanning tokens**")
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
        self.driver.info("**Sending transaction**")
        send_transaction_view = self.send_transaction_button.click()
        send_transaction_view.select_asset_button.click()
        asset_name = kwargs.get('asset_name', 'ETH').upper()
        asset_button = send_transaction_view.asset_by_name(asset_name)
        send_transaction_view.select_asset_button.click_until_presence_of_element(send_transaction_view.eth_asset_in_select_asset_bottom_sheet_button)
        asset_button.click()
        send_transaction_view.amount_edit_box.click()

        transaction_amount = str(kwargs.get('amount', send_transaction_view.get_unique_amount()))

        send_transaction_view.amount_edit_box.set_value(transaction_amount)
        if kwargs.get('account_name'):
            send_transaction_view.chose_recipient_button.click()
            send_transaction_view.accounts_button.click()
            send_transaction_view.element_by_text(kwargs.get('account_name')).click()
        else:
            send_transaction_view.set_recipient_address(kwargs.get('recipient'))
        if kwargs.get('sign_transaction', True):
            send_transaction_view.sign_transaction_button.click_until_presence_of_element(send_transaction_view.network_fee_button)
            send_transaction_view.sign_transaction(keycard=kwargs.get('keycard', False),
                                                   default_gas_price=kwargs.get('default_gas_price', False),
                                                   sender_password=kwargs.get('sender_password', common_password))
        return send_transaction_view

    def find_transaction_in_history(self, amount, asset='ETH', account_name=None):
        if account_name == None:
            account_name = self.status_account_name
        self.driver.info('**Finding %s %s transaction for %s**' % (amount, asset, account_name))
        if not self.transaction_history_button.is_element_displayed():
            self.get_account_by_name(account_name).click()
            self.transaction_history_button.wait_for_element()
        transactions_view = self.transaction_history_button.click()
        return transactions_view.transactions_table.find_transaction(amount=amount, asset=asset)

    def set_currency(self, desired_currency='EUR'):
        self.driver.info("**Setting '%s' currency**" % desired_currency)
        self.multiaccount_more_options.click_until_presence_of_element(self.set_currency_button)
        self.set_currency_button.click()
        desired_currency = self.element_by_text_part(desired_currency)
        desired_currency.scroll_to_element()
        desired_currency.click()

    def get_account_by_name(self, account_name: str):
        self.driver.info("**Getting account '%s'**" % account_name)
        return AccountElementButton(self.driver, account_name)

    def add_account(self, account_name: str, password: str = common_password, keycard=False):
        self.driver.info("**Adding account '%s'**" % account_name)
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
            self.add_account_generate_account_button.click()
