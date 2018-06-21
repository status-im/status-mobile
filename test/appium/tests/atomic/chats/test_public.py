import random
import string

from tests import marks
from tests.base_test_case import MultipleDeviceTestCase
from views.sign_in_view import SignInView


@marks.chat
class TestMessagesPublicChat(MultipleDeviceTestCase):

    @marks.skip
    @marks.testrail_case_id(1383)
    def test_public_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        users = list()
        chats = list()
        chat_name = ''.join(random.choice(string.ascii_lowercase) for _ in range(7))
        for sign_in in device_1, device_2:
            users.append(sign_in.create_user())
            home = sign_in.get_home_view()
            chats.append(home.join_public_chat(chat_name))
        chat_1, chat_2 = chats[0], chats[1]

        if chat_1.connection_status.text != 'Fetching messages...':
            self.errors.append("'Fetching messages...' status is not shown")

        message = 'hello'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()

        chat_2.verify_message_is_under_today_text(message, self.errors)
