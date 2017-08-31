import pytest
from tests.basetestcase import MultiplyDeviceTestCase
from tests.preconditions import set_password_as_new_user
from views.home import HomeView
from views.chats import UserNameText


@pytest.mark.sanity
class TestDev(MultiplyDeviceTestCase):

    def test_group_chat(self):
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

        user_d1_name = chats_d2.user_name_text.text

        device_2.back_button.click()
        chats_d2.new_group_chat_button.click()

        profile_d1.back_button.click()

        user_contact = UserNameText.UserContactByName(self.driver_2, user_d1_name)
        user_contact.scroll_to_element()
        user_contact.click()

        chats_d2.next_button.click()
        chats_d2.name_edit_box.send_keys('new_chat')
        chats_d2.save_button.click()

        chats_d2.chat_message_input.send_keys('SOMETHING')
        chats_d2.send_message_button.click()

        profile_d1.find_text('SOMETHING')
