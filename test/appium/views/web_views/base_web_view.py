import time
import pytest

from views.base_element import BaseElement, BaseEditBox, BaseButton, BaseText
from views.base_view import BaseView


class ProgressBarIcon(BaseElement):

    def __init__(self, driver):
        super(ProgressBarIcon, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.ProgressBar")


class WebLinkEditBox(BaseEditBox):

    def __init__(self, driver):
        super(WebLinkEditBox, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.EditText")


class BackToHomeButton(BaseButton):
    def __init__(self, driver):
        super(BackToHomeButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[1]')


class BrowserPreviousPageButton(BaseButton):
    def __init__(self, driver):
        super(BrowserPreviousPageButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('previous-page-button')


class BrowserNextPageButton(BaseButton):
    def __init__(self, driver):
        super(BrowserNextPageButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('next-page-button')


class BrowserRefreshPageButton(BaseButton):
    def __init__(self, driver):
        super(BrowserRefreshPageButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('refresh-page-button')


class WebViewBrowserButton(BaseButton):
    def __init__(self, driver):
        super(WebViewBrowserButton, self).__init__(driver)
        self.locator = self.Locator.text_part_selector('WebView Browser Tester')


class AlwaysButton(BaseButton):
    def __init__(self, driver):
        super(AlwaysButton, self).__init__(driver)
        self.locator = self.Locator.text_part_selector('ALWAYS')


class WebViewMenuButton(BaseButton):
    def __init__(self, driver):
        super(WebViewMenuButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-menu-button')


class URLEditBoxLockIcon(BaseEditBox):

    def __init__(self, driver):
        super(URLEditBoxLockIcon, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@content-desc='chat-menu-button']/../preceding-sibling::*[1]//*[@content-desc='icon']")


class PolicySummary(BaseElement):

    def __init__(self, driver):
        super(PolicySummary, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('Policy summary')


class BaseWebView(BaseView):

    def __init__(self, driver):
        super(BaseWebView, self).__init__(driver)
        self.driver = driver

        self.progress_bar_icon = ProgressBarIcon(self.driver)

        self.url_edit_box_lock_icon = URLEditBoxLockIcon(self.driver)
        self.policy_summary = PolicySummary(self.driver)
        self.back_to_home_button = BackToHomeButton(self.driver)
        self.browser_previous_page_button = BrowserPreviousPageButton(self.driver)
        self.browser_next_page_button = BrowserNextPageButton(self.driver)

        self.web_view_browser = WebViewBrowserButton(self.driver)
        self.web_view_menu_button = WebViewMenuButton(self.driver)
        self.always_button = AlwaysButton(self.driver)
        self.browser_refresh_page_button = BrowserRefreshPageButton(self.driver)

    def wait_for_d_aap_to_load(self, wait_time=35):
        counter = 0
        while self.progress_bar_icon.is_element_present(5):
            time.sleep(1)
            counter += 1
            if counter > wait_time:
                self.driver.fail("Page is not loaded during %s seconds" % wait_time)

    def open_in_webview(self):
        self.web_view_browser.click()
        self.always_button.click()
