import pytest

from tests import marks, group_chat_users, basic_user, camera_access_error_text
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from views.sign_in_view import SignInView


@marks.chat
class TestChatManagement(SingleDeviceTestCase):

    @marks.testrail_id(1428)
    def test_clear_history_one_to_one_chat(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        chat_view = home_view.add_contact(basic_user['public_key'])
        for _ in range(2):
            chat_view.chat_message_input.send_keys('test message')
            chat_view.send_message_button.click()
        chat_view.clear_history()
        if not chat_view.no_messages_in_chat.is_element_present():
            pytest.fail('Message history is shown')
        home_view.relogin()
        home_view.get_chat_with_user(basic_user['username']).click()
        if not chat_view.no_messages_in_chat.is_element_present():
            pytest.fail('Message history is shown after re-login')

    @marks.testrail_id(1395)
    @marks.smoke_1
    def test_swipe_to_delete_1_1_chat(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat = home.add_contact(group_chat_users['A_USER']['public_key'])
        chat.chat_message_input.send_keys('test message')
        chat.send_message_button.click()
        chat.get_back_to_home_view()
        home.get_chat_with_user(group_chat_users['A_USER']['username']).swipe_and_delete()
        self.driver.close_app()
        self.driver.launch_app()
        sign_in.accept_agreements()
        sign_in.sign_in()
        if home.get_chat_with_user(group_chat_users['A_USER']['username']).is_element_displayed():
            pytest.fail('Deleted 1-1 chat is present after relaunch app')

    @marks.testrail_id(3718)
    @marks.smoke_1
    def test_swipe_to_delete_public_chat(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat_name = home.get_public_chat_name()
        chat = home.join_public_chat(chat_name)
        message = 'test message'
        chat.chat_message_input.send_keys(message)
        chat.send_message_button.click()
        chat.get_back_to_home_view()
        home.get_chat_with_user('#' + chat_name).swipe_and_delete()
        profile = home.profile_button.click()
        profile.logout()
        sign_in.sign_in()
        if home.get_chat_with_user('#' + chat_name).is_element_displayed():
            self.errors.append('Deleted public chat is present after relogin')
        home.join_public_chat(chat_name)
        if chat.chat_element_by_text(message).is_element_displayed():
            self.errors.append('Chat history is shown')
        self.verify_no_errors()

    @marks.testrail_id(763)
    @marks.smoke_1
    def test_add_contact_by_pasting_public_key(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        public_key = group_chat_users['A_USER']['public_key']

        chat = home.join_public_chat(home.get_public_chat_name())
        chat.chat_message_input.send_keys(public_key)
        chat.send_message_button.click()
        chat.chat_element_by_text(public_key).long_press_element()
        chat.element_by_text('Copy to clipboard').click()
        chat.get_back_to_home_view()

        start_new_chat = home.plus_button.click()
        start_new_chat.start_new_chat_button.click()
        start_new_chat.public_key_edit_box.paste_text_from_clipboard()
        if start_new_chat.public_key_edit_box.text != public_key:
            pytest.fail('Public key is not pasted from clipboard')
        start_new_chat.confirm()
        start_new_chat.get_back_to_home_view()
        home.plus_button.click()
        start_new_chat.start_new_chat_button.click()
        if not start_new_chat.element_by_text(group_chat_users['A_USER']['username']).is_element_displayed():
            pytest.fail("List of contacts doesn't contain added user")

    @marks.testrail_id(3719)
    def test_delete_one_to_one_chat_via_delete_button(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat_view = home.add_contact(basic_user['public_key'])
        for _ in range(2):
            chat_view.chat_message_input.send_keys('test message')
            chat_view.send_message_button.click()
        chat_view.delete_chat()
        if home.get_chat_with_user(basic_user['username']).is_element_present(10):
            self.errors.append("One-to-one' chat is shown, but the chat has been deleted")
        home.relogin()
        if home.get_chat_with_user(basic_user['username']).is_element_present(10):
            self.errors.append("One-to-one' chat is shown after re-login, but the chat has been deleted")
        self.verify_no_errors()

    @marks.testrail_id(3720)
    def test_delete_public_chat_via_delete_button(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat_name = home.get_public_chat_name()
        public_chat = home.join_public_chat(chat_name)
        public_chat.chat_message_input.send_keys('test message')
        public_chat.send_message_button.click()
        public_chat.delete_chat()
        if home.element_by_text(chat_name).is_element_present(5):
            self.errors.append("Public chat '%s' is shown, but the chat has been deleted" % chat_name)
        home.relogin()
        if home.element_by_text(chat_name).is_element_present(5):
            self.errors.append("Public chat '%s' is shown after re-login, but the chat has been deleted" % chat_name)
        self.verify_no_errors()

    @marks.testrail_id(2172)
    def test_incorrect_contact_code_start_new_chat(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        start_new_chat = home.plus_button.click()
        start_new_chat.start_new_chat_button.click()
        start_new_chat.public_key_edit_box.set_value(group_chat_users['B_USER']['public_key'][:-1])
        start_new_chat.confirm()
        warning_text = start_new_chat.element_by_text('Please enter or scan a valid contact code or username')
        if not warning_text.is_element_displayed():
            pytest.fail('Error is not shown for invalid public key')

    @marks.testrail_id(2175)
    def test_deny_camera_access_scanning_contact_code(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        start_new_chat = home.plus_button.click()
        start_new_chat.start_new_chat_button.click()
        start_new_chat.scan_contact_code_button.click()
        start_new_chat.deny_button.click()
        start_new_chat.element_by_text(camera_access_error_text).wait_for_visibility_of_element(3)
        start_new_chat.ok_button.click()
        start_new_chat.scan_contact_code_button.click()
        start_new_chat.deny_button.wait_for_visibility_of_element(2)


@marks.chat
class TestChatManagementMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(3694)
    @marks.smoke_1
    def test_add_contact_from_public_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        chat_name = 'testaddcontact'
        chat_1, chat_2 = home_1.join_public_chat(chat_name), home_2.join_public_chat(chat_name)
        message = 'test message'

        chat_2.chat_message_input.send_keys(message)
        chat_2.send_message_button.click()
        chat_2.driver.quit()

        chat_element = chat_1.chat_element_by_text(message)
        chat_element.find_element()
        username = chat_element.username.text
        chat_element.member_photo.click()
        for element in [chat_1.contact_profile_picture, chat_1.add_to_contacts, chat_1.profile_send_message,
                        chat_1.profile_send_transaction, chat_1.public_key_text,
                        chat_1.element_by_text(username, 'text')]:
            if not element.is_element_displayed():
                self.errors.append('%s is not visible' % 'user name' if 'Base' in element.name else element.name)
        chat_1.add_to_contacts.click()
        if not chat_1.element_by_text('In contacts').is_element_displayed():
            self.errors.append("'Add to contacts' is not changed to 'In contacts'")

        chat_1.get_back_to_home_view()
        start_new_chat = home_1.plus_button.click()
        start_new_chat.start_new_chat_button.click()
        if not start_new_chat.element_by_text(username).is_element_displayed():
            self.errors.append("List of contacts doesn't contain added user")

        self.verify_no_errors()
