import random
import string

from tests import marks
from tests.base_test_case import MultipleDeviceTestCase
from views.sign_in_view import SignInView


@marks.chat
class TestMessagesPublicChat(MultipleDeviceTestCase):

    @marks.testrail_id(1383)
    def test_public_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1, username_2 = 'user_1', 'user_2'
        home_1, home_2 = device_1.create_user(username=username_1), device_2.create_user(username=username_2)
        public_key_2 = home_2.get_public_key()
        home_2.home_button.click()

        home_1.add_contact(public_key_2)
        home_1.get_back_to_home_view()

        public_chat_name = ''.join(random.choice(string.ascii_lowercase) for _ in range(7))
        chat_1, chat_2 = home_1.join_public_chat(public_chat_name), home_2.join_public_chat(public_chat_name)

        if chat_2.connection_status.text != 'Fetching messages...':
            self.errors.append("'Fetching messages...' status is not shown")

        message = 'hello'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()

        chat_2.verify_message_is_under_today_text(message, self.errors)
        if chat_2.chat_element_by_text(message).username.text != username_1:
            self.errors.append("Username '%s' is not shown next to the received message" % username_1)

        if chat_1.element_by_text(username_1).is_element_displayed():
            self.errors.append("Username '%s' is shown for the sender" % username_1)

        self.verify_no_errors()
