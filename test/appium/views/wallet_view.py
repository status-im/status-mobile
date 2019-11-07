import time

from tests import common_password
from views.base_element import BaseButton, BaseText, BaseEditBox
from views.base_view import BaseView


class SendRequestButton(BaseButton):

    def __init__(self, driver):
        super(SendRequestButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('sent-request-button')


class ChooseRecipientButton(BaseButton):

    def __init__(self, driver):
        super(ChooseRecipientButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('choose-recipient-button')


class TransactionHistoryButton(BaseButton):

    def __init__(self, driver):
        super(TransactionHistoryButton, self).__init__(driver)
        self.locator = self.Locator.text_selector("History")

    def navigate(self):
        from views.transactions_view import TransactionsView
        return TransactionsView(self.driver)


class ChooseFromContactsButton(BaseButton):
    def __init__(self, driver):
        super(ChooseFromContactsButton, self).__init__(driver)
        self.locator = self.Locator.text_selector("Choose From Contacts")


class AssetText(BaseText):
    def __init__(self, driver, asset):
        super(AssetText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='%s']/preceding-sibling::*[1]" % asset)


class UsdTotalValueText(BaseText):
    def __init__(self, driver):
        super(UsdTotalValueText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('total-amount-value-text')


class SendTransactionRequestButton(BaseButton):
    def __init__(self, driver):
        super(SendTransactionRequestButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('sent-transaction-request-button')

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)


class OptionsButton(BaseButton):
    def __init__(self, driver):
        super(OptionsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('options-menu-button')

class AccountOptionsButton(BaseButton):
    def __init__(self, driver, account_name):
        super(AccountOptionsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//*[@text="%s"]/..//*[@content-desc="icon"])[2]' % account_name)


class ManageAssetsButton(BaseButton):
    def __init__(self, driver):
        super(ManageAssetsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('wallet-manage-assets')


class STTCheckBox(BaseButton):
    def __init__(self, driver):
        super(STTCheckBox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='STT']"
                                                   "/../android.view.ViewGroup[@content-desc='checkbox']")


class QRCodeImage(BaseButton):
    def __init__(self, driver):
        super(QRCodeImage, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('qr-code-image')


class AddressText(BaseButton):
    def __init__(self, driver):
        super(AddressText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('address-text')


class SetUpButton(BaseButton):
    def __init__(self, driver):
        super(SetUpButton, self).__init__(driver)
        self.locator = self.Locator.text_selector("Let’s get set up")


class SetCurrencyButton(BaseButton):
    def __init__(self, driver):
        super(SetCurrencyButton, self).__init__(driver)
        self.locator = self.Locator.text_selector("Set currency")


class SignInPhraseText(BaseText):
    def __init__(self, driver):
        super(SignInPhraseText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="This is your signing phrase"]'
                                                   '//following-sibling::*[2]/android.widget.TextView')

    @property
    def string(self):
        return self.text

    @property
    def list(self):
        return self.string.split()


class RemindMeLaterButton(BaseButton):
    def __init__(self, driver):
        super(RemindMeLaterButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Remind me later']")


class AssetTextElement(BaseText):
    def __init__(self, driver, asset_name):
        super(AssetTextElement, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='%s']" % asset_name)


class CollectibleTextElement(BaseText):
    def __init__(self, driver, collectible_name):
        super().__init__(driver)
        self.locator = self.Locator.accessibility_id('%s-collectible-value-text' % collectible_name.lower())


class AssetCheckBox(BaseButton):
    def __init__(self, driver, asset_name):
        super(AssetCheckBox, self).__init__(driver)
        self.asset_name = asset_name
        self.locator = self.Locator.xpath_selector("//*[@text='%s']" % self.asset_name)

    def click(self):
        self.scroll_to_element().click()
        self.driver.info('Click %s asset checkbox' % self.asset_name)


class TotalAmountText(BaseText):

    def __init__(self, driver):
        super(TotalAmountText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('total-amount-value-text')


class CurrencyText(BaseText):

    def __init__(self, driver):
        super(CurrencyText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('total-amount-currency-text')


class CollectiblesButton(BaseButton):
    def __init__(self, driver):
        super(CollectiblesButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Collectibles')


class CryptoKittiesInCollectiblesButton(BaseButton):
    def __init__(self, driver):
        super(CryptoKittiesInCollectiblesButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('CryptoKitties')


class ViewInCryptoKittiesButton(BaseButton):
    def __init__(self, driver):
        super(ViewInCryptoKittiesButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('open-collectible-button')

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)

    def click(self):
        self.wait_for_element(30).click()
        self.driver.info('Tap on View in CryptoKitties')
        return self.navigate()


class BackupRecoveryPhrase(BaseButton):
    def __init__(self, driver):
        super(BackupRecoveryPhrase, self).__init__(driver)
        self.locator = self.Locator.text_selector('Backup your seed phrase')

    def navigate(self):
        from views.profile_view import ProfileView
        return ProfileView(self.driver)


class BackupRecoveryPhraseWarningText(BaseButton):
    def __init__(self, driver):
        super(BackupRecoveryPhraseWarningText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('back-up-your-seed-phrase-warning')


class MultiaccountMoreOptions(BaseButton):
    def __init__(self, driver):
        super(MultiaccountMoreOptions, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('accounts-more-options')


class AccountElementButton(BaseButton):
    def __init__(self, driver, account_name):
        super(AccountElementButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.HorizontalScrollView//*[@text='%s']/.." % account_name)

    def color_matches(self, expected_color_image_name: str):
        amount_text = BaseText(self.driver)
        amount_text.locator = amount_text.Locator.xpath_selector(self.locator.value + "//*[@text='0 USD']")
        return amount_text.is_element_image_equals_template(expected_color_image_name)


class SendTransactionButton(BaseButton):

    def __init__(self, driver):
        super(SendTransactionButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Send']")

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)

    def click(self):
        self.find_element().click()
        self.driver.info('Tap on %s' % self.name)
        return self.navigate()


class ReceiveTransactionButton(BaseButton):

    def __init__(self, driver):
        super(ReceiveTransactionButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Receive']")

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)


class AddCustomTokenButton(BaseButton):
    def __init__(self, driver):
        super(AddCustomTokenButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Add custom token']")

    def navigate(self):
        from views.add_custom_token_view import AddCustomTokenView
        return AddCustomTokenView(self.driver)


class AddAccountButton(BaseButton):
    def __init__(self, driver):
        super(AddAccountButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Add account')


class AddAnAccountButton(BaseButton):
    def __init__(self, driver):
        super(AddAnAccountButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Add an account')


class GenerateNewAccountButton(BaseButton):
    def __init__(self, driver):
        super(GenerateNewAccountButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Generate a new key')


class EnterYourPasswordInput(BaseEditBox):
    def __init__(self, driver):
        super(EnterYourPasswordInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='Enter your password']/following-sibling::android.widget.EditText")


class GenerateAccountButton(BaseButton):
    def __init__(self, driver):
        super(GenerateAccountButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Generate key')


class AccountNameInput(BaseEditBox):
    def __init__(self, driver):
        super(AccountNameInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Account name']"
                                                   "/following-sibling::android.view.ViewGroup/android.widget.EditText")


class AccountColorButton(BaseButton):
    def __init__(self, driver):
        super(AccountColorButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Account color']"
                                                   "/following-sibling::android.view.ViewGroup[1]")

    def select_color_by_position(self, position: int):
        self.click()
        self.driver.find_element_by_xpath(
            "//*[@text='Cancel']/../preceding-sibling::android.widget.ScrollView/*/*[%s]" % position).click()


class FinishButton(BaseButton):
    def __init__(self, driver):
        super(FinishButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Finish')

class AccountSettingsButton(BaseButton):
    def __init__(self, driver):
        super(AccountSettingsButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Account settings')

class ApplySettingsButton(BaseButton):
    def __init__(self, driver):
        super(ApplySettingsButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Apply')

class WalletView(BaseView):
    def __init__(self, driver):
        super(WalletView, self).__init__(driver)
        self.driver = driver

        self.send_transaction_button = SendTransactionButton(self.driver)
        self.transaction_history_button = TransactionHistoryButton(self.driver)
        self.usd_total_value = UsdTotalValueText(self.driver)

        self.send_transaction_request = SendTransactionRequestButton(self.driver)
        self.receive_transaction_button = ReceiveTransactionButton(self.driver)

        self.send_request_button = SendRequestButton(self.driver)
        self.options_button = OptionsButton(self.driver)
        self.manage_assets_button = ManageAssetsButton(self.driver)
        self.stt_check_box = STTCheckBox(self.driver)

        self.qr_code_image = QRCodeImage(self.driver)
        self.address_text = AddressText(self.driver)

        self.set_up_button = SetUpButton(self.driver)
        self.sign_in_phrase = SignInPhraseText(self.driver)
        self.remind_me_later_button = RemindMeLaterButton(self.driver)

        self.total_amount_text = TotalAmountText(self.driver)
        self.currency_text = CurrencyText(self.driver)
        self.backup_recovery_phrase = BackupRecoveryPhrase(self.driver)
        self.backup_recovery_phrase_warning_text = BackupRecoveryPhraseWarningText(self.driver)

        self.add_custom_token_button = AddCustomTokenButton(self.driver)

        # elements for multiaccount
        self.multiaccount_more_options = MultiaccountMoreOptions(self.driver)
        self.accounts_status_account = AccountElementButton(self.driver, account_name="Status account")
        self.collectibles_button = CollectiblesButton(self.driver)
        self.cryptokitties_in_collectibles_button = CryptoKittiesInCollectiblesButton(self.driver)
        self.view_in_cryptokitties_button = ViewInCryptoKittiesButton(self.driver)
        self.set_currency_button = SetCurrencyButton(self.driver)
        self.add_account_button = AddAccountButton(self.driver)
        self.add_an_account_button = AddAnAccountButton(self.driver)
        self.generate_new_account_button = GenerateNewAccountButton(self.driver)
        self.enter_your_password_input = EnterYourPasswordInput(self.driver)
        self.generate_account_button = GenerateAccountButton(self.driver)
        self.account_name_input = AccountNameInput(self.driver)
        self.account_color_button = AccountColorButton(self.driver)
        self.finish_button = FinishButton(self.driver)

        # individual account settings
        self.account_settings_button = AccountSettingsButton(self.driver)
        self.apply_settings_button = ApplySettingsButton(self.driver)
        self.account_options_button = AccountOptionsButton(self.driver, account_name='Status account')

    def get_usd_total_value(self):
        import re
        return float(re.sub('[~,]', '', self.usd_total_value.text))



    def get_asset_amount_by_name(self, asset: str):
        asset_value = AssetText(self.driver, asset)
        asset_value.scroll_to_element()
        return float(asset_value.text)

    def verify_currency_balance(self, expected_rate: int, errors: list):
        usd = self.get_usd_total_value()
        eth = self.get_asset_amount_by_name('ETHro')
        expected_usd = round(eth * expected_rate, 2)
        percentage_diff = abs((usd - expected_usd) / ((usd + expected_usd) / 2)) * 100
        if percentage_diff > 2:
            errors.append('Difference between current (%s) and expected (%s) USD balance > 2%%!!' % (usd, expected_usd))
        else:
            self.driver.info('Current USD balance %s is ok' % usd)


    def wait_balance_is_equal_expected_amount(self, asset ='ETHro', expected_balance=0.1, wait_time=300):
        counter = 0
        while True:
            if counter >= wait_time:
                self.driver.fail('Balance is not changed during %s seconds!' % wait_time)
            elif self.get_asset_amount_by_name(asset) != expected_balance:
                counter += 10
                time.sleep(10)
                self.swipe_down()
                self.driver.info('Waiting %s seconds for %s balance update' % (counter,asset))
            else:
                self.driver.info('Transaction received, balance updated!')
                return

    def wait_balance_is_changed(self, asset ='ETHro', initial_balance=0, wait_time=300):
        counter = 0
        while True:
            if counter >= wait_time:
                self.driver.fail('Balance is not changed during %s seconds!' % wait_time)
            elif self.asset_by_name(asset).is_element_present() and self.get_asset_amount_by_name(asset) == initial_balance:
                counter += 10
                time.sleep(10)
                self.swipe_down()
                self.driver.info('Waiting %s seconds for %s to update' % (counter,asset))
            elif not self.asset_by_name(asset).is_element_present(10):
                counter += 10
                time.sleep(10)
                self.swipe_down()
                self.driver.info('Waiting %s seconds for %s to display asset' % (counter, asset))
            else:
                self.driver.info('Balance is updated!')
                return


    def get_sign_in_phrase(self):
        return ' '.join([element.text for element in self.sign_in_phrase.find_elements()])

    def set_up_wallet(self):
        phrase = self.sign_in_phrase.string
        self.ok_got_it_button.click()
        return phrase

    def get_wallet_address(self, account_name="Status account"):
        self.wallet_account_by_name(account_name).click()
        self.receive_transaction_button.click()
        address = self.address_text.text
        self.back_button.click()
        return address

    def wallet_account_by_name(self, account_name):
        return AccountElementButton(self.driver, account_name)

    def asset_by_name(self, asset_name):
        return AssetTextElement(self.driver, asset_name)

    def asset_checkbox_by_name(self, asset_name):
        return AssetCheckBox(self.driver, asset_name)

    def account_options_by_name(self, account_name):
        return AccountOptionsButton(self.driver, account_name)

    def select_asset(self, *args):
        self.multiaccount_more_options.click()
        self.manage_assets_button.click()
        for asset in args:
            self.asset_checkbox_by_name(asset).click()
        self.cross_icon.click()

    def send_transaction(self, **kwargs):
        send_transaction_view = self.send_transaction_button.click()
        send_transaction_view.select_asset_button.click()
        asset_name = kwargs.get('asset_name', 'ETHro').upper()
        asset_button = send_transaction_view.asset_by_name(asset_name)
        send_transaction_view.select_asset_button.click_until_presence_of_element(asset_button)
        asset_button.click()
        send_transaction_view.amount_edit_box.click()

        transaction_amount = str(kwargs.get('amount', send_transaction_view.get_unique_amount()))

        send_transaction_view.amount_edit_box.set_value(transaction_amount)
        send_transaction_view.confirm()
        send_transaction_view.chose_recipient_button.click()

        recipient = kwargs.get('recipient')

        if '0x' in recipient:
            send_transaction_view.enter_recipient_address_button.click()
            send_transaction_view.enter_recipient_address_input.set_value(recipient)
            send_transaction_view.done_button.click()
        else:
            send_transaction_view.recent_recipients_button.click()
            recent_recipient = send_transaction_view.element_by_text(recipient)
            send_transaction_view.recent_recipients_button.click_until_presence_of_element(recent_recipient)
            recent_recipient.click()
        if kwargs.get('sign_transaction', True):
            send_transaction_view.sign_transaction_button.click()
            send_transaction_view.sign_transaction()

    def receive_transaction(self, **kwargs):
        self.receive_transaction_button.click()
        send_transaction_view = self.send_transaction_request.click()
        send_transaction_view.select_asset_button.click()
        asset_name = kwargs.get('asset_name', 'ETHro').upper()
        asset_button = send_transaction_view.asset_by_name(asset_name)
        send_transaction_view.select_asset_button.click_until_presence_of_element(asset_button)
        asset_button.click()
        send_transaction_view.amount_edit_box.click()

        transaction_amount = str(kwargs.get('amount')) if kwargs.get('amount') else \
            send_transaction_view.get_unique_amount()

        send_transaction_view.amount_edit_box.set_value(transaction_amount)
        send_transaction_view.confirm()
        send_transaction_view.chose_recipient_button.click()

        recipient = kwargs.get('recipient')
        send_transaction_view.recent_recipients_button.click()
        recent_recipient = send_transaction_view.element_by_text(recipient)
        send_transaction_view.recent_recipients_button.click_until_presence_of_element(recent_recipient)
        recent_recipient.click()
        self.send_request_button.click()

    def collectible_amount_by_name(self, name):
        elm = CollectibleTextElement(self.driver, name)
        elm.scroll_to_element()
        return elm.text

    def set_currency(self, desired_currency='EUR'):
        """
        :param desired_currency: defines a currency designator which is expressed by ISO 4217 code
        """
        self.multiaccount_more_options.click_until_presence_of_element(self.set_currency_button)
        self.set_currency_button.click()
        desired_currency = self.element_by_text_part(desired_currency)
        desired_currency.scroll_to_element()
        desired_currency.click()

    def get_account_by_name(self, account_name: str):
        return AccountElementButton(self.driver, account_name)

    def add_account(self, account_name: str, password: str = common_password):
        self.add_account_button.click()
        self.add_an_account_button.click()
        self.generate_new_account_button.click()
        self.generate_account_button.click()
        self.enter_your_password_input.send_keys(password)
        self.generate_account_button.click()
        self.account_name_input.send_keys(account_name)
        self.finish_button.click()
