import time
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from views.base_element import BaseButton, BaseText, BaseElement, BaseEditBox
from views.base_view import BaseView
from tests import test_dapp_url


class WelcomeImageElement(BaseElement):
    def __init__(self, driver):
        super(WelcomeImageElement, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//android.widget.ImageView')


class PlusButton(BaseButton):
    def __init__(self, driver):
        super(PlusButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("new-chat-button")


class DeleteChatButton(BaseButton):
    def __init__(self, driver):
        super(DeleteChatButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("delete-chat-button")


class ClearHistoryButton(BaseButton):
    def __init__(self, driver):
        super(ClearHistoryButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("clear-history-button")


class StartNewChatButton(BaseButton):
    def __init__(self, driver):
        super(StartNewChatButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('start-1-1-chat-button')

    def navigate(self):
        from views.contacts_view import ContactsView
        return ContactsView(self.driver)


class NewGroupChatButton(BaseButton):

    def __init__(self, driver):
        super(NewGroupChatButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('start-group-chat-button')

    def navigate(self):
        from views.contacts_view import ContactsView
        return ContactsView(self.driver)


class JoinPublicChatButton(BaseButton):

    def __init__(self, driver):
        super(JoinPublicChatButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('join-public-chat-button')

    def navigate(self):
        from views.contacts_view import ContactsView
        return ContactsView(self.driver)


class InviteFriendsButton(BaseButton):
    def __init__(self, driver):
        super(InviteFriendsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('invite-friends-button')


class ChatsMenuInviteFriendsButton(BaseButton):
    def __init__(self, driver):
        super(ChatsMenuInviteFriendsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chats-menu-invite-friends-button')


class UserNameBelowNewChatButton(BaseButton):
    def __init__(self, driver, username_part):
        super(UserNameBelowNewChatButton, self).__init__(driver)
        self.username = username_part
        self.locator = self.Locator.xpath_selector(
            "//*[@content-desc='enter-contact-code-input']/../..//*[starts-with(@text,'%s')]" % self.username)


class ChatElement(BaseButton):
    def __init__(self, driver, username_part):
        super(ChatElement, self).__init__(driver)
        self.username = username_part
        self.locator = self.Locator.xpath_selector(
            "//*[@content-desc='chat-name-text'][starts-with(@text,'%s')]/.." % self.username)

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
                locator_str = "/../..//*[@content-desc='icon']"
                self.locator = self.Locator.xpath_selector(parent_locator + locator_str)

        return DeleteButton(self.driver, self.locator.value)

    @property
    def new_messages_counter(self):
        class UnreadMessagesCountText(BaseText):
            def __init__(self, driver, parent_locator: str):
                super(UnreadMessagesCountText, self).__init__(driver)
                # TODO: commented until accessibility-id will be added back
                # locator_str = "//*[@content-desc='unread-messages-count-text']"
                # self.locator = self.Locator.xpath_selector(parent_locator + locator_str)
                locator_str = "//android.widget.TextView)[last()]"
                self.locator = self.Locator.xpath_selector("(" + parent_locator + locator_str)

        return UnreadMessagesCountText(self.driver, self.locator.value)

    @property
    def new_messages_public_chat(self):
        class UnreadMessagesPublicChat(BaseElement):
            def __init__(self, driver):
                super(UnreadMessagesPublicChat, self).__init__(driver)
                self.locator = self.Locator.accessibility_id('unviewed-messages-public')

        return UnreadMessagesPublicChat(self.driver)


class MarkAllMessagesAsReadButton(BaseButton):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.accessibility_id('mark-all-read-button')


class ChatNameText(BaseText):
    def __init__(self, driver):
        super(ChatNameText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-name-text')


class ChatUrlText(BaseText):
    def __init__(self, driver):
        super(ChatUrlText, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-url-text')


class SearchChatInput(BaseEditBox):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.text_selector('Search')


class HomeView(BaseView):
    def __init__(self, driver):
        super(HomeView, self).__init__(driver)
        self.welcome_image = WelcomeImageElement(self.driver)
        self.plus_button = PlusButton(self.driver)
        self.chat_name_text = ChatNameText(self.driver)
        self.chat_url_text = ChatUrlText(self.driver)
        self.search_chat_input = SearchChatInput(self.driver)

        self.start_new_chat_button = StartNewChatButton(self.driver)
        self.new_group_chat_button = NewGroupChatButton(self.driver)
        self.join_public_chat_button = JoinPublicChatButton(self.driver)
        self.invite_friends_button = InviteFriendsButton(self.driver)
        self.chats_menu_invite_friends_button = ChatsMenuInviteFriendsButton(self.driver)
        self.delete_chat_button = DeleteChatButton(self.driver)
        self.clear_history_button = ClearHistoryButton(self.driver)
        self.mark_all_messages_as_read_button = MarkAllMessagesAsReadButton(self.driver)

    def wait_for_syncing_complete(self):
        self.driver.info('Waiting for syncing complete:')
        while True:
            try:
                sync = self.find_text_part('Syncing', 10)
                self.driver.info(sync.text)
            except TimeoutException:
                break

    def get_chat(self, username):
        return ChatElement(self.driver, username[:25])

    def get_username_below_start_new_chat_button(self, username_part):
        return UserNameBelowNewChatButton(self.driver, username_part)

    def add_contact(self, public_key, add_in_contacts=True):
        self.plus_button.click_until_presence_of_element(self.start_new_chat_button)
        contacts_view = self.start_new_chat_button.click()
        contacts_view.public_key_edit_box.click()
        contacts_view.public_key_edit_box.send_keys(public_key)
        one_to_one_chat = self.get_chat_view()
        contacts_view.confirm_until_presence_of_element(one_to_one_chat.chat_message_input)
        if add_in_contacts:
            one_to_one_chat.add_to_contacts.click()
        return one_to_one_chat

    def start_1_1_chat(self, username):
        self.plus_button.click()
        self.start_new_chat_button.click()
        self.element_by_text(username).click()
        from views.chat_view import ChatView
        return ChatView(self.driver)

    def create_group_chat(self, user_names_to_add: list, group_chat_name: str = 'new_group_chat'):
        self.plus_button.click()
        contacts_view = self.new_group_chat_button.click()
        for user_name in user_names_to_add:
            user_contact = contacts_view.get_username_checkbox(user_name)
            user_contact.scroll_to_element()
            user_contact.click()
        contacts_view.next_button.click()
        contacts_view.chat_name_editbox.send_keys(group_chat_name)
        contacts_view.create_button.click()
        from views.chat_view import ChatView
        return ChatView(self.driver)

    def join_public_chat(self, chat_name: str):
        self.plus_button.click_until_presence_of_element(self.join_public_chat_button, attempts=5)
        self.join_public_chat_button.wait_for_visibility_of_element(5)
        contacts_view = self.join_public_chat_button.click()
        contacts_view.chat_name_editbox.click()
        contacts_view.chat_name_editbox.send_keys(chat_name)
        time.sleep(2)
        chat_view = self.get_chat_view()
        self.confirm_until_presence_of_element(chat_view.chat_message_input)
        return chat_view

    def open_status_test_dapp(self, allow_all=True):
        dapp_view = self.dapp_tab_button.click()
        dapp_view.open_url(test_dapp_url)
        status_test_dapp = dapp_view.get_status_test_dapp_view()
        status_test_dapp.allow_button.wait_for_element(20)
        if allow_all:
            status_test_dapp.allow_button.click_until_absense_of_element(status_test_dapp.allow_button)
        else:
            status_test_dapp.deny_button.click_until_absense_of_element(status_test_dapp.deny_button)
        return status_test_dapp

    def delete_chat_long_press(self, username):
        self.get_chat(username).long_press_element()
        self.delete_chat_button.click()
        self.delete_button.click()

    def leave_chat_long_press(self, username):
        self.get_chat(username).long_press_element()
        from views.chat_view import LeaveChatButton, LeaveButton
        LeaveChatButton(self.driver).click()
        LeaveButton(self.driver).click()

    def clear_chat_long_press(self, username):
        self.get_chat(username).long_press_element()
        self.clear_history_button.click()
        from views.chat_view import ClearButton
        self.clear_button = ClearButton(self.driver)
        self.clear_button.click()
