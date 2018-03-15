import time
from selenium.common.exceptions import TimeoutException
from tests import info
from views.base_element import BaseButton, BaseEditBox, BaseText
from views.base_view import BaseView


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
        info('Tap on %s' % self.name)


class AddToContacts(BaseButton):
    def __init__(self, driver):
        super(AddToContacts, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Add to contacts']")


class UserNameText(BaseText):
    def __init__(self, driver):
        super(UserNameText, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="toolbar-back-button"]'
                                        '//..//android.widget.TextView)[1]')


class TransactionPopupText(BaseText):
    def __init__(self, driver):
        super(TransactionPopupText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Send transaction']")


class SendCommand(BaseButton):
    def __init__(self, driver):
        super(SendCommand, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='/send']")

    def click(self):
        desired_element = TransactionPopupText(self.driver)
        self.click_until_presence_of_element(desired_element=desired_element)


class RequestCommand(BaseButton):
    def __init__(self, driver):
        super(RequestCommand, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='/request']")


class ChatOptions(BaseButton):
    def __init__(self, driver):
        super(ChatOptions, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[2]')


class MembersButton(BaseButton):

    def __init__(self, driver):
        super(MembersButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="action"])[1]')


class DeleteChatButton(BaseButton):

    def __init__(self, driver):
        super(DeleteChatButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Delete chat"]')


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


class MessageByUsername(BaseText):
    def __init__(self, driver, username):
        super(MessageByUsername, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="' + username + '"]'
                                                   '/following-sibling::android.widget.TextView')


class MoreUsersButton(BaseButton):
    def __init__(self, driver):
        super(MoreUsersButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[contains(@text, 'MORE')]")


class UserProfileIconTopRight(BaseButton):
    def __init__(self, driver):
        super(UserProfileIconTopRight, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-icon')


class UserProfileDetails(BaseButton):
    def __init__(self, driver):
        super(UserProfileDetails, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Profile']")


class OpenInBrowserButton(BaseButton):
    def __init__(self, driver):
        super(OpenInBrowserButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Open in browser']")

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class ChatView(BaseView):
    def __init__(self, driver):
        super(ChatView, self).__init__(driver)

        self.chat_message_input = ChatMessageInput(self.driver)
        self.send_message_button = SendMessageButton(self.driver)
        self.add_to_contacts = AddToContacts(self.driver)
        self.user_name_text = UserNameText(self.driver)

        self.send_command = SendCommand(self.driver)
        self.request_command = RequestCommand(self.driver)

        self.chat_options = ChatOptions(self.driver)
        self.members_button = MembersButton(self.driver)
        self.delete_chat_button = DeleteChatButton(self.driver)

        self.chat_settings = ChatSettings(self.driver)
        self.more_users_button = MoreUsersButton(self.driver)
        self.user_options = UserOptions(self.driver)
        self.remove_button = RemoveButton(self.driver)

        self.first_recipient_button = FirstRecipient(self.driver)

        self.user_profile_icon_top_right = UserProfileIconTopRight(self.driver)
        self.user_profile_details = UserProfileDetails(self.driver)

        self.open_in_browser_button = OpenInBrowserButton(self.driver)

    def wait_for_syncing_complete(self):
        info('Waiting for syncing complete:')
        while True:
            try:
                sync = self.find_text_part('Syncing', 10)
                info(sync.text)
            except TimeoutException:
                break

    def wait_for_message_in_one_to_one_chat(self, expected_message: str, errors: list):
        try:
            self.find_full_text(expected_message, wait_time=20)
        except TimeoutException:
            errors.append('Message with text "%s" was not received' % expected_message)

    def wait_for_messages_by_user(self, username: str, expected_messages: list, errors: list, wait_time: int = 30):
        expected_messages = expected_messages if type(expected_messages) == list else [expected_messages]
        repeat = 0
        while repeat <= wait_time:
            received_messages = [element.text for element in MessageByUsername(self.driver, username).find_elements()]
            if not set(expected_messages) - set(received_messages):
                break
            time.sleep(3)
            repeat += 3
        if set(expected_messages) - set(received_messages):
            errors.append('Not received messages from user %s: "%s"' % (username, ', '.join(
                [i for i in list(set(expected_messages) - set(received_messages))])))

    def send_eth_to_request(self, request, sender_password):
        gas_popup = self.element_by_text_part('Send transaction')
        request.click_until_presence_of_element(gas_popup)
        send_transaction = self.get_send_transaction_view()
        self.send_message_button.click_until_presence_of_element(send_transaction.sign_transaction_button)
        send_transaction.sign_transaction(sender_password)

    def delete_chat(self, chat_name: str, errors: list):
        self.chat_options.click()
        self.delete_chat_button.click()
        self.delete_button.click()
        from views.home_view import HomeView
        if not HomeView(self.driver).plus_button.is_element_present() or \
                self.element_by_text(chat_name).is_element_present():
            errors.append('Chat was not deleted')
