from tests import base_user
from tests.base_test_case import BaseTestCase
from views.sign_in_view import SignInView


class TestChats(BaseTestCase):

    def test_start_new_chat(self):
        sign_in = SignInView()
        home = sign_in.create_account()
        home.plus_button.click()
        home.contact_code_input.input_value(base_user['public_key'])
        home.start_chat_button.click()
        home.find_text(base_user['username'])
