import time

import pytest
from selenium.common.exceptions import TimeoutException
from tests import info
from views.base_element import BaseButton, BaseEditBox, BaseText, BaseElement
from views.base_view import BaseView
from views.profile_view import ProfilePictureElement


class ChatMessageInput(BaseEditBox):
    def __init__(self, driver):
        super(ChatMessageInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-message-input')


class AddToContacts(BaseButton):
    def __init__(self, driver):
        super(AddToContacts, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('add-to-contacts-button')


class UserNameText(BaseText):
    def __init__(self, driver):
        super(UserNameText, self).__init__(driver)
        self.locator = \
            self.Locator.accessibility_id('chat-name-text')


class TransactionPopupText(BaseText):
    def __init__(self, driver):
        super(TransactionPopupText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Specify amount']")


class SendCommand(BaseButton):
    def __init__(self, driver):
        super(SendCommand, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('send-payment-button')


class RequestCommand(BaseButton):
    def __init__(self, driver):
        super(RequestCommand, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('request-payment-button')


class EthAsset(BaseButton):
    def __init__(self, driver):
        super(EthAsset, self).__init__(driver)
        self.locator = self.Locator.text_selector('ETH')


class ChatMenuButton(BaseButton):
    def __init__(self, driver):
        super(ChatMenuButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-menu-button')


class MembersButton(BaseButton):

    def __init__(self, driver):
        super(MembersButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="action"])[1]')


class DeleteChatButton(BaseButton):

    def __init__(self, driver):
        super(DeleteChatButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Delete chat"]')


class ClearHistoryButton(BaseButton):

    def __init__(self, driver):
        super(ClearHistoryButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Clear history"]')


class LeaveChatButton(BaseButton):

    def __init__(self, driver):
        super(LeaveChatButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Leave public chat"]')


class ClearButton(BaseButton):

    def __init__(self, driver):
        super(ClearButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="CLEAR"]')


class LeaveButton(BaseButton):

    def __init__(self, driver):
        super(LeaveButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="LEAVE"]')


class ChatSettings(BaseButton):
    def __init__(self, driver):
        super(ChatSettings, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Settings']")


class UserOptions(BaseButton):
    def __init__(self, driver):
        super(UserOptions, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('options')


class RemoveButton(BaseButton):
    def __init__(self, driver):
        super(RemoveButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Remove']")


class FirstRecipient(BaseButton):
    def __init__(self, driver):
        super(FirstRecipient, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('contact-item')


class MoreUsersButton(BaseButton):
    def __init__(self, driver):
        super(MoreUsersButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[contains(@text, 'MORE')]")


class OpenInBrowserButton(BaseButton):
    def __init__(self, driver):
        super(OpenInBrowserButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Open in browser']")

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class CommandsButton(BaseButton):
    def __init__(self, driver):
        super(CommandsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-commands-button')


class ViewProfileButton(BaseButton):
    def __init__(self, driver):
        super(ViewProfileButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="View profile"]')


class NoMessagesInChatText(BaseText):
    def __init__(self, driver):
        super(NoMessagesInChatText, self).__init__(driver)
        self.locator = self.Locator.text_part_selector(
            'Any messages you send here are encrypted and can only be read by you and')


class ProfileSendMessageButton(BaseButton):
    def __init__(self, driver):
        super(ProfileSendMessageButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('start-conversation-button')


class ProfileSendTransactionButton(BaseButton):
    def __init__(self, driver):
        super(ProfileSendTransactionButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('send-transaction-button')


class ChatElementByText(BaseText):
    def __init__(self, driver, text):
        super(ChatElementByText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[starts-with(@text,'%s')]/ancestor::android.view.ViewGroup[@content-desc='chat-item']" % text)

    @property
    def status(self):
        class StatusText(BaseText):
            def __init__(self, driver, parent_locator: str):
                super(StatusText, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(parent_locator + '/android.widget.TextView')

        return StatusText(self.driver, self.locator.value)

    @property
    def progress_bar(self):
        class ProgressBar(BaseElement):
            def __init__(self, driver, parent_locator: str):
                super(ProgressBar, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(parent_locator + '//android.widget.ProgressBar')

        return ProgressBar(self.driver, self.locator.value)

    def contains_text(self, text) -> bool:
        element = BaseText(self.driver)
        element.locator = element.Locator.xpath_selector(
            self.locator.value + "//android.view.ViewGroup//android.widget.TextView[@text='%s']" % text)
        return element.is_element_displayed()

    @property
    def username(self):
        class Username(BaseText):
            def __init__(self, driver, parent_locator):
                super(Username, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(parent_locator + "/*[1][name()='android.widget.TextView']")

        return Username(self.driver, self.locator.value)

    @property
    def send_request_button(self):
        class SendRequestButton(BaseButton):
            def __init__(self, driver, parent_locator):
                super(SendRequestButton, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(parent_locator + '//*[@text="Send"]')

        return SendRequestButton(self.driver, self.locator.value)


class ChatView(BaseView):
    def __init__(self, driver):
        super(ChatView, self).__init__(driver)

        self.chat_message_input = ChatMessageInput(self.driver)
        self.add_to_contacts = AddToContacts(self.driver)
        self.user_name_text = UserNameText(self.driver)
        self.no_messages_in_chat = NoMessagesInChatText(self.driver)

        self.commands_button = CommandsButton(self.driver)
        self.send_command = SendCommand(self.driver)
        self.request_command = RequestCommand(self.driver)
        self.eth_asset = EthAsset(self.driver)

        self.chat_options = ChatMenuButton(self.driver)
        self.members_button = MembersButton(self.driver)
        self.delete_chat_button = DeleteChatButton(self.driver)
        self.clear_history_button = ClearHistoryButton(self.driver)
        self.clear_button = ClearButton(self.driver)
        self.leave_chat_button = LeaveChatButton(self.driver)
        self.leave_button = LeaveButton(self.driver)

        self.chat_settings = ChatSettings(self.driver)
        self.view_profile_button = ViewProfileButton(self.driver)
        self.more_users_button = MoreUsersButton(self.driver)
        self.user_options = UserOptions(self.driver)
        self.remove_button = RemoveButton(self.driver)

        self.first_recipient_button = FirstRecipient(self.driver)

        self.open_in_browser_button = OpenInBrowserButton(self.driver)

        # Contact's profile
        self.contact_profile_picture = ProfilePictureElement(self.driver)
        self.profile_send_message = ProfileSendMessageButton(self.driver)
        self.profile_send_transaction = ProfileSendTransactionButton(self.driver)

    def wait_for_syncing_complete(self):
        info('Waiting for syncing complete:')
        while True:
            try:
                sync = self.find_text_part('Syncing', 10)
                info(sync.text)
            except TimeoutException:
                break

    def wait_for_message_in_one_to_one_chat(self, expected_message: str, errors: list, wait_time: int = 20):
        try:
            element = ChatElementByText(self.driver, expected_message)
            element.wait_for_element(wait_time)
        except TimeoutException:
            errors.append('Message with text "%s" was not received' % expected_message)

    def wait_for_messages(self, username: str, expected_messages: list, errors: list, wait_time: int = 30):
        expected_messages = expected_messages if type(expected_messages) == list else [expected_messages]
        repeat = 0
        received_messages = list()
        while repeat <= wait_time:
            for message in expected_messages:
                if self.element_starts_with_text(message, 'text').is_element_present(1):
                    received_messages.append(message)
            if not set(expected_messages) - set(received_messages):
                break
            time.sleep(3)
            repeat += 3
        if set(expected_messages) - set(received_messages):
            errors.append('Not received messages from user %s: "%s"' % (username, ', '.join(
                [i for i in list(set(expected_messages) - set(received_messages))])))

    def send_eth_to_request(self, amount, sender_password, wallet_set_up=False):
        gas_popup = self.element_by_text_part('Specify amount')
        send_request_button = self.chat_element_by_text(amount).send_request_button
        send_request_button.click_until_presence_of_element(gas_popup)
        send_transaction = self.get_send_transaction_view()
        if wallet_set_up:
            wallet_view = self.get_wallet_view()
            self.send_message_button.click_until_presence_of_element(wallet_view.sign_in_phrase)
            wallet_view.done_button.click()
            wallet_view.yes_button.click()
        else:
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

    def clear_history(self):
        self.chat_options.click()
        self.clear_history_button.click()
        self.clear_button.click()

    def send_transaction_in_1_1_chat(self, amount, password, wallet_set_up=False):
        self.commands_button.click()
        self.send_command.click()
        self.eth_asset.click()
        self.send_as_keyevent(amount)
        send_transaction_view = self.get_send_transaction_view()
        if wallet_set_up:
            wallet_view = self.get_wallet_view()
            self.send_message_button.click_until_presence_of_element(wallet_view.sign_in_phrase)
            wallet_view.done_button.click()
            wallet_view.yes_button.click()
        else:
            self.send_message_button.click_until_presence_of_element(send_transaction_view.sign_transaction_button)
        send_transaction_view.sign_transaction(password)
        chat_elem = self.chat_element_by_text(amount)
        chat_elem.wait_for_visibility_of_element()
        chat_elem.progress_bar.wait_for_invisibility_of_element()
        if chat_elem.status.text not in ('Sent', 'Delivered', 'Seen'):
            pytest.fail('Sent transaction message was not sent')

    def send_transaction_in_group_chat(self, amount, password, recipient):
        self.commands_button.click()
        self.send_command.click()
        self.find_full_text(recipient['username']).click()
        self.send_as_keyevent(amount)
        self.send_message_button.click()

        send_transaction_view = self.get_send_transaction_view()
        self.send_message_button.click_until_presence_of_element(send_transaction_view.sign_transaction_button)
        send_transaction_view.sign_transaction(password)
        send_transaction_view.find_full_text(amount)
        self.find_full_text('to  ' + recipient['username'], 10)

    def request_transaction_in_1_1_chat(self, amount):
        self.commands_button.click()
        self.request_command.click()
        self.eth_asset.click()
        self.send_as_keyevent(amount)
        self.send_message_button.click()

    def chat_element_by_text(self, text):
        info("Looking for full text: '%s'" % text)
        return ChatElementByText(self.driver, text)

    def verify_message_is_under_today_text(self, text, errors):
        message_element = self.chat_element_by_text(text)
        message_element.wait_for_visibility_of_element()
        message_location = message_element.find_element().location['y']
        today_text_element = self.element_by_text('Today').find_element()
        today_location = today_text_element.location['y']
        today_height = today_text_element.size['height']
        if message_location < today_location + today_height:
            errors.append("Message '%s' is not uder 'Today' text" % text)
