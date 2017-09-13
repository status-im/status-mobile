from views.base_view import BaseViewObject
from views.base_element import *


class ProfileButton(BaseButton):

    def __init__(self, driver):
        super(ProfileButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('toolbar-hamburger-menu')


class ProfileIcon(BaseButton):

    def __init__(self, driver):
        super(ProfileIcon, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('drawer-profile-icon')

    def navigate(self):
        from views.profile import ProfileViewObject
        return ProfileViewObject(self.driver)


class PlusButton(BaseButton):

    def __init__(self, driver):
        super(PlusButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='+']")


class AddNewContactButton(BaseButton):

    def __init__(self, driver):
        super(AddNewContactButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='Add new contact']")


class NewGroupChatButton(BaseButton):

    def __init__(self, driver):
        super(NewGroupChatButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='New group chat']")

    class NextButton(BaseButton):
        def __init__(self, driver):
            super(NewGroupChatButton.NextButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector(
                "//android.widget.TextView[@text='NEXT']")

    class NameEditBox(BaseEditBox):
        def __init__(self, driver):
            super(NewGroupChatButton.NameEditBox, self).__init__(driver)
            self.locator = \
                self.Locator.xpath_selector("//android.widget.EditText[@NAF='true']")

    class SaveButton(BaseButton):
        def __init__(self, driver):
            super(NewGroupChatButton.SaveButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector(
                "//android.widget.TextView[@text='SAVE']")

    class GroupChatOptions(BaseButton):
        def __init__(self, driver):
            super(NewGroupChatButton.GroupChatOptions, self).__init__(driver)
            self.locator = self.Locator.xpath_selector(
                "//android.view.ViewGroup[2]//android.widget.TextView[@text='n']")

    class ChatSettings(BaseButton):
        def __init__(self, driver):
            super(NewGroupChatButton.ChatSettings, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Settings']")

    class UserOptions(BaseButton):
        def __init__(self, driver):
            super(NewGroupChatButton.UserOptions, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('//android.widget.ImageView[@content-desc="chat-icon"]'
                                                       '/../..//android.view.View')

    class RemoveButton(BaseButton):
        def __init__(self, driver):
            super(NewGroupChatButton.RemoveButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Remove']")


class PublicKeyEditBox(BaseEditBox):

    def __init__(self, driver):
        super(PublicKeyEditBox, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.EditText[@NAF='true']")


class ConfirmPublicKeyButton(BaseButton):

    def __init__(self, driver):
        super(ConfirmPublicKeyButton, self).__init__(driver)
        self.locator = \
            self.Locator.accessibility_id('toolbar-action')


class ChatMessageInput(BaseEditBox):

    def __init__(self, driver):
        super(ChatMessageInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-message-input')


class SendMessageButton(BaseButton):

    def __init__(self, driver):
        super(SendMessageButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("send-message-button")


class UserNameText(BaseText):

    def __init__(self, driver):
        super(UserNameText, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.ScrollView//android.widget.TextView")


class ChatsViewObject(BaseViewObject):

    def __init__(self, driver):
        super(ChatsViewObject, self).__init__(driver)
        self.driver = driver

        self.profile_button = ProfileButton(self.driver)
        self.profile_icon = ProfileIcon(self.driver)
        self.plus_button = PlusButton(self.driver)
        self.add_new_contact = AddNewContactButton(self.driver)

        self.public_key_edit_box = PublicKeyEditBox(self.driver)
        self.confirm_public_key_button = ConfirmPublicKeyButton(self.driver)

        self.new_group_chat_button = NewGroupChatButton(self.driver)
        self.next_button = NewGroupChatButton.NextButton(self.driver)
        self.name_edit_box = NewGroupChatButton.NameEditBox(self.driver)
        self.save_button = NewGroupChatButton.SaveButton(self.driver)
        self.group_chat_options = NewGroupChatButton.GroupChatOptions(self.driver)
        self.chat_settings = NewGroupChatButton.ChatSettings(self.driver)
        self.user_options = NewGroupChatButton.UserOptions(self.driver)
        self.remove_button = NewGroupChatButton.RemoveButton(self.driver)

        self.chat_message_input = ChatMessageInput(self.driver)
        self.send_message_button = SendMessageButton(self.driver)
        self.user_name_text = UserNameText(self.driver)
