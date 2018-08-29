import time
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from views.base_element import BaseButton, BaseText, BaseElement
from views.base_view import BaseView


class WelcomeImageElement(BaseElement):
    def __init__(self, driver):
        super(WelcomeImageElement, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//android.widget.ImageView')


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


class ChatElement(BaseButton):
    def __init__(self, driver, username_part):
        super(ChatElement, self).__init__(driver)
        self.username = username_part
        self.locator = self.Locator.xpath_selector(
            "//*[@content-desc='chat-item'][.//*[starts-with(@text,'%s')]]" % self.username)

    def navigate(self):
        from views.chat_view import ChatView
        return ChatView(self.driver)

    def click(self):
        from views.chat_view import ChatMessageInput
        desired_element = ChatMessageInput(self.driver)
        self.click_until_presence_of_element(desired_element=desired_element)
        return self.navigate()

    def find_element(self):
        self.driver.info('Looking for %s' % self.name)
        for i in range(2):
            try:
                return super(ChatElement, self).find_element()
            except NoSuchElementException as e:
                if i == 0:
                    HomeView(self.driver).reconnect()
                else:
                    e.msg = 'Device %s: Unable to find chat with user %s' % (self.driver.number, self.username)
                    raise e

    @property
    def swipe_delete_button(self):
        class DeleteButton(BaseButton):
            def __init__(self, driver, parent_locator: str):
                super(DeleteButton, self).__init__(driver)
                locator_str = "/android.view.ViewGroup/*[@content-desc='icon']"
                self.locator = self.Locator.xpath_selector(parent_locator + locator_str)

        return DeleteButton(self.driver, self.locator.value)

    def swipe_and_delete(self):
        counter = 0
        while counter < 10:
            self.swipe_element()
            if self.swipe_delete_button.is_element_present():
                break
            time.sleep(10)
            counter += 1
        self.swipe_delete_button.click()

    @property
    def new_messages_counter(self):
        class UnreadMessagesCountText(BaseText):
            def __init__(self, driver, parent_locator: str):
                super(UnreadMessagesCountText, self).__init__(driver)
                locator_str = "//*[@content-desc='unread-messages-count-text']"
                self.locator = self.Locator.xpath_selector(parent_locator + locator_str)

        return UnreadMessagesCountText(self.driver, self.locator.value)


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
        self.welcome_image = WelcomeImageElement(self.driver)
        self.plus_button = PlusButton(self.driver)
        self.chat_name_text = ChatNameText(self.driver)
        self.chat_url_text = ChatUrlText(self.driver)

    def wait_for_syncing_complete(self):
        self.driver.info('Waiting for syncing complete:')
        while True:
            try:
                sync = self.find_text_part('Syncing', 10)
                self.driver.info(sync.text)
            except TimeoutException:
                break

    def get_chat_with_user(self, username):
        return ChatElement(self.driver, username[:25])

    def add_contact(self, public_key):
        start_new_chat = self.plus_button.click()
        start_new_chat.start_new_chat_button.click()
        start_new_chat.public_key_edit_box.set_value(public_key)
        one_to_one_chat = self.get_chat_view()
        start_new_chat.confirm_until_presence_of_element(one_to_one_chat.chat_message_input)
        return one_to_one_chat

    def start_1_1_chat(self, username):
        start_new_chat = self.plus_button.click()
        start_new_chat.start_new_chat_button.click()
        self.element_by_text(username).click()
        from views.chat_view import ChatView
        return ChatView(self.driver)

    def create_group_chat(self, user_names_to_add: list, group_chat_name: str = 'new_group_chat'):
        start_new_chat = self.plus_button.click()
        start_new_chat.new_group_chat_button.click()
        for user_name in user_names_to_add:
            user_contact = start_new_chat.get_username_checkbox(user_name)
            user_contact.scroll_to_element()
            user_contact.click()
        start_new_chat.next_button.click()
        start_new_chat.chat_name_editbox.send_keys(group_chat_name)
        start_new_chat.create_button.click()

    def join_public_chat(self, chat_name: str):
        start_new_chat = self.plus_button.click()
        start_new_chat.join_public_chat_button.click()
        start_new_chat.chat_name_editbox.set_value(chat_name)
        time.sleep(2)
        chat_view = self.get_chat_view()
        start_new_chat.confirm_until_presence_of_element(chat_view.chat_message_input)
        return chat_view
