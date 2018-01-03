import logging
import time
import pytest
from views.base_element import *
from tests import api_requests
from datetime import datetime


class BackButton(BaseButton):
    def __init__(self, driver):
        super(BackButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='toolbar-back-button']")

    def click(self):
        self.wait_for_element(30)
        self.find_element().click()
        logging.info('Tap on %s' % self.name)
        return self.navigate()


class AllowButton(BaseButton):
    def __init__(self, driver):
        super(AllowButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Allow']")

    def click(self):
        try:
            for _ in range(3):
                self.find_element().click()
        except NoSuchElementException:
            pass
        logging.info('Tap on %s' % self.name)


class DenyButton(BaseButton):
    def __init__(self, driver):
        super(DenyButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Deny']")


class YesButton(BaseButton):
    def __init__(self, driver):
        super(YesButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Yes']")


class NoButton(BaseButton):
    def __init__(self, driver):
        super(NoButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='No']")


class OkButton(BaseButton):
    def __init__(self, driver):
        super(OkButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='OK']")


class ContinueButtonAPK(BaseButton):
    def __init__(self, driver):
        super(ContinueButtonAPK, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Continue']")


class ContactsButton(BaseButton):
    def __init__(self, driver):
        super(ContactsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Contacts']/..")

    def navigate(self):
        from views.contacts_view import ContactsView
        return ContactsView(self.driver)


class WalletButton(BaseButton):
    def __init__(self, driver):
        super(WalletButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Wallet']/..")

    def navigate(self):
        from views.wallet_view import WalletView
        return WalletView(self.driver)


class DiscoverButton(BaseButton):
    def __init__(self, driver):
        super(DiscoverButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Discover']/..")

    def navigate(self):
        from views.discover_view import DiscoverView
        return DiscoverView(self.driver)


class ChatsButton(BaseButton):
    def __init__(self, driver):
        super(ChatsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Chats']/..")


class SaveButton(BaseButton):
    def __init__(self, driver):
        super(SaveButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='SAVE']")


class NextButton(BaseButton):
    def __init__(self, driver):
        super(NextButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='NEXT']")


class ChatRequestInput(BaseEditBox):

    def __init__(self, driver):
        super(ChatRequestInput, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.EditText[@content-desc!='chat-message-input']")


class AppsButton(BaseButton):
    def __init__(self, driver):
        super(AppsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("Apps")


class StatusAppIcon(BaseButton):
    def __init__(self, driver):
        super(StatusAppIcon, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Status']")


class BaseView(object):
    def __init__(self, driver):
        self.driver = driver

        self.yes_button = YesButton(self.driver)
        self.no_button = NoButton(self.driver)
        self.back_button = BackButton(self.driver)
        self.allow_button = AllowButton(self.driver)
        self.deny_button = DenyButton(self.driver)
        self.continue_button_apk = ContinueButtonAPK(self.driver)
        self.ok_button_apk = OkButton(self.driver)
        self.next_button = NextButton(self.driver)
        self.apps_button = AppsButton(self.driver)
        self.status_app_icon = StatusAppIcon(self.driver)

        self.contacts_button = ContactsButton(self.driver)
        self.wallet_button = WalletButton(self.driver)
        self.discover_button = DiscoverButton(self.driver)
        self.chats_button = ChatsButton(self.driver)

        self.save_button = SaveButton(self.driver)

        self.chat_request_input = ChatRequestInput(self.driver)

        self.element_types = {
            'base': BaseElement,
            'button': BaseButton,
            'edit_box': BaseEditBox,
            'text': BaseText
        }

    @property
    def logcat(self):
        return self.driver.get_log("logcat")

    def confirm(self):
        logging.info("Tap 'Confirm' on native keyboard")
        self.driver.keyevent(66)

    def send_as_keyevent(self, string):
        keys = {'0': 7, '1': 8, '2': 9, '3': 10, '4': 11, '5': 12, '6': 13, '7': 14, '8': 15, '9': 16,

                ',': 55, '-': 69, '+': 81, '.': 56, '/': 76, '\\': 73, ';': 74, ' ': 62,
                '[': 71, ']': 72, '=': 70,

                'a': 29, 'b': 30, 'c': 31, 'd': 32, 'e': 33, 'f': 34, 'g': 35, 'h': 36, 'i': 37, 'j': 38,
                'k': 39, 'l': 40, 'm': 41, 'n': 42, 'o': 43, 'p': 44, 'q': 45, 'r': 46, 's': 47, 't': 48,
                'u': 49, 'v': 50, 'w': 51, 'x': 52, 'y': 53, 'z': 54}
        for i in string:
            logging.info("Tap '%s' on native keyboard" % i)
            time.sleep(1)
            self.driver.keyevent(keys[i])

    def find_full_text(self, text, wait_time=60):
        logging.info("Looking for full text: '%s'" % text)
        element = BaseElement(self.driver)
        element.locator = element.Locator.xpath_selector('//*[@text="' + text + '"]')
        return element.wait_for_element(wait_time)

    def find_text_part(self, text, wait_time=60):
        logging.info("Looking for a text part: '%s'" % text)
        element = BaseElement(self.driver)
        element.locator = element.Locator.xpath_selector('//*[contains(@text, "' + text + '")]')
        return element.wait_for_element(wait_time)

    def element_by_text(self, text, element_type='base'):
        logging.info("Looking for an element by text: '%s'" % text)
        element = self.element_types[element_type](self.driver)
        element.locator = element.Locator.xpath_selector('//*[@text="' + text + '"]')
        return element

    def element_by_text_part(self, text, element_type='base'):
        logging.info("Looking for an element by text part: '%s'" % text)
        element = self.element_types[element_type](self.driver)
        element.locator = element.Locator.xpath_selector('//*[contains(@text, "' + text + '")]')
        return element

    def get_chat_view(self):
        from views.chat_view import ChatView
        return ChatView(self.driver)

    def get_sign_in_view(self):
        from views.sign_in_view import SignInView
        return SignInView(self.driver)

    def get_send_transaction_view(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)

    def get_unique_amount(self):
        return '0.0%s' % datetime.now().strftime('%-m%-d%-H%-M%-S').strip('0')
