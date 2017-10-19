from views.base_view import BaseViewObject
from views.base_element import *
from tests import tests_data


class ChatRequestInput(BaseEditBox):

    def __init__(self, driver):
        super(ChatRequestInput, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.EditText[@content-desc!='chat-message-input']")


class RequestPasswordIcon(BaseButton):

    def __init__(self, driver):
        super(RequestPasswordIcon, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='request-password']")

    def click(self):
        self.wait_for_element(10)
        self.find_element().click()
        logging.info('Tap on %s' % self.name)
        return self.navigate()


class HomeView(BaseViewObject):

    def __init__(self, driver):
        super(HomeView, self).__init__(driver)
        for i in self.ok_button_apk, self.continue_button_apk:
            try:
                i.click()
            except (NoSuchElementException, TimeoutException):
                pass
        self.chat_request_input = ChatRequestInput(driver)
        self.request_password_icon = RequestPasswordIcon(driver)
