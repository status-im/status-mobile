import time

import pytest
from selenium.common.exceptions import NoSuchElementException

from views.base_element import *
from views.base_view import BaseView


class FirstRecipient(BaseButton):
    def __init__(self, driver):
        super(FirstRecipient, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Choose recipient']/.."
                                                   "//android.widget.ImageView[@content-desc='chat-icon']")


class SignTransactionButton(BaseButton):
    def __init__(self, driver):
        super(SignTransactionButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='SIGN TRANSACTION']")


class SignLaterButton(BaseButton):
    def __init__(self, driver):
        super(SignLaterButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='SIGN LATER']")


class AmountEditBox(BaseEditBox, BaseButton):

    def __init__(self, driver):
        super(AmountEditBox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Amount']/..//android.widget.EditText")


class PasswordInput(BaseEditBox):
    def __init__(self, driver):
        super(PasswordInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Password']")


class EnterPasswordInput(BaseEditBox):
    def __init__(self, driver):
        super(EnterPasswordInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.EditText[@NAF='true']")


class ConfirmButton(BaseButton):
    def __init__(self, driver):
        super(ConfirmButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='CONFIRM']")


class GotItButton(BaseButton):
    def __init__(self, driver):
        super(GotItButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='GOT IT']")


class ChooseRecipientButton(BaseButton):

    def __init__(self, driver):
        super(ChooseRecipientButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Choose recipient...']")


class ChooseFromContactsButton(BaseButton):
    def __init__(self, driver):
        super(ChooseFromContactsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Choose From Contacts']")


class SendTransactionView(BaseView):
    def __init__(self, driver):
        super(SendTransactionView, self).__init__(driver)

        self.chose_recipient_button = ChooseRecipientButton(self.driver)
        self.chose_from_contacts_button = ChooseFromContactsButton(self.driver)
        self.first_recipient_button = FirstRecipient(self.driver)

        self.amount_edit_box = AmountEditBox(self.driver)
        self.sign_transaction_button = SignTransactionButton(self.driver)
        self.sign_later_button = SignLaterButton(self.driver)
        self.confirm_button = ConfirmButton(self.driver)
        self.password_input = PasswordInput(self.driver)
        self.enter_password_input = EnterPasswordInput(self.driver)
        self.got_it_button = GotItButton(self.driver)
