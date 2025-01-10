import random
import string
import time

from appium.webdriver import WebElement
from appium.webdriver.applicationstate import ApplicationState
from selenium.common.exceptions import NoSuchElementException, TimeoutException
from selenium.webdriver.support import expected_conditions
from selenium.webdriver.support.wait import WebDriverWait

from tests import common_password, transl
from views.base_element import Button, BaseElement, EditBox, Text, CheckBox


class BackButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="back-button")

    def click(self, times_to_click: int = 1):
        for _ in range(times_to_click):
            self.find_element().click()
        return self.navigate()


class AllowButton(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="allow", uppercase=True)

    def click(self, times_to_click=3):
        try:
            for _ in range(times_to_click):
                self.find_element().click()
        except NoSuchElementException:
            pass


class UnreadMessagesCountText(Text):
    def __init__(self, driver, parent_locator: str):
        super().__init__(driver,
                         xpath="%s//*[@resource-id='counter-component']/android.widget.TextView" % parent_locator)


class TabButton(Button):
    @property
    def counter(self):
        return UnreadMessagesCountText(self.driver, parent_locator='//*[@content-desc="%s"]' % self.accessibility_id)


class CommunitiesTab(TabButton):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="communities-stack-tab")


class ChatsTab(TabButton):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="chats-stack-tab")

    def navigate(self):
        from views.home_view import HomeView
        return HomeView(self.driver)


class WalletTab(TabButton):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="wallet-stack-tab")

    def navigate(self):
        from views.wallet_view import WalletView
        return WalletView(self.driver)


class BrowserTab(TabButton):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="browser-stack-tab")

    def navigate(self):
        from views.dapps_view import DappsView
        return DappsView(self.driver)


class ProfileButton(TabButton):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="open-profile")

    def navigate(self):
        from views.profile_view import ProfileView
        return ProfileView(self.driver)

    def click(self, desired_element_text='privacy'):
        if not self.is_element_displayed():
            ChatsTab(self.driver).click()
        from views.profile_view import ProfileView
        self.click_until_presence_of_element(ProfileView(self.driver).profile_password_button)
        return self.navigate()


class SendMessageButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="send-message-button")


class SlideButton(Button):
    def __init__(self, driver):
        super().__init__(driver, xpath="//*[@resource-id='slide-button-track']")

    def slide(self):
        self.swipe_right_on_element(width_percentage=1.3, start_x=100)


