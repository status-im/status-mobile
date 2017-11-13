from views.base_view import BaseViewObject
import time
from views.base_element import *


class ProfileButton(BaseButton):

    def __init__(self, driver):
        super(ProfileButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('toolbar-hamburger-menu')

    class ProfileIcon(BaseButton):

        def __init__(self, driver):
            super(ProfileButton.ProfileIcon, self).__init__(driver)
            self.locator = self.Locator.accessibility_id('drawer-profile-icon')

        def navigate(self):
            from views.profile import ProfileViewObject
            return ProfileViewObject(self.driver)

    class SwitchUsersButton(BaseButton):

        def __init__(self, driver):
            super(ProfileButton.SwitchUsersButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='SWITCH USERS']")

        def click(self):
            time.sleep(2)
            self.find_element().click()
            logging.info('Tap on %s' % self.name)
            return self.navigate()

        def navigate(self):
            from views.login import LoginView
            return LoginView(self.driver)


class PlusButton(BaseButton):

    def __init__(self, driver):
        super(PlusButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='+']")


class ConsoleButton(BaseButton):

    def __init__(self, driver):
        super(ConsoleButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Console']")


class AddNewContactButton(BaseButton):

    def __init__(self, driver):
        super(AddNewContactButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='Add new contact']")


class NewContactButton(BaseButton):

    def __init__(self, driver):
        super(NewContactButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='']")


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
            self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[2]')


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


class SendFundsButton(BaseButton):

    def __init__(self, driver):
        super(SendFundsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='/send']")

    class FirstRecipient(BaseButton):

        def __init__(self, driver):
            super(SendFundsButton.FirstRecipient, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//*[@text='Choose recipient']/.."
                                                       "//android.widget.ImageView[@content-desc='chat-icon']")

    class SignTransactionButton(BaseButton):

        def __init__(self, driver):
            super(SendFundsButton.SignTransactionButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//*[@text='SIGN TRANSACTION']")

    class SignLaterButton(BaseButton):

        def __init__(self, driver):
            super(SendFundsButton.SignLaterButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//*[@text='SIGN LATER']")

    class PasswordInput(BaseEditBox):

        def __init__(self, driver):
            super(SendFundsButton.PasswordInput, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//*[@text='Password']")

    class EnterPasswordInput(BaseEditBox):

        def __init__(self, driver):
            super(SendFundsButton.EnterPasswordInput, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//android.widget.EditText[@NAF='true']")

    class ConfirmButton(BaseButton):

        def __init__(self, driver):
            super(SendFundsButton.ConfirmButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//*[@text='CONFIRM']")

    class GotItButton(BaseButton):

        def __init__(self, driver):
            super(SendFundsButton.GotItButton, self).__init__(driver)
            self.locator = self.Locator.xpath_selector("//*[@text='GOT IT']")


class ChatsViewObject(BaseViewObject):

    def __init__(self, driver):
        super(ChatsViewObject, self).__init__(driver)
        self.driver = driver

        self.profile_button = ProfileButton(self.driver)
        self.profile_icon = ProfileButton.ProfileIcon(self.driver)
        self.switch_users_button = ProfileButton.SwitchUsersButton(self.driver)

        self.plus_button = PlusButton(self.driver)
        self.add_new_contact = AddNewContactButton(self.driver)
        self.console_button = ConsoleButton(self.driver)

        self.public_key_edit_box = PublicKeyEditBox(self.driver)
        self.confirm_public_key_button = ConfirmPublicKeyButton(self.driver)

        self.new_group_chat_button = NewGroupChatButton(self.driver)
        self.next_button = NewGroupChatButton.NextButton(self.driver)
        self.name_edit_box = NewGroupChatButton.NameEditBox(self.driver)
        self.group_chat_options = NewGroupChatButton.GroupChatOptions(self.driver)
        self.chat_settings = NewGroupChatButton.ChatSettings(self.driver)
        self.user_options = NewGroupChatButton.UserOptions(self.driver)
        self.remove_button = NewGroupChatButton.RemoveButton(self.driver)

        self.chat_message_input = ChatMessageInput(self.driver)
        self.send_message_button = SendMessageButton(self.driver)
        self.add_to_contacts = AddToContacts(self.driver)
        self.user_name_text = UserNameText(self.driver)

        self.send_funds_button = SendFundsButton(self.driver)

        self.first_recipient_button = SendFundsButton.FirstRecipient(self.driver)
        self.sign_transaction_button = SendFundsButton.SignTransactionButton(self.driver)
        self.sign_later_button = SendFundsButton.SignLaterButton(self.driver)
        self.confirm_button = SendFundsButton.ConfirmButton(self.driver)
        self.password_input = SendFundsButton.PasswordInput(self.driver)
        self.enter_password_input = SendFundsButton.EnterPasswordInput(self.driver)
        self.got_it_button = SendFundsButton.GotItButton(self.driver)

        self.new_contact_button = NewContactButton(self.driver)

    def wait_for_syncing_complete(self):
        logging.info('Waiting for syncing complete:')
        while True:
            try:
                sync = self.find_text_part('Syncing', 10)
                logging.info(sync.text)
            except TimeoutException:
                break
