import os

from views.base_element import BaseElement, InputField
from views.base_view import BaseView

IMAGES_PATH = os.path.join(os.path.dirname(__file__), 'images/sign_in_view')


class CreateAccountButton(BaseElement):
    def __init__(self):
        super(CreateAccountButton, self).__init__(IMAGES_PATH + '/create_account.png')


class PasswordInput(InputField):
    def __init__(self):
        super(PasswordInput, self).__init__(IMAGES_PATH + '/password_input.png')


class ConfirmPasswordInput(InputField):
    def __init__(self):
        super(ConfirmPasswordInput, self).__init__(IMAGES_PATH + '/confirm_password_input.png')


class UserNameInput(InputField):
    def __init__(self):
        super(UserNameInput, self).__init__(IMAGES_PATH + '/username_input.png')


class NextButton(InputField):
    def __init__(self):
        super(NextButton, self).__init__(IMAGES_PATH + '/next_button.png')


class SignInView(BaseView):
    def __init__(self):
        super(SignInView, self).__init__()
        self.create_account_button = CreateAccountButton()
        self.password_input = PasswordInput()
        self.confirm_password_input = ConfirmPasswordInput()
        self.username_input = UserNameInput()
        self.next_button = NextButton()
