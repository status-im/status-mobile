from views.base_view import BaseView
import pytest
from views.base_element import *


class FirstAccountButton(BaseButton):

    def __init__(self, driver):
        super(FirstAccountButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.ScrollView//android.widget.TextView")


class PasswordInput(BaseEditBox):

    def __init__(self, driver):
        super(PasswordInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Password']")


class SignInButton(BaseButton):

    def __init__(self, driver):
        super(SignInButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Sign in']")


class RecoverAccessButton(BaseButton):

    def __init__(self, driver):
        super(RecoverAccessButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Recover access']")

    def navigate(self):
        from views.recover_access_view import RecoverAccessView
        return RecoverAccessView(self.driver)


class SignInView(BaseView):

    def __init__(self, driver):
        super(SignInView, self).__init__(driver)
        self.driver = driver

        self.first_account_button = FirstAccountButton(self.driver)
        self.password_input = PasswordInput(self.driver)
        self.sign_in_button = SignInButton(self.driver)
        self.recover_access_button = RecoverAccessButton(self.driver)
