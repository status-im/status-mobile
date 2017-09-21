from views.base_view import BaseViewObject
import pytest
from views.base_element import *


class FirstAccountButton(BaseButton):

    def __init__(self, driver):
        super(FirstAccountButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.ScrollView//android.widget.TextView")


class PasswordInput(BaseEditBox):

    def __init__(self, driver):
        super(PasswordInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.EditText")


class SignInButton(BaseButton):

    def __init__(self, driver):
        super(SignInButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Sign in']")


class LoginView(BaseViewObject):

    def __init__(self, driver):
        super(LoginView, self).__init__(driver)
        self.driver = driver

        self.first_account_button = FirstAccountButton(self.driver)
        self.password_input = PasswordInput(self.driver)
        self.sign_in_button = SignInButton(self.driver)
