from views.base_view import BaseViewObject
import pytest
from views.base_element import *


class SendButton(BaseButton):

    def __init__(self, driver):
        super(SendButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='SEND']")


class AmountEditBox(BaseEditBox, BaseButton):

    def __init__(self, driver):
        super(AmountEditBox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='0.000']")


class ChooseRecipientButton(BaseButton):

    def __init__(self, driver):
        super(ChooseRecipientButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Choose recipient...']")


class TransactionsIcon(BaseButton):

    def __init__(self, driver):
        super(TransactionsIcon, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[4]')


class UnsignedTab(BaseButton):

    def __init__(self, driver):
        super(UnsignedTab, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='UNSIGNED']")

    class SignButton(BaseButton):

        def __init__(self, driver):
            super(UnsignedTab.SignButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//*[@text='SIGN']")


class ChooseFromContactsButton(BaseButton):

    def __init__(self, driver):
        super(ChooseFromContactsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Choose From Contacts']")


class WalletViewObject(BaseViewObject):

    def __init__(self, driver):
        super(WalletViewObject, self).__init__(driver)
        self.driver = driver

        self.send_button = SendButton(self.driver)
        self.amount_edit_box = AmountEditBox(self.driver)
        self.chose_recipient_button = ChooseRecipientButton(self.driver)
        self.chose_from_contacts_button = ChooseFromContactsButton(self.driver)
        self.unsigned_tab = UnsignedTab(self.driver)
        self.sign_button = UnsignedTab.SignButton(self.driver)
        self.transactions_icon = TransactionsIcon(self.driver)
