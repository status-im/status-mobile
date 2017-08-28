import pytest
from tests.basetestcase import MultiplyDeviceTestCase
from tests.preconditions import set_chat_for_users_from_scratch
from views.home import HomeView


@pytest.mark.sanity
class TestMultiplyDevices(MultiplyDeviceTestCase):

    def test_private_chat(self):

        device_1, device_2 = HomeView(self.driver_1), HomeView(self.driver_2)
        set_chat_for_users_from_scratch(device_1, device_2)

        device_1.back_button.click()
        chats_d1 = device_1.get_chats()
        chats_d1.profile_button.click()
        profile_d1 = chats_d1.profile_icon.click()
        key = profile_d1.public_key_text.get_key()

        device_2.back_button.click()
        chats_d2 = device_2.get_chats()
        chats_d2.plus_button.click()
        chats_d2.add_new_contact.click()
        chats_d2.public_key_edit_box.send_keys(key)
        chats_d2.confirm()
        chats_d2.confirm_public_key_button.click()

        chats_d2.chat_message_input.send_keys('SOMETHING')
        chats_d2.send_message_button.click()

        profile_d1.back_button.click()
        profile_d1.find_text('SOMETHING')
