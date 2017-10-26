import logging

from views.base_view import BaseViewObject
from views.base_element import BaseButton, BaseEditBox, BaseText


class SendButton(BaseButton):

    def __init__(self, driver):
        super(SendButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='SEND']")


class AmountEditBox(BaseEditBox, BaseButton):

    def __init__(self, driver):
        super(AmountEditBox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Amount']/..//android.widget.EditText")


class ChooseRecipientButton(BaseButton):

    def __init__(self, driver):
        super(ChooseRecipientButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Choose recipient...']")


class TransactionsButton(BaseButton):

    def __init__(self, driver):
        super(TransactionsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[4]')

    def navigate(self):
        from views.transactions import TransactionsViewObject
        return TransactionsViewObject(self.driver)


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


class WalletViewObject(BaseViewObject):
    def __init__(self, driver):
        super(WalletViewObject, self).__init__(driver)
        self.driver = driver

        self.send_button = SendButton(self.driver)
        self.amount_edit_box = AmountEditBox(self.driver)
        self.chose_recipient_button = ChooseRecipientButton(self.driver)
        self.chose_from_contacts_button = ChooseFromContactsButton(self.driver)
        self.transactions_button = TransactionsButton(self.driver)
        self.eth_asset = EthAssetText(self.driver)
        self.usd_total_value = UsdTotalValueText(self.driver)

    def get_usd_total_value(self):
        return float(self.usd_total_value.text)

    def get_eth_value(self):
        return float(self.eth_asset.text)

    def verify_eth_rate(self, expected_rate: int, errors: list):
        usd = self.get_usd_total_value()
        eth = self.get_eth_value()
        current_rate = usd / eth
        if round(current_rate, 2) != expected_rate:
            errors.append('Current ETH rate %s is not equal to the expected %s' % (current_rate, expected_rate))
        logging.info('Current ETH rate %s is ok' % current_rate)
