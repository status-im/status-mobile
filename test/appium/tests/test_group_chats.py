import pytest
from tests.basetestcase import MultiplyDeviceTestCase
from tests.preconditions import set_password_as_new_user
from views.home import HomeView


@pytest.mark.all
class TestGroupChats(MultiplyDeviceTestCase):

    @pytest.mark.chat
    def test_group_chat_send_receive_messages_and_remove_user(self):

        device_1, device_2 = HomeView(self.driver_1), \
                             HomeView(self.driver_2)
        set_password_as_new_user(device_2, device_1)

        device_1.back_button.click()
        chats_d1 = device_1.get_chats()
        chats_d1.profile_button.click()
        profile_d1 = chats_d1.profile_icon.click()
        key = profile_d1.public_key_text.text

        device_2.back_button.click()
        chats_d2 = device_2.get_chats()
        chats_d2.plus_button.click()
        chats_d2.add_new_contact.click()
        chats_d2.public_key_edit_box.send_keys(key)
        chats_d2.confirm()
        chats_d2.confirm_public_key_button.click()
        user_name_d1 = chats_d2.user_name_text.text

        device_2.back_button.click()
        chats_d2.new_group_chat_button.click()

        user_contact = chats_d2.element_by_text(user_name_d1, 'button')
        user_contact.scroll_to_element()
        user_contact.click()
        chats_d2.next_button.click()

        chat_name = 'new_chat'
        message_1 = 'first SOMETHING'
        message_2 = 'second SOMETHING'
        message_3 = 'third SOMETHING'

        chats_d2.name_edit_box.send_keys(chat_name)
        chats_d2.save_button.click()

        # send_and_receive_messages

        chats_d2.chat_message_input.send_keys(message_1)
        chats_d2.send_message_button.click()

        profile_d1.back_button.click()
        chats_d1 = profile_d1.get_chats()
        chats_d1.find_full_text(message_1)
        group_chat_d1 = chats_d1.element_by_text(chat_name, 'button')
        group_chat_d1.click()

        chats_d2.chat_message_input.send_keys(message_2)
        chats_d2.send_message_button.click()

        chats_d1.find_full_text(message_2)

        # remove_user

        chats_d2.group_chat_options.click()
        chats_d2.chat_settings.click()
        chats_d2.confirm()
        chats_d2.user_options.click()
        chats_d2.remove_button.click()
        device_2.back_button.click()

        # chats_d2.find_full_text("You\'ve removed " + user_name_d1)

        chats_d2.chat_message_input.send_keys(message_3)
        chats_d2.send_message_button.click()

        chats_d1.find_text_part("removed you from group chat")
        message_text = chats_d1.element_by_text(message_3, 'text')
        if message_text.is_element_present(20):
            pytest.fail('Message is shown for the user which has been removed from the GroupChat', False)
