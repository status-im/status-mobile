from views.base_element import *
from views.base_view import BaseView
from views.contacts_view import ContactsView


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


class NameEditBox(BaseEditBox):
    def __init__(self, driver):
        super(NameEditBox, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.EditText[@NAF='true']")


class StarNewChatView(ContactsView):
    def __init__(self, driver):
        super(StarNewChatView, self).__init__(driver)

        self.add_new_contact = AddNewContactButton(self.driver)
        self.new_group_chat_button = NewGroupChatButton(self.driver)

        self.name_edit_box = NameEditBox(self.driver)
