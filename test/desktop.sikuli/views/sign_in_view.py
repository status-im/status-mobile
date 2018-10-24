import org.sikuli.script.SikulixForJython
import pytest
from sikuli import *
import os

from _pytest.runner import Failed

from views.base_element import BaseElement, InputField
from views.base_view import BaseView
from views.home_view import HomeView

IMAGES_PATH = os.path.join(os.path.dirname(__file__), 'images/sign_in_view')


class CreateAccountButton(BaseElement):
    def __init__(self):
        super(CreateAccountButton, self).__init__(IMAGES_PATH + '/create_account.png')

    def find_element(self, log=False):
        try:
            super(CreateAccountButton, self).find_element(log=log)
        except Failed:
            self.screenshot = IMAGES_PATH + '/create_new_account.png'
            super(CreateAccountButton, self).find_element(log=log)


class SignInView(BaseView):
    def __init__(self):
        super(SignInView, self).__init__()
        self.create_account_button = CreateAccountButton()
        self.i_have_account_button = BaseElement(IMAGES_PATH + '/i_have_account.png')
        self.other_accounts_button = BaseElement(IMAGES_PATH + '/other_accounts.png')
        self.privacy_policy_button = BaseElement(IMAGES_PATH + 'privacy_policy_button.png')
        self.create_password_input = InputField(IMAGES_PATH + '/create_password_input.png')
        self.confirm_password_input = InputField(IMAGES_PATH + '/confirm_password_input.png')
        self.username_input = InputField(IMAGES_PATH + '/username_input.png')
        self.next_button = BaseElement(IMAGES_PATH + '/next_button.png')
        self.recovery_phrase_input = InputField(IMAGES_PATH + '/recovery_phrase_input.png')
        self.recover_password_input = InputField(IMAGES_PATH + '/recover_password_input.png')
        self.sign_in_button = BaseElement(IMAGES_PATH + '/sign_in_button.png')
        self.password_input = InputField(IMAGES_PATH + '/password_input.png')

    def create_account(self, password='qwerty', username='test'):
        self.create_account_button.click()
        self.create_password_input.input_value(password)
        self.next_button.click()
        self.confirm_password_input.input_value(password)
        self.next_button.click()
        self.username_input.input_value(username)
        self.next_button.click()
        self.home_button.find_element()
        return HomeView()

    def recover_access(self, passphrase):
        self.i_have_account_button.click()
        self.recovery_phrase_input.send_keys(passphrase)
        self.recover_password_input.send_keys('123456')
        self.sign_in_button.click()
        self.home_button.find_element()
        return HomeView()
