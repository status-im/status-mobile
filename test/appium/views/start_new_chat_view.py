from views.base_element import BaseButton, BaseEditBox
from views.contacts_view import ContactsView


class AddNewContactButton(BaseButton):
    def __init__(self, driver):
        super(AddNewContactButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='Start new chat']")


class NewGroupChatButton(BaseButton):

    def __init__(self, driver):
        super(NewGroupChatButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='Start group chat']")


class JoinPublicChatButton(BaseButton):

    def __init__(self, driver):
        super(JoinPublicChatButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='Join public chat']")


class ChatNameEditBox(BaseEditBox):
    def __init__(self, driver):
        super(ChatNameEditBox, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.EditText")


class OpenDAapButton(BaseButton):
    def __init__(self, driver):
        super(OpenDAapButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='Open ÐApp']")


class OpenButton(BaseButton):
    def __init__(self, driver):
        super(OpenButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='Open']")


class ConfirmButton(BaseButton):
    def __init__(self, driver):
        super(ConfirmButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[2]')


class EnterUrlEditbox(BaseEditBox):
    def __init__(self, driver):
        super(EnterUrlEditbox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.EditText")


class UsernameCheckbox(BaseButton):
    def __init__(self, driver, username):
        super(UsernameCheckbox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='%s']/../../android.widget.CheckBox" % username)


class StartNewChatView(ContactsView):
    def __init__(self, driver):
        super(StartNewChatView, self).__init__(driver)

        self.add_new_contact = AddNewContactButton(self.driver)
        self.new_group_chat_button = NewGroupChatButton(self.driver)
        self.join_public_chat_button = JoinPublicChatButton(self.driver)

        self.open_d_app_button = OpenDAapButton(self.driver)
        self.open_button = OpenButton(self.driver)

        self.chat_name_editbox = ChatNameEditBox(self.driver)
        self.enter_url_editbox = EnterUrlEditbox(self.driver)
        self.confirm_button = ConfirmButton(self.driver)

    def get_username_checkbox(self, username: str):
        return UsernameCheckbox(self.driver, username)
