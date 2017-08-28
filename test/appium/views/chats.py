from views.base_view import BaseViewObject
from views.base_element import *


class ProfileButton(BaseButton):

    def __init__(self, driver):
        super(ProfileButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.support.v4.view.ViewPager//android.view.ViewGroup[1]/android.widget.ImageView")


class ProfileIcon(BaseButton):

    def __init__(self, driver):
        super(ProfileIcon, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.EditText/../android.view.ViewGroup")

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


class PublicKeyEditBox(BaseEditBox):

    def __init__(self, driver):
        super(PublicKeyEditBox, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.EditText[@NAF='true']")


class ConfirmPublicKeyButton(BaseButton):

    def __init__(self, driver):
        super(ConfirmPublicKeyButton, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.TextView[@text='Add new contact']"
                                        "/following-sibling::android.view.ViewGroup/"
                                        "android.widget.ImageView")


class ChatMessageInput(BaseEditBox):

    def __init__(self, driver):
        super(ChatMessageInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='chat-message-input']")


class SendMessageButton(BaseButton):

    def __init__(self, driver):
        super(SendMessageButton, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.FrameLayout//"
                                        "android.view.ViewGroup[3]//"
                                        "android.view.ViewGroup[2]//android.widget.ImageView")


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
        self.chat_message_input = ChatMessageInput(self.driver)
        self.send_message_button = SendMessageButton(self.driver)