class BaseView(object):
    def __init__(self, driver):
        self.driver = driver
        self.send_message_button = SendMessageButton(self.driver)
        self.password_input = EditBox(self.driver, accessibility_id="password-input")
        from views.sign_in_view import LogInButton
        self.login_button = LogInButton(self.driver)
        self.continue_button = Button(self.driver, accessibility_id='continue-button')

        # Tabs
        self.communities_tab = CommunitiesTab(self.driver)
        self.profile_button = ProfileButton(self.driver)
        self.chats_tab = ChatsTab(self.driver)
        self.wallet_tab = WalletTab(self.driver)
        self.browser_tab = BrowserTab(self.driver)

        # Floating screens (introduced by https://github.com/status-im/status-mobile/pull/16438)
        self.chat_floating_screen = BaseElement(self.driver, accessibility_id=":chat-floating-screen")
        self.community_floating_screen = BaseElement(self.driver,
                                                     accessibility_id=":community-overview-floating-screen")
        self.discover_communities_floating_screen = BaseElement(self.driver,
                                                                accessibility_id=":discover-communities-floating-screen")

        self.jump_to_button = Button(self.driver, accessibility_id="jump-to")
        self.yes_button = Button(self.driver, xpath="//*[@text='YES' or @text='GOT IT']")
        self.no_button = Button(self.driver, translation_id="no")
        self.back_button = BackButton(self.driver)
        self.allow_button = AllowButton(self.driver)
        self.allow_all_button = Button(self.driver, xpath="//*[@text='Allow all']")
        self.allow_all_the_time = Button(self.driver, xpath="//*[@text='Allow all the time']")
        self.deny_button = Button(self.driver, translation_id="deny", uppercase=True)
        self.continue_button = Button(self.driver, translation_id="continue", uppercase=True)
        self.ok_button = Button(self.driver, xpath="//*[@text='OK' or @text='Ok']")
        self.add_button = Button(self.driver, translation_id="add")
        self.save_button = Button(self.driver, translation_id="save")
        self.done_button = Button(self.driver, accessibility_id="Done")
        self.delete_button = Button(self.driver, translation_id="delete", uppercase=True)
        self.ok_continue_button = Button(self.driver, accessibility_id="Okay, continue")
        self.confirm_button = Button(self.driver, accessibility_id='Confirm')

        self.native_close_button = Button(self.driver, id="android:id/aerr_close")
        self.close_button = Button(self.driver, accessibility_id="back-button")
        self.navigate_up_button = Button(self.driver, accessibility_id="Navigate Up")
        self.ok_got_it_button = Button(self.driver, accessibility_id="Okay, got it")
        self.status_in_background_button = Button(self.driver, xpath="//*[contains(@content-desc,'Status')]")
        self.cancel_button = Button(self.driver, translation_id="cancel", uppercase=True)
        self.search_input = EditBox(self.driver, accessibility_id="search-input")
        self.toast_content_element = BaseElement(self.driver, accessibility_id="toast-content")
        self.next_button = Button(self.driver, accessibility_id="next-button")
        self.native_alert_title = Text(self.driver, xpath="//*[@resource-id='android:id/alertTitle']")

        # share contact screen
        self.show_qr_code_button = Button(self.driver, accessibility_id="show-qr-button")

        self.link_to_profile_button = Button(self.driver, accessibility_id="share-qr-code-info-text")
        self.sharing_text_native = Text(self.driver, xpath="//*[@resource-id='android:id/content_preview_text']")

        # checkboxes and toggles
        self.checkbox_button = CheckBox(
            self.driver, xpath="//*[@content-desc='checkbox-off'][@resource-id='checkbox-component']")
        self.slide_button_track = SlideButton(self.driver)

        # external browser
        self.open_in_android_button = Button(self.driver, translation_id="browsing-open-in-android-web-browser")

        self.element_types = {
            'base': BaseElement,
            'button': Button,
            'edit_box': EditBox,
            'text': Text
        }

    @staticmethod
    def get_translation_by_key(translation_id):
        return transl[translation_id]

    def confirm(self):
        self.driver.info("Tap 'Confirm' on native keyboard")
        self.driver.press_keycode(66)

    def just_fyi(self, some_str):
        self.driver.info('# STEP: %s' % some_str, device=False)
        # self.driver.execute_script("sauce:context=STEP: %s" % some_str)
        self.driver.log_event("appium", "STEP: %s" % some_str)

    def hide_keyboard_if_shown(self):
        if self.driver.is_keyboard_shown():
            self.click_system_back_button()

    def click_system_back_button(self, times=1):
        self.driver.info('Click system back button')
        for _ in range(times):
            self.driver.press_keycode(4)

    def _navigate_back_to_view(self, element, attempts=3):
        counter = 0
        while not element.is_element_displayed(1) and counter <= attempts:
            self.driver.press_keycode(4)
            try:
                element.wait_for_element(2)
                return self
            except (NoSuchElementException, TimeoutException):
                counter += 1
        else:
            self.driver.info("Could not reach %s by pressing system back button" % element.name)

    def navigate_back_to_home_view(self):
        while not self.chat_floating_screen.is_element_disappeared(1) \
                or not self.community_floating_screen.is_element_disappeared(1) \
                or not self.discover_communities_floating_screen.is_element_disappeared(1):
            self.driver.press_keycode(4)
        self._navigate_back_to_view(self.chats_tab)

    def navigate_back_to_chat_view(self):
        self._navigate_back_to_view(self.get_chat_view().chat_message_input)

    def navigate_back_to_wallet_view(self, attempts=3):
        element = self.get_wallet_view().network_drop_down
        self._navigate_back_to_view(element)

    def click_system_home_button(self):
        self.driver.info('Press system Home button')
        self.driver.press_keycode(3)

    def put_app_to_background(self):
        self.driver.info('App to background')
        self.driver.press_keycode(187)

    def send_as_keyevent(self, keyevent):
        self.driver.info("Sending as keyevent `%s`" % keyevent)
        keys = {'0': 7, '1': 8, '2': 9, '3': 10, '4': 11, '5': 12, '6': 13, '7': 14, '8': 15, '9': 16,

                ',': 55, '-': 69, '+': 81, '.': 56, '/': 76, '\\': 73, ';': 74, ' ': 62,
                '[': 71, ']': 72, '=': 70, '\n': 66, '_': [69, 5], ':': [74, 5],

                'a': 29, 'b': 30, 'c': 31, 'd': 32, 'e': 33, 'f': 34, 'g': 35, 'h': 36, 'i': 37, 'j': 38,
                'k': 39, 'l': 40, 'm': 41, 'n': 42, 'o': 43, 'p': 44, 'q': 45, 'r': 46, 's': 47, 't': 48,
                'u': 49, 'v': 50, 'w': 51, 'x': 52, 'y': 53, 'z': 54}
        time.sleep(3)
        for i in keyevent:
            if i.isalpha() and i.isupper():
                keycode, metastate = keys[i.lower()], 64  # META_SHIFT_LEFT_ON Constant Value: 64. Example: i='n' -> 'N'
            elif type(keys[i]) is list:
                keycode, metastate = keys[i][0], keys[i][1]
            else:
                keycode, metastate = keys[i], None
            self.driver.press_keycode(keycode=keycode, metastate=metastate)

    def element_by_text(self, text, element_type='button'):
        element = self.element_types[element_type](self.driver)
        element.locator = '//*[@text="%s"]' % text
        return element

    def element_by_text_part(self, text, element_type='button'):
        element = self.element_types[element_type](self.driver)
        element.locator = '//*[contains(@text, "' + text + '")]'
        return element

    def element_starts_with_text(self, text, element_type='button'):
        element = self.element_types[element_type](self.driver, xpath="//*[starts-with(@text,'%s')]" % text)
        return element

    def element_by_translation_id(self, translation_id, element_type='button', uppercase=False):
        element = self.element_types[element_type](self.driver, translation_id=translation_id, uppercase=uppercase)
        return element

    def get_dapp_view(self):
        from views.dapps_view import DappsView
        return DappsView(self.driver)

    def get_home_view(self):
        from views.home_view import HomeView
        return HomeView(self.driver)

    def get_chat_view(self):
        from views.chat_view import ChatView
        return ChatView(self.driver)

    def get_community_view(self):
        from views.chat_view import CommunityView
        return CommunityView(self.driver)

    def get_sign_in_view(self):
        from views.sign_in_view import SignInView
        return SignInView(self.driver)

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
    def get_random_chat_name():
        return ''.join(random.choice(string.ascii_lowercase) for _ in range(7))

    def click_on_floating_jump_to(self):
        self.hide_keyboard_if_shown()
        if self.chat_floating_screen.is_element_displayed(1):
            Button(self.driver, xpath='//*[@content-desc="%s"]//*[@content-desc="%s"]' %
                                      (self.chat_floating_screen.accessibility_id,
                                       self.jump_to_button.accessibility_id)).click()
        elif self.community_floating_screen.is_element_displayed(1):
            Button(self.driver, xpath='//*[@content-desc="%s"]//*[@content-desc="%s"]' %
                                      (self.community_floating_screen.accessibility_id,
                                       self.jump_to_button.accessibility_id)).click()
        else:
            self.jump_to_button.click()

    def wait_for_application_to_be_running(self, app_package: str, wait_time: int = 3):
        for _ in range(wait_time):
            if self.driver.query_app_state(app_package) == ApplicationState.RUNNING_IN_FOREGROUND:
                return
            time.sleep(1)
        raise TimeoutException(msg="Status app is not running in foreground after %s sec" % wait_time)

    def wait_for_application_to_not_run(self, app_package: str, wait_time: int = 3):
        for _ in range(wait_time):
            if self.driver.query_app_state(app_package) == ApplicationState.NOT_RUNNING:
                return
            time.sleep(1)
        raise TimeoutException(msg="Status app is not terminated after %s sec" % wait_time)

    def reopen_app(self, password=common_password, sign_in=True, user_name=None):
        app_package = self.driver.current_package
        self.driver.terminate_app(app_package)
        self.wait_for_application_to_not_run(app_package=app_package)
        self.driver.activate_app(app_package)
        if sign_in:
            sign_in_view = self.get_sign_in_view()
            sign_in_view.sign_in(user_name, password)

    def click_upon_push_notification_by_text(self, text):
        element = self.element_by_text_part(text)
        self.driver.info("Click on PN with text: '%s'" % element.exclude_emoji(text))
        element.click()
        return self.get_chat_view()

    def open_notification_bar(self):
        self.driver.open_notifications()

    def tap_by_coordinates(self, x, y):
        self.driver.tap(positions=[(x, y)])

    def wait_for_current_package_to_be(self, expected_package_name: str, timeout: int = 10):
        start_time = time.time()
        while time.time() - start_time <= timeout:
            package = self.driver.current_package
            if package == expected_package_name:
                return
            time.sleep(1)
        raise TimeoutException("Driver current package is '%s' after %s seconds" % (package, timeout))

    def wait_for_staleness_of_element(self, element_instance: WebElement, seconds=10):
        try:
            return WebDriverWait(self.driver, seconds).until(expected_conditions.staleness_of(element_instance))
        except TimeoutException:
            raise TimeoutException(
                "Device %s: expected element is not stale after %s seconds" % (self.driver.number, seconds)) from None

    def open_link_from_google_search_app(self, link_text: str, app_package: str):
        Button(self.driver, accessibility_id="Search").click()
        EditBox(self.driver, xpath="//android.widget.EditText").send_keys(link_text)
        self.driver.press_keycode(66)
        text_to_click = "Status PR" if app_package.endswith(".pr") else "Status"
        Button(self.driver,
               xpath="//*[@resource-id='android:id/resolver_list']//*[@text='%s']" % text_to_click).click_if_shown()
        Button(self.driver, xpath="//*[@resource-id='android:id/button_once']").click()
