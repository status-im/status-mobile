from views.base_element import BaseButton, BaseEditBox
from views.contacts_view import ContactsView


class StartNewChatButton(BaseButton):
    def __init__(self, driver):
        super(StartNewChatButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('start-1-1-chat-button')


class NewGroupChatButton(BaseButton):

    def __init__(self, driver):
        super(NewGroupChatButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('start-group-chat-button')


class JoinPublicChatButton(BaseButton):

    def __init__(self, driver):
        super(JoinPublicChatButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('join-public-chat-button')


class ChatNameEditBox(BaseEditBox):
    def __init__(self, driver):
        super(ChatNameEditBox, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-name-input')


class OpenDAppButton(BaseButton):
    def __init__(self, driver):
        super(OpenDAppButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('open-dapp-button')


class OpenButton(BaseButton):
    def __init__(self, driver):
        super(OpenButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('open-dapp-button')

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class CreateButton(BaseButton):
    def __init__(self, driver):
        super(CreateButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('create-button')

    def navigate(self):
        from views.chat_view import ChatView
        return ChatView(self.driver)


class EnterUrlEditbox(BaseEditBox):
    def __init__(self, driver):
        super(EnterUrlEditbox, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('dapp-url-input')


class UsernameCheckbox(BaseButton):
    def __init__(self, driver, username):
        super(UsernameCheckbox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='%s']/../../android.widget.CheckBox" % username)


class InviteFriendsButton(BaseButton):
    def __init__(self, driver):
        super(InviteFriendsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('invite-friends-button')


class StartNewChatView(ContactsView):
    def __init__(self, driver):
        super(StartNewChatView, self).__init__(driver)

        self.start_new_chat_button = StartNewChatButton(self.driver)
        self.new_group_chat_button = NewGroupChatButton(self.driver)
        self.join_public_chat_button = JoinPublicChatButton(self.driver)

        self.open_d_app_button = OpenDAppButton(self.driver)
        self.open_button = OpenButton(self.driver)
        self.invite_friends_button = InviteFriendsButton(self.driver)

        self.chat_name_editbox = ChatNameEditBox(self.driver)
        self.enter_url_editbox = EnterUrlEditbox(self.driver)
        self.create_button = CreateButton(self.driver)

    def get_username_checkbox(self, username: str):
        return UsernameCheckbox(self.driver, username)
