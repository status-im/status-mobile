import random
import string
import time
import base64
import pytest
import re
import zbarlight
from tests import common_password, test_fairy_warning_text
from eth_keys import datatypes
from selenium.common.exceptions import NoSuchElementException, TimeoutException, StaleElementReferenceException
from PIL import Image
from datetime import datetime
from io import BytesIO
from views.base_element import BaseButton, BaseElement, BaseEditBox, BaseText


class BackButton(BaseButton):
    def __init__(self, driver):
        super(BackButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('back-button')

    def click(self, times_to_click: int = 1):
        for _ in range(times_to_click):
            self.find_element().click()
            self.driver.info('Tap on %s' % self.name)
        return self.navigate()


class AllowButton(BaseButton):
    def __init__(self, driver):
        super(AllowButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Allow' or @text='ALLOW']")

    def click(self):
        try:
            for _ in range(3):
                self.find_element().click()
                self.driver.info('Tap on %s' % self.name)
        except NoSuchElementException:
            pass


class DenyButton(BaseButton):
    def __init__(self, driver):
        super(DenyButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='DENY']")


class DeleteButton(BaseButton):
    def __init__(self, driver):
        super(DeleteButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='DELETE']")


class YesButton(BaseButton):
    def __init__(self, driver):
        super(YesButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='YES']")


class NoButton(BaseButton):
    def __init__(self, driver):
        super(NoButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='No']")


class OkButton(BaseButton):
    def __init__(self, driver):
        super(OkButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='OK']")


class ContinueButton(BaseButton):
    def __init__(self, driver):
        super(ContinueButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='CONTINUE' or @text='Continue']")


class TabButton(BaseButton):

    @property
    def counter(self):
        class Counter(BaseText):
            def __init__(self, driver, parent_locator):
                super(Counter, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(
                    "//*[@content-desc='%s']//android.view.ViewGroup[2]/android.widget.TextView" % parent_locator)

        return Counter(self.driver, self.locator.value)


class HomeButton(TabButton):
    def __init__(self, driver):
        super(HomeButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('home-tab-button')

    def navigate(self):
        from views.home_view import HomeView
        return HomeView(self.driver)

    def click(self):
        from views.home_view import PlusButton
        self.click_until_presence_of_element(PlusButton(self.driver))
        return self.navigate()


class WalletButton(TabButton):
    def __init__(self, driver):
        super(WalletButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('wallet-tab-button')

    def navigate(self):
        from views.wallet_view import WalletView
        return WalletView(self.driver)

    def click(self):
        self.driver.info('Tap on %s' % self.name)
        from views.wallet_view import SetUpButton, SendTransactionButton
        for _ in range(3):
            self.find_element().click()
            if SetUpButton(self.driver).is_element_displayed() or SendTransactionButton(
                    self.driver).is_element_displayed():
                return self.navigate()


class ProfileButton(TabButton):
    def __init__(self, driver):
        super(ProfileButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('profile-tab-button')

    def navigate(self):
        from views.profile_view import ProfileView
        return ProfileView(self.driver)

    def click(self):
        from views.profile_view import ShareMyContactKeyButton
        self.click_until_presence_of_element(ShareMyContactKeyButton(self.driver))
        return self.navigate()


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


class DoneButton(BaseButton):
    def __init__(self, driver):
        super(DoneButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='DONE']")


class AppsButton(BaseButton):
    def __init__(self, driver):
        super(AppsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("Apps")


class StatusAppIcon(BaseButton):
    def __init__(self, driver):
        super(StatusAppIcon, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Status']")


class SendMessageButton(BaseButton):
    def __init__(self, driver):
        super(SendMessageButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("send-message-button")

    def click(self):
        self.find_element().click()
        self.driver.info('Tap on %s' % self.name)


class ConnectionStatusText(BaseText):
    def __init__(self, driver):
        super(ConnectionStatusText, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@content-desc='connection-status-text']/android.widget.TextView")


class TestFairyWarning(BaseText):
    def __init__(self, driver):
        super(TestFairyWarning, self).__init__(driver)
        self.locator = self.Locator.text_selector(test_fairy_warning_text)
        self.is_shown = bool()


class OkContinueButton(BaseButton):

    def __init__(self, driver):
        super(OkContinueButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='OK, CONTINUE']")


class DiscardButton(BaseButton):

    def __init__(self, driver):
        super(DiscardButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='DISCARD']")


class ConfirmButton(BaseButton):
    def __init__(self, driver):
        super(ConfirmButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='CONFIRM']")


class ProgressBar(BaseElement):
    def __init__(self, driver, parent_locator: str = ''):
        super(ProgressBar, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(parent_locator + '//android.widget.ProgressBar')


class WalletModalButton(BaseButton):
    def __init__(self, driver):
        super(WalletModalButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('wallet-modal-button')

    def navigate(self):
        from views.wallet_view import WalletView
        return WalletView(self.driver)


class CrossIcon(BaseButton):

    def __init__(self, driver):
        super(CrossIcon, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[1]')


class BaseView(object):
    def __init__(self, driver):
        self.driver = driver
        self.send_message_button = SendMessageButton(self.driver)
        self.home_button = HomeButton(self.driver)
        self.wallet_button = WalletButton(self.driver)
        self.profile_button = ProfileButton(self.driver)

        self.yes_button = YesButton(self.driver)
        self.no_button = NoButton(self.driver)
        self.back_button = BackButton(self.driver)
        self.allow_button = AllowButton(self.driver)
        self.deny_button = DenyButton(self.driver)
        self.continue_button = ContinueButton(self.driver)
        self.ok_button = OkButton(self.driver)
        self.next_button = NextButton(self.driver)
        self.save_button = SaveButton(self.driver)
        self.done_button = DoneButton(self.driver)
        self.delete_button = DeleteButton(self.driver)
        self.ok_continue_button = OkContinueButton(self.driver)
        self.discard_button = DiscardButton(self.driver)
        self.confirm_button = ConfirmButton(self.driver)
        self.connection_status = ConnectionStatusText(self.driver)
        self.cross_icon = CrossIcon(self.driver)

        self.apps_button = AppsButton(self.driver)
        self.status_app_icon = StatusAppIcon(self.driver)

        self.test_fairy_warning = TestFairyWarning(self.driver)

        self.wallet_modal_button = WalletModalButton(self.driver)

        self.element_types = {
            'base': BaseElement,
            'button': BaseButton,
            'edit_box': BaseEditBox,
            'text': BaseText
        }

    def accept_agreements(self):
        iterations = int()
        from views.sign_in_view import CreateAccountButton, PasswordInput
        while iterations <= 3 and not (CreateAccountButton(self.driver).is_element_displayed(2) or PasswordInput(
                self.driver).is_element_displayed(2)):
            for button in self.ok_button, self.continue_button:
                try:
                    button.wait_for_element(3)
                    button.click()
                except (NoSuchElementException, TimeoutException):
                    pass
            iterations += 1

    @property
    def logcat(self):
        logcat = self.driver.get_log("logcat")
        if len(logcat) > 1000:
            return str([i for i in logcat if 'appium' not in str(i).lower()])
        raise TimeoutError('Logcat is empty')

    def confirm(self):
        self.driver.info("Tap 'Confirm' on native keyboard")
        self.driver.press_keycode(66)

    def confirm_until_presence_of_element(self, desired_element, attempts=3):
        counter = 0
        while not desired_element.is_element_present(1) and counter <= attempts:
            try:
                self.confirm()
                self.driver.info('Wait for %s' % desired_element.name)
                desired_element.wait_for_element(5)
                return
            except TimeoutException:
                counter += 1

    def click_system_back_button(self):
        self.driver.info('Click system back button')
        self.driver.press_keycode(4)

    def cut_text(self):
        self.driver.info('Cut text')
        self.driver.press_keycode(277)

    def copy_text(self):
        self.driver.info('Copy text')
        self.driver.press_keycode(278)

    def paste_text(self):
        self.driver.info('Paste text')
        self.driver.press_keycode(279)

    def send_as_keyevent(self, string):
        keys = {'0': 7, '1': 8, '2': 9, '3': 10, '4': 11, '5': 12, '6': 13, '7': 14, '8': 15, '9': 16,

                ',': 55, '-': 69, '+': 81, '.': 56, '/': 76, '\\': 73, ';': 74, ' ': 62,
                '[': 71, ']': 72, '=': 70, '\n': 66, '_': [69, 5], ':': [74, 5],

                'a': 29, 'b': 30, 'c': 31, 'd': 32, 'e': 33, 'f': 34, 'g': 35, 'h': 36, 'i': 37, 'j': 38,
                'k': 39, 'l': 40, 'm': 41, 'n': 42, 'o': 43, 'p': 44, 'q': 45, 'r': 46, 's': 47, 't': 48,
                'u': 49, 'v': 50, 'w': 51, 'x': 52, 'y': 53, 'z': 54}
        time.sleep(3)
        self.driver.info("Enter '%s' using native keyboard" % string)
        for i in string:
            if type(keys[i]) is list:
                keycode, metastate = keys[i][0], keys[i][1]
            else:
                keycode, metastate = keys[i], None
            self.driver.press_keycode(keycode=keycode, metastate=metastate)

    def find_full_text(self, text, wait_time=60):
        self.driver.info("Looking for full text: '%s'" % text)
        element = BaseElement(self.driver)
        element.locator = element.Locator.text_selector(text)
        return element.wait_for_element(wait_time)

    def find_text_part(self, text, wait_time=60):
        self.driver.info("Looking for a text part: '%s'" % text)
        element = BaseElement(self.driver)
        element.locator = element.Locator.text_part_selector(text)
        return element.wait_for_element(wait_time)

    def element_by_text(self, text, element_type='button'):
        self.driver.info("Looking for an element by text: '%s'" % text)
        element = self.element_types[element_type](self.driver)
        element.locator = element.Locator.text_selector(text)
        return element

    def element_by_text_part(self, text, element_type='button'):
        self.driver.info("Looking for an element by text part: '%s'" % text)
        element = self.element_types[element_type](self.driver)
        element.locator = element.Locator.text_part_selector(text)
        return element

    def element_starts_with_text(self, text, element_type='base'):
        self.driver.info("Looking for full text: '%s'" % text)
        element = self.element_types[element_type](self.driver)
        element.locator = element.Locator.xpath_selector("//*[starts-with(@text,'%s')]" % text)
        return element

    def wait_for_element_starts_with_text(self, text, wait_time=60):
        self.driver.info("Looking for full text: '%s'" % text)
        element = BaseElement(self.driver)
        element.locator = element.Locator.xpath_selector("//*[starts-with(@text,'%s')]" % text)
        return element.wait_for_element(wait_time)

    def element_by_accessibility_id(self, accessibility_id, element_type='button'):
        self.driver.info("Looking for an element by accessibility id: '%s'" % accessibility_id)
        element = self.element_types[element_type](self.driver)
        element.locator = element.Locator.accessibility_id(accessibility_id)
        return element

    def element_by_xpath(self, xpath, element_type='button'):
        self.driver.info("Looking for an element by xpath: '%s'" % xpath)
        element = self.element_types[element_type](self.driver)
        element.locator = element.Locator.xpath_selector(xpath)
        return element

    def swipe_down(self):
        self.driver.swipe(500, 500, 500, 1000)

    def get_home_view(self):
        from views.home_view import HomeView
        return HomeView(self.driver)

    def get_chat_view(self):
        from views.chat_view import ChatView
        return ChatView(self.driver)

    def get_sign_in_view(self):
        from views.sign_in_view import SignInView
        return SignInView(self.driver)

    def get_send_transaction_view(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)

    def get_base_web_view(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)

    def get_profile_view(self):
        from views.profile_view import ProfileView
        return ProfileView(self.driver)

    def get_wallet_view(self):
        from views.wallet_view import WalletView
        return WalletView(self.driver)

    @staticmethod
    def get_unique_amount():
        return '0.0%s' % datetime.now().strftime('%-m%-d%-H%-M%-S').strip('0')

    @staticmethod
    def get_public_chat_name():
        return ''.join(random.choice(string.ascii_lowercase) for _ in range(7))

    def get_text_from_qr(self):
        image = Image.open(BytesIO(base64.b64decode(self.driver.get_screenshot_as_base64())))
        image.load()
        try:
            return str(zbarlight.scan_codes('qrcode', image)[0])[2:][:132]
        except IndexError:
            raise BaseException('No data in QR code')

    def public_key_to_address(self, public_key):
        raw_public_key = bytearray.fromhex(public_key.replace('0x04', ''))
        return datatypes.PublicKey(raw_public_key).to_address()[2:]

    def get_back_to_home_view(self):
        counter = 0
        while not self.home_button.is_element_displayed(2):
            try:
                if counter >= 5:
                    return
                self.back_button.click()
            except (NoSuchElementException, TimeoutException):
                counter += 1
        return self.get_home_view()

    def relogin(self, password=common_password):
        self.get_back_to_home_view()
        profile_view = self.profile_button.click()
        profile_view.logout()
        sign_in_view = self.get_sign_in_view()
        sign_in_view.sign_in(password)

    def get_public_key(self):
        profile_view = self.profile_button.click()
        profile_view.share_my_contact_key_button.click()
        profile_view.public_key_text.wait_for_visibility_of_element()
        public_key = profile_view.public_key_text.text
        profile_view.cross_icon.click()
        return public_key

    def share_via_messenger(self):
        self.element_by_text('Messenger').click()
        self.element_by_text('NEW MESSAGE').click()
        self.send_as_keyevent('+0')
        self.confirm()
        self.element_by_accessibility_id('Send Message').click()

    def reconnect(self):
        connect_status = self.connection_status
        for i in range(3):
            if connect_status.is_element_displayed(5, ignored_exceptions=StaleElementReferenceException):
                if 'Tap to reconnect' in connect_status.text:
                    try:
                        connect_status.click()
                    except AttributeError:
                        pass
                    try:
                        connect_status.wait_for_invisibility_of_element()
                    except TimeoutException as e:
                        if i == 2:
                            e.msg = "Device %s: Can't reconnect to mail server after 3 attempts" % self.driver.number
                            raise e

    def check_no_values_in_logcat(self, **kwargs):
        logcat = self.logcat
        for key, value in kwargs.items():
            if re.findall('\W%s$|\W%s\W' % (value, value), logcat):
                pytest.fail('%s in logcat!!!' % key.capitalize(), pytrace=False)
