from views.base_element import BaseButton, BaseEditBox
from views.base_view import BaseView


class SimpleDAppButton(BaseButton):

    def __init__(self, driver):
        super(SimpleDAppButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Simple Dapp')

    def navigate(self):
        from views.web_views.simple_dapp import SimpleDAppWebView
        return SimpleDAppWebView(self.driver)


class PlusButton(BaseButton):
    def __init__(self, driver):
        super(PlusButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='+']")


class PublicKeyEditBox(BaseEditBox):
    def __init__(self, driver):
        super(PublicKeyEditBox, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('enter-contact-code-input')


class ConfirmPublicKeyButton(BaseButton):
    def __init__(self, driver):
        super(ConfirmPublicKeyButton, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[2]')


class ContactsView(BaseView):

    def __init__(self, driver):
        super(ContactsView, self).__init__(driver)
        self.driver = driver

        self.plus_button = PlusButton(self.driver)
        self.public_key_edit_box = PublicKeyEditBox(self.driver)
        self.confirm_public_key_button = ConfirmPublicKeyButton(self.driver)

        self.simple_dapp_button = SimpleDAppButton(self.driver)
