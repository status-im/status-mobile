from tests import info
import time
from selenium.common.exceptions import TimeoutException
from views.base_element import BaseButton
from views.base_view import BaseView


class PlusButton(BaseButton):
    def __init__(self, driver):
        super(PlusButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='+']")

    def navigate(self):
        from views.start_new_chat_view import StarNewChatView
        return StarNewChatView(self.driver)


class ConsoleButton(BaseButton):
    def __init__(self, driver):
        super(ConsoleButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Console']")


class ChatElement(BaseButton):
    def __init__(self, driver, username):
        super(ChatElement, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='%s']" % username)

    def navigate(self):
        from views.chat_view import ChatView
        return ChatView(self.driver)


class HomeView(BaseView):
    def __init__(self, driver):
        super(HomeView, self).__init__(driver)

        self.plus_button = PlusButton(self.driver)
        self.console_button = ConsoleButton(self.driver)

    def wait_for_syncing_complete(self):
        info('Waiting for syncing complete:')
        while True:
            try:
                sync = self.find_text_part('Syncing', 10)
                info(sync.text)
            except TimeoutException:
                break

    def get_chat_with_user(self, username):
        return ChatElement(self.driver, username)

    def add_contact(self, public_key):
        start_new_chat = self.plus_button.click()
        start_new_chat.add_new_contact.click()
        start_new_chat.public_key_edit_box.send_keys(public_key)
        start_new_chat.confirm()
        start_new_chat.confirm_public_key_button.click()

    def create_group_chat(self, user_names_to_add: list, group_chat_name: str = 'new_group_chat'):
        start_new_chat = self.plus_button.click()
        start_new_chat.new_group_chat_button.click()
        for user_name in user_names_to_add:
            user_contact = start_new_chat.element_by_text(user_name, 'button')
            user_contact.scroll_to_element()
            user_contact.click()
        start_new_chat.next_button.click()
        start_new_chat.name_edit_box.send_keys(group_chat_name)
        start_new_chat.save_button.click()

    def get_public_key(self):
        profile_view = self.profile_button.click()
        profile_view.share_my_contact_key_button.click()
        time.sleep(4)
        public_key = profile_view.get_text_from_qr()
        profile_view.cross_icon.click()
        return public_key
