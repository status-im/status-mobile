from views.base_element import BaseButton, BaseEditBox
from views.base_view import BaseView


class PlusButton(BaseButton):
    def __init__(self, driver):
        super(PlusButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='+']")


class PublicKeyEditBox(BaseEditBox):
    def __init__(self, driver):
        super(PublicKeyEditBox, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('enter-contact-code-input')

    def set_value(self, value):
        for _ in range(2):
            if self.text != value:
                self.driver.info("Type '%s' to %s" % (value, self.name))
                self.find_element().set_value(value)


class ScanContactCodeButton(BaseEditBox):
    def __init__(self, driver):
        super(ScanContactCodeButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('scan-contact-code-button')


class ConfirmPublicKeyButton(BaseButton):
    def __init__(self, driver):
        super(ConfirmPublicKeyButton, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[2]')


class UsernameCheckbox(BaseButton):
    def __init__(self, driver, username):
        super(UsernameCheckbox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='%s']" % username)


class ChatNameEditBox(BaseEditBox):
    def __init__(self, driver):
        super(ChatNameEditBox, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-name-input')


class CreateButton(BaseButton):
    def __init__(self, driver):
        super(CreateButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('create-group-chat-button')

    def navigate(self):
        from views.chat_view import ChatView
        return ChatView(self.driver)


class ContactsView(BaseView):

    def __init__(self, driver):
        super(ContactsView, self).__init__(driver)
        self.driver = driver

        self.plus_button = PlusButton(self.driver)
        self.public_key_edit_box = PublicKeyEditBox(self.driver)
        self.scan_contact_code_button = ScanContactCodeButton(self.driver)
        self.confirm_public_key_button = ConfirmPublicKeyButton(self.driver)

        self.chat_name_editbox = ChatNameEditBox(self.driver)
        self.create_button = CreateButton(self.driver)

    def get_username_checkbox(self, username: str):
        return UsernameCheckbox(self.driver, username)
