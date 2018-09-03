import time
import pytest
import numpy
from tests.base_test_case import SingleDeviceTestCase
from random import randint
from views.sign_in_view import SignInView

stop = int(time.time()) + int(pytest.config.getoption('running_time'))


@pytest.mark.chatbot
class TestChatBot(SingleDeviceTestCase):

    def setup_method(self, method, max_duration=10800):
        super(TestChatBot, self).setup_method(method, max_duration=10800)

    public_keys = pytest.config.getoption('public_keys').split()
    repeats = 24 / len(public_keys)

    @pytest.mark.chatbot
    @pytest.mark.parametrize('key', numpy.repeat(public_keys, repeats))
    def test_one_to_one_chatbot(self, key):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat = home.add_contact(key)

        message_text = 'test'
        chat.chat_message_input.send_keys(message_text)
        chat.send_message_button.click()

        message = 'Message # %s, sent by e2e test, to %s '
        counter = 0
        while int(time.time()) < stop:
            counter += 1
            time.sleep(randint(60, 120))
            chat.chat_message_input.send_keys(message % (counter, key))
            chat.send_message_button.click()
