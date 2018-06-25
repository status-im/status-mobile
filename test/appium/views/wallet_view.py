from tests import info
import time
import pytest
from views.base_view import BaseView
from views.base_element import BaseButton, BaseText


class SendButton(BaseButton):

    def __init__(self, driver):
        super(SendButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('send-transaction-button')

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)


class RequestButton(BaseButton):

    def __init__(self, driver):
        super(RequestButton, self).__init__(driver)
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


class TransactionsButton(BaseButton):

    def __init__(self, driver):
        super(TransactionsButton, self).__init__(driver)
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
        self.locator = self.Locator.xpath_selector("//*[@text='ETH']/../android.widget.TextView[1]")


class UsdTotalValueText(BaseText):
    def __init__(self, driver):
        super(UsdTotalValueText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='USD']/../android.widget.TextView[1]")


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


class WalletView(BaseView):
    def __init__(self, driver):
        super(WalletView, self).__init__(driver)
        self.driver = driver

        self.send_button = SendButton(self.driver)
        self.transactions_button = TransactionsButton(self.driver)
        self.eth_asset = EthAssetText(self.driver)
        self.usd_total_value = UsdTotalValueText(self.driver)

        self.send_transaction_request = SendTransactionRequestButton(self.driver)
        self.request_button = RequestButton(self.driver)

        self.send_request_button = SendRequestButton(self.driver)
        self.options_button = OptionsButton(self.driver)
        self.manage_assets_button = ManageAssetsButton(self.driver)
        self.stt_check_box = STTCheckBox(self.driver)
        self.done_button = DoneButton(self.driver)

        self.qr_code_image = QRCodeImage(self.driver)
        self.address_text = AddressText(self.driver)

        self.set_up_button = SetUpButton(self.driver)
        self.sign_in_phrase = SignInPhraseText(self.driver)

    def get_usd_total_value(self):
        import re
        return float(re.sub('[$,]', '', self.usd_total_value.text))

    def get_eth_value(self):
        return float(self.eth_asset.text)

    def verify_currency_balance(self, expected_rate: int, errors: list):
        usd = self.get_usd_total_value()
        eth = self.get_eth_value()
        expected_usd = round(eth * expected_rate, 2)
        percentage_diff = abs((usd - expected_usd) / ((usd + expected_usd) / 2)) * 100
        if percentage_diff > 2:
            errors.append('Difference between current (%s) and expected (%s) USD balance > 2%%!!' % (usd, expected_usd))
        else:
            info('Current USD balance %s is ok' % usd)

    def wait_balance_changed_on_wallet_screen(self, initial_balance=0, wait_time=300):
        counter = 0
        while True:
            if counter >= wait_time:
                pytest.fail('Balance is not changed during %s seconds!' % wait_time)
            elif self.get_eth_value() == initial_balance:
                counter += 10
                time.sleep(10)
                self.swipe_down()
                info('Waiting %s seconds for ETH update' % counter)
            else:
                info('Transaction received, balance updated!')
                return

    def get_sign_in_phrase(self):
        return ' '.join([element.text for element in self.sign_in_phrase.find_elements()])

    def set_up_wallet(self):
        self.set_up_button.click()
        phrase = self.get_sign_in_phrase()
        self.done_button.click()
        self.yes_button.click()
        return phrase
