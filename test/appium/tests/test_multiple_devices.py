import pytest
from selenium.common.exceptions import NoSuchElementException, TimeoutException
from tests import api_requests
from tests.base_test_case import MultipleDeviceTestCase
from tests import transaction_users, get_current_time
from views.console_view import ConsoleView


@pytest.mark.all
class TestMultipleDevices(MultipleDeviceTestCase):

    @pytest.mark.chat
    def test_one_to_one_chat(self):
        self.create_drivers(2)
        device_1, device_2 = \
            ConsoleView(self.drivers[0]),  ConsoleView(self.drivers[1])
        for console in device_1, device_2:
            console.create_user()
            console.back_button.click()
        device_1_home, device_2_home = device_1.get_home_view(), device_2.get_home_view()
        device_1_public_key = device_1_home.get_public_key()
        device_2_home.add_contact(device_1_public_key)
        device_2_chat = device_2_home.get_chat_view()
        message_1 = 'SOMETHING'
        device_2_chat.chat_message_input.send_keys(message_1)
        device_2_chat.send_message_button.click()
        message_2 = 'another SOMETHING'
        device_1_home.home_button.click()
        device_1_home.find_full_text(message_1)
        device_1_home.element_by_text(message_1, 'button').click()
        device_1_chat = device_1_home.get_chat_view()
        device_1_chat.chat_message_input.send_keys(message_2)
        device_1_chat.send_message_button.click()
        device_2_chat.find_full_text(message_2)

    @pytest.mark.chat
    def test_group_chat_send_receive_messages_and_remove_user(self):
        self.create_drivers(2)
        device_1, device_2 = \
            ConsoleView(self.drivers[0]),  ConsoleView(self.drivers[1])
        for console in device_1, device_2:
            console.create_user()
            console.back_button.click()
        device_1_home = device_1.get_home_view()
        device_2_home = device_2.get_home_view()
        device_1_public_key = device_1_home.get_public_key()
        device_2_home.add_contact(device_1_public_key)
        device_2_chat = device_2_home.get_chat_view()
        device_1_user_name = device_2_chat.user_name_text.text
        device_2_home.get_back_to_home_view()
        chat_name = 'new_chat'
        message_1 = 'first SOMETHING'
        message_2 = 'second SOMETHING'
        message_3 = 'third SOMETHING'
        device_2_home.create_group_chat([device_1_user_name], chat_name)

        # send_and_receive_messages
        device_2_chat.chat_message_input.send_keys(message_1)
        device_2_chat.send_message_button.click()
        device_1.home_button.click()
        device_1_home.find_full_text(message_1)
        device_1_home.element_by_text(chat_name, 'button').click()
        group_chat_d1 = device_1_home.get_chat_view()
        group_chat_d1.chat_message_input.send_keys(message_2)
        group_chat_d1.send_message_button.click()
        device_2_chat.find_full_text(message_2)

        # remove user
        device_2_chat.chat_options.click()
        device_2_chat.chat_settings.click()
        for _ in range(2):
            try:
                device_2_chat.user_options.click()
            except (NoSuchElementException, TimeoutException):
                pass
        device_2_chat.remove_button.click()
        device_2_chat.confirm()
        device_2.back_button.click()

        # verify removed user receives no messages
        device_2_chat.chat_message_input.send_keys(message_3)
        device_2_chat.send_message_button.click()
        group_chat_d1.find_text_part("removed you from group chat")
        if group_chat_d1.element_by_text(message_3, 'text').is_element_present(20):
            pytest.fail('Message is shown for the user which has been removed from the GroupChat', False)
