from views.base_view import BaseView
from views.base_element import *


class ProfileButton(BaseButton):
    def __init__(self, driver):
        super(ProfileButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('toolbar-hamburger-menu')

    def navigate(self):
        from views.profile_drawer_view import ProfileDrawer
        return ProfileDrawer(self.driver)


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


class ChatMessageInput(BaseEditBox):
    def __init__(self, driver):
        super(ChatMessageInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-message-input')


class SendMessageButton(BaseButton):
    def __init__(self, driver):
        super(SendMessageButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("send-message-button")

    def click(self):
        self.find_element().click()
        logging.info('Tap on %s' % self.name)


class AddToContacts(BaseButton):
    def __init__(self, driver):
        super(AddToContacts, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Add to contacts']")


class UserNameText(BaseText):
    def __init__(self, driver):
        super(UserNameText, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.ScrollView//android.widget.TextView")


class SendCommand(BaseButton):
    def __init__(self, driver):
        super(SendCommand, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='/send']")


class RequestCommand(BaseButton):
    def __init__(self, driver):
        super(RequestCommand, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='/request']")


class GroupChatOptions(BaseButton):
    def __init__(self, driver):
        super(GroupChatOptions, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.view.ViewGroup[2]//android.widget.TextView[@text='n']")


class ChatSettings(BaseButton):
    def __init__(self, driver):
        super(ChatSettings, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Settings']")


class UserOptions(BaseButton):
    def __init__(self, driver):
        super(UserOptions, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//android.widget.ImageView[@content-desc="chat-icon"]'
                                                   '/../..//android.view.View')


class RemoveButton(BaseButton):
    def __init__(self, driver):
        super(RemoveButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Remove']")


class FirstRecipient(BaseButton):
    def __init__(self, driver):
        super(FirstRecipient, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Choose recipient']/.."
                                                   "//android.widget.ImageView[@content-desc='chat-icon']")


class ChatView(BaseView):
    def __init__(self, driver):
        super(ChatView, self).__init__(driver)

        self.profile_button = ProfileButton(self.driver)
        self.plus_button = PlusButton(self.driver)
        self.console_button = ConsoleButton(self.driver)

        self.chat_message_input = ChatMessageInput(self.driver)
        self.send_message_button = SendMessageButton(self.driver)
        self.add_to_contacts = AddToContacts(self.driver)
        self.user_name_text = UserNameText(self.driver)

        self.send_command = SendCommand(self.driver)
        self.request_command = RequestCommand(self.driver)

        self.group_chat_options = GroupChatOptions(self.driver)
        self.chat_settings = ChatSettings(self.driver)
        self.user_options = UserOptions(self.driver)
        self.remove_button = RemoveButton(self.driver)

        self.first_recipient_button = FirstRecipient(self.driver)

    def wait_for_syncing_complete(self):
        logging.info('Waiting for syncing complete:')
        while True:
            try:
                sync = self.find_text_part('Syncing', 10)
                logging.info(sync.text)
            except TimeoutException:
                break
