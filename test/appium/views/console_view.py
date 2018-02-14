from tests import info
from selenium.common.exceptions import NoSuchElementException, TimeoutException
from views.base_element import BaseButton, BaseEditBox
from views.base_view import BaseView


class RequestPasswordIcon(BaseButton):

    def __init__(self, driver):
        super(RequestPasswordIcon, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('request-password')

    def click(self):
        self.wait_for_element(10)
        self.find_element().click()
        info('Tap on %s' % self.name)
        return self.navigate()


class RecoverButton(BaseButton):

    def __init__(self, driver):
        super(RecoverButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Recover']")

    def navigate(self):
        from views.recover_access_view import RecoverAccessView
        return RecoverAccessView(self.driver)


class ChatRequestInput(BaseEditBox):

    def __init__(self, driver):
        super(ChatRequestInput, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.EditText[@content-desc!='chat-message-input']")


class ConsoleView(BaseView):

    def __init__(self, driver):
        super(ConsoleView, self).__init__(driver)

        self.request_password_icon = RequestPasswordIcon(self.driver)
        self.recover_button = RecoverButton(self.driver)
        self.chat_request_input = ChatRequestInput(self.driver)

        self.accept_agreements()

    def accept_agreements(self):
        for i in self.ok_button_apk, self.continue_button_apk:
            try:
                i.click()
            except (NoSuchElementException, TimeoutException):
                pass

    def create_user(self):
        self.request_password_icon.click()
        self.chat_request_input.send_keys("qwerty1234")
        self.confirm()
        self.chat_request_input.send_keys("qwerty1234")
        self.confirm()
        self.find_full_text(
            "Here is your signing phrase. You will use it to verify your transactions. Write it down and keep it safe!")

    def recover_access(self, passphrase, password, username):
        recover_access_view = self.recover_button.click()
        recover_access_view.passphrase_input.send_keys(passphrase)
        recover_access_view.password_input.send_keys(password)
        recover_access_view.confirm_recover_access.click()
        recovered_user = recover_access_view.element_by_text(username, 'button')
        recover_access_view.confirm()
        recovered_user.click()
        recover_access_view.password_input.send_keys(password)
        recover_access_view.sign_in_button.click()
        recover_access_view.find_full_text('Wallet', 60)
