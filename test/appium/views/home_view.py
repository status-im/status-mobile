from tests import info
import time
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from views.base_element import BaseButton, BaseText
from views.base_view import BaseView


class PlusButton(BaseButton):
    def __init__(self, driver):
        super(PlusButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("new-chat-button")

    def navigate(self):
        from views.start_new_chat_view import StartNewChatView
        return StartNewChatView(self.driver)

    def click(self):
        from views.start_new_chat_view import StartNewChatButton
        desired_element = StartNewChatButton(self.driver)
        self.click_until_presence_of_element(desired_element=desired_element)
        return self.navigate()


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

    @property
    def swipe_delete_button(self):

        class DeleteButton(BaseButton):
            def __init__(self, driver, parent_locator: str):
                super(DeleteButton, self).__init__(driver)
                locator_str = "/../../following-sibling::*[1][name()='android.view.ViewGroup']/*[@content-desc='icon']"
                self.locator = self.Locator.xpath_selector(parent_locator + locator_str)

        return DeleteButton(self.driver, self.locator.value)


class ChatNameText(BaseText):
    def __init__(self, driver):
        super(ChatNameText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-name-text')


class ChatUrlText(BaseText):
    def __init__(self, driver):
        super(ChatUrlText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-url-text')


class HomeView(BaseView):
    def __init__(self, driver):
        super(HomeView, self).__init__(driver)

        self.plus_button = PlusButton(self.driver)
        self.console_button = ConsoleButton(self.driver)
        self.chat_name_text = ChatNameText(self.driver)
        self.chat_url_text = ChatUrlText(self.driver)

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

    def get_back_to_home_view(self):
        counter = 0
        while not self.home_button.is_element_displayed(2):
            try:
                if counter >= 5:
                    return
                self.back_button.click()
            except (NoSuchElementException, TimeoutException):
                counter += 1

    def add_contact(self, public_key):
        start_new_chat = self.plus_button.click()
        start_new_chat.start_new_chat_button.click()
        start_new_chat.public_key_edit_box.set_value(public_key)
        start_new_chat.confirm()
        one_to_one_chat = self.get_chat_view()
        one_to_one_chat.chat_message_input.wait_for_element(60)

    def create_group_chat(self, user_names_to_add: list, group_chat_name: str = 'new_group_chat'):
        start_new_chat = self.plus_button.click()
        start_new_chat.new_group_chat_button.click()
        for user_name in user_names_to_add:
            user_contact = start_new_chat.get_username_checkbox(user_name)
            user_contact.scroll_to_element()
            user_contact.click()
        start_new_chat.next_button.click()
        start_new_chat.chat_name_editbox.send_keys(group_chat_name)
        start_new_chat.confirm_button.click()

    def join_public_chat(self, chat_name: str):
        start_new_chat = self.plus_button.click()
        start_new_chat.join_public_chat_button.click()
        start_new_chat.chat_name_editbox.send_keys(chat_name)
        start_new_chat.confirm()

    def get_public_key(self):
        profile_view = self.profile_button.click()
        profile_view.share_my_contact_key_button.click()
        time.sleep(4)
        public_key = profile_view.get_text_from_qr()
        profile_view.cross_icon.click()
        return public_key

    def swipe_and_delete_chat(self, chat_name: str):
        chat_element = self.get_chat_with_user(chat_name)
        location = chat_element.find_element().location
        x, y = location['x'], location['y']
        size = chat_element.find_element().size
        width, height = size['width'], size['height']
        counter = 0
        while counter < 10:
            self.driver.swipe(start_x=x + width / 2, start_y=y + height / 2, end_x=x, end_y=y + height / 2)
            if chat_element.swipe_delete_button.is_element_present():
                break
            time.sleep(10)
            counter += 1
        chat_element.swipe_delete_button.click()
