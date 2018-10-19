import time
from views.base_view import BaseView
from views.base_element import BaseButton, BaseText


class SendTransactionButton(BaseButton):

    def __init__(self, driver):
        super(SendTransactionButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('send-transaction-button')

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)


class ReceiveTransactionButton(BaseButton):

    def __init__(self, driver):
        super(ReceiveTransactionButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('receive-transaction-button')

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)


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
        self.locator = self.Locator.accessibility_id('transaction-history-button')

    def navigate(self):
        from views.transactions_view import TransactionsView
        return TransactionsView(self.driver)


class ChooseFromContactsButton(BaseButton):
    def __init__(self, driver):
        super(ChooseFromContactsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Choose From Contacts']")


class EthAssetText(BaseText):
    def __init__(self, driver):
        super(EthAssetText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('eth-asset-value-text')


class STTAssetText(BaseText):
    def __init__(self, driver):
        super(STTAssetText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('stt-asset-value-text')


class UsdTotalValueText(BaseText):
    def __init__(self, driver):
        super(UsdTotalValueText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('total-amount-value-text')


class SendTransactionRequestButton(BaseButton):
    def __init__(self, driver):
        super(SendTransactionRequestButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('sent-transaction-request-button')


class OptionsButton(BaseButton):
    def __init__(self, driver):
        super(OptionsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('options-menu-button')


class ManageAssetsButton(BaseButton):
    def __init__(self, driver):
        super(ManageAssetsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Manage Assets']")


class STTCheckBox(BaseButton):
    def __init__(self, driver):
        super(STTCheckBox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='STT']/../android.widget.CheckBox")


class DoneButton(BaseButton):
    def __init__(self, driver):
        super(DoneButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('done-button')


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
        self.locator = self.Locator.text_selector("LET’S GET SET UP")


class SignInPhraseText(BaseText):
    def __init__(self, driver):
        super(SignInPhraseText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[contains(@text,'phrase')]/preceding-sibling::*[1]/android.widget.TextView")

    @property
    def list(self):
        return [element.text for element in self.find_elements()]

    @property
    def string(self):
        return ' '.join(self.list)


class AssetTextElement(BaseText):
    def __init__(self, driver, asset_name):
        super(AssetTextElement, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('%s-asset-value-text' % asset_name.lower())


class AssetCheckBox(BaseButton):
    def __init__(self, driver, asset_name):
        super(AssetCheckBox, self).__init__(driver)
        self.asset_name = asset_name
        self.locator = self.Locator.xpath_selector("//*[@text='%s']/../android.widget.CheckBox" % self.asset_name)

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


class BackupRecoveryPhrase(BaseButton):
    def __init__(self, driver):
        super(BackupRecoveryPhrase, self).__init__(driver)
        self.locator = self.Locator.text_selector('Backup your recovery phrase')

    def navigate(self):
        from views.profile_view import ProfileView
        return ProfileView(self.driver)


class WalletView(BaseView):
    def __init__(self, driver):
        super(WalletView, self).__init__(driver)
        self.driver = driver

        self.send_transaction_button = SendTransactionButton(self.driver)
        self.transaction_history_button = TransactionHistoryButton(self.driver)
        self.eth_asset_value = EthAssetText(self.driver)
        self.stt_asset_value = STTAssetText(self.driver)
        self.usd_total_value = UsdTotalValueText(self.driver)

        self.send_transaction_request = SendTransactionRequestButton(self.driver)
        self.receive_transaction_button = ReceiveTransactionButton(self.driver)

        self.send_request_button = SendRequestButton(self.driver)
        self.options_button = OptionsButton(self.driver)
        self.manage_assets_button = ManageAssetsButton(self.driver)
        self.stt_check_box = STTCheckBox(self.driver)
        self.done_button = DoneButton(self.driver)

        self.qr_code_image = QRCodeImage(self.driver)
        self.address_text = AddressText(self.driver)

        self.set_up_button = SetUpButton(self.driver)
        self.sign_in_phrase = SignInPhraseText(self.driver)

        self.total_amount_text = TotalAmountText(self.driver)
        self.currency_text = CurrencyText(self.driver)
        self.backup_recovery_phrase = BackupRecoveryPhrase(self.driver)

    def get_usd_total_value(self):
        import re
        return float(re.sub('[~,]', '', self.usd_total_value.text))

    def get_eth_value(self):
        return float(self.eth_asset_value.text)

    def get_stt_value(self):
        self.stt_asset_value.scroll_to_element()
        return float(self.stt_asset_value.text)

    def verify_currency_balance(self, expected_rate: int, errors: list):
        usd = self.get_usd_total_value()
        eth = self.get_eth_value()
        expected_usd = round(eth * expected_rate, 2)
        percentage_diff = abs((usd - expected_usd) / ((usd + expected_usd) / 2)) * 100
        if percentage_diff > 2:
            errors.append('Difference between current (%s) and expected (%s) USD balance > 2%%!!' % (usd, expected_usd))
        else:
            self.driver.info('Current USD balance %s is ok' % usd)

    def wait_balance_changed_on_wallet_screen(self, expected_balance=0.1, wait_time=300):
        counter = 0
        while True:
            if counter >= wait_time:
                self.driver.info('Balance is not changed during %s seconds!' % wait_time)
                return
            elif self.get_eth_value() != expected_balance:
                counter += 10
                time.sleep(10)
                self.swipe_down()
                self.driver.info('Waiting %s seconds for ETH update' % counter)
            else:
                self.driver.info('Transaction received, balance updated!')
                return

    def get_sign_in_phrase(self):
        return ' '.join([element.text for element in self.sign_in_phrase.find_elements()])

    def set_up_wallet(self):
        self.set_up_button.click()
        phrase = self.sign_in_phrase.string
        self.done_button.click()
        self.yes_button.click()
        return phrase

    def get_wallet_address(self):
        self.receive_transaction_button.click()
        address = self.address_text.text
        self.back_button.click()
        return address

    def asset_by_name(self, asset_name):
        return AssetTextElement(self.driver, asset_name)

    def asset_checkbox_by_name(self, asset_name):
        return AssetCheckBox(self.driver, asset_name)
