import time
import base64
import zbarlight
from tests import info
from eth_keys import datatypes
from selenium.common.exceptions import NoSuchElementException, TimeoutException
from PIL import Image
from datetime import datetime
from io import BytesIO
from views.base_element import BaseButton, BaseElement, BaseEditBox, BaseText


class BackButton(BaseButton):
    def __init__(self, driver):
        super(BackButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='toolbar-back-button']")

    def click(self, times_to_click: int = 1):
        for _ in range(times_to_click):
            try:
                self.find_element().click()
                info('Tap on %s' % self.name)
            except (NoSuchElementException, TimeoutException):
                pass
        return self.navigate()


class AllowButton(BaseButton):
    def __init__(self, driver):
        super(AllowButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Allow']")

    def click(self):
        try:
            for _ in range(3):
                self.find_element().click()
                info('Tap on %s' % self.name)
        except NoSuchElementException:
            pass


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


class HomeButton(BaseButton):
    def __init__(self, driver):
        super(HomeButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Home']/..")

    def navigate(self):
        from views.home_view import HomeView
        return HomeView(self.driver)


class WalletButton(BaseButton):
    def __init__(self, driver):
        super(WalletButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Wallet']/..")

    def navigate(self):
        from views.wallet_view import WalletView
        return WalletView(self.driver)


class ProfileButton(BaseButton):
    def __init__(self, driver):
        super(ProfileButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Profile']/..")

    def navigate(self):
        from views.profile_view import ProfileView
        return ProfileView(self.driver)


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
            "//android.widget.TextView[@text='Done']")


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

        self.home_button = HomeButton(self.driver)
        self.button = WalletButton(self.driver)
        self.wallet_button = self.button
        self.profile_button = ProfileButton(self.driver)

        self.yes_button = YesButton(self.driver)
        self.no_button = NoButton(self.driver)
        self.back_button = BackButton(self.driver)
        self.allow_button = AllowButton(self.driver)
        self.deny_button = DenyButton(self.driver)
        self.continue_button_apk = ContinueButtonAPK(self.driver)
        self.ok_button_apk = OkButton(self.driver)
        self.next_button = NextButton(self.driver)
        self.save_button = SaveButton(self.driver)
        self.done_button = DoneButton(self.driver)

        self.apps_button = AppsButton(self.driver)
        self.status_app_icon = StatusAppIcon(self.driver)

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
        info("Tap 'Confirm' on native keyboard")
        self.driver.keyevent(66)

    def send_as_keyevent(self, string):
        keys = {'0': 7, '1': 8, '2': 9, '3': 10, '4': 11, '5': 12, '6': 13, '7': 14, '8': 15, '9': 16,

                ',': 55, '-': 69, '+': 81, '.': 56, '/': 76, '\\': 73, ';': 74, ' ': 62,
                '[': 71, ']': 72, '=': 70,

                'a': 29, 'b': 30, 'c': 31, 'd': 32, 'e': 33, 'f': 34, 'g': 35, 'h': 36, 'i': 37, 'j': 38,
                'k': 39, 'l': 40, 'm': 41, 'n': 42, 'o': 43, 'p': 44, 'q': 45, 'r': 46, 's': 47, 't': 48,
                'u': 49, 'v': 50, 'w': 51, 'x': 52, 'y': 53, 'z': 54}
        for i in string:
            info("Tap '%s' on native keyboard" % i)
            time.sleep(1)
            self.driver.keyevent(keys[i])

    def find_full_text(self, text, wait_time=60):
        info("Looking for full text: '%s'" % text)
        element = BaseElement(self.driver)
        element.locator = element.Locator.xpath_selector('//*[@text="' + text + '"]')
        return element.wait_for_element(wait_time)

    def find_text_part(self, text, wait_time=60):
        info("Looking for a text part: '%s'" % text)
        element = BaseElement(self.driver)
        element.locator = element.Locator.xpath_selector('//*[contains(@text, "' + text + '")]')
        return element.wait_for_element(wait_time)

    def element_by_text(self, text, element_type='base'):
        info("Looking for an element by text: '%s'" % text)
        element = self.element_types[element_type](self.driver)
        element.locator = element.Locator.xpath_selector('//*[@text="' + text + '"]')
        return element

    def element_by_text_part(self, text, element_type='base'):
        info("Looking for an element by text part: '%s'" % text)
        element = self.element_types[element_type](self.driver)
        element.locator = element.Locator.xpath_selector('//*[contains(@text, "' + text + '")]')
        return element

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

    def get_unique_amount(self):
        return '0.0%s' % datetime.now().strftime('%-m%-d%-H%-M%-S').strip('0')

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
