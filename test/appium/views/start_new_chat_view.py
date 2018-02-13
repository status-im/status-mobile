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


class NameEditBox(BaseEditBox):
    def __init__(self, driver):
        super(NameEditBox, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.EditText[@NAF='true']")


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


class EnterUrlEditbox(BaseEditBox):
    def __init__(self, driver):
        super(EnterUrlEditbox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.EditText")


class StartNewChatView(ContactsView):
    def __init__(self, driver):
        super(StartNewChatView, self).__init__(driver)

        self.add_new_contact = AddNewContactButton(self.driver)
        self.new_group_chat_button = NewGroupChatButton(self.driver)

        self.open_d_app_button = OpenDAapButton(self.driver)
        self.open_button = OpenButton(self.driver)

        self.name_edit_box = NameEditBox(self.driver)
        self.enter_url_editbox = EnterUrlEditbox(self.driver)
