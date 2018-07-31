from views.base_view import *
import pytest


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
        self.locator = self.Locator.accessibility_id('previou-page-button')


class BrowserNextPageButton(BaseButton):
    def __init__(self, driver):
        super(BrowserNextPageButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('next-page-button')


class BrowserRefreshPageButton(BaseButton):
    def __init__(self, driver):
        super(BrowserRefreshPageButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@content-desc='next-page-button']/following-sibling::*/*[@content-desc='icon']")


class WebViewBrowserButton(BaseButton):
    def __init__(self, driver):
        super(WebViewBrowserButton, self).__init__(driver)
        self.locator = self.Locator.text_part_selector('WebView Browser Tester')


class AlwaysButton(BaseButton):
    def __init__(self, driver):
        super(AlwaysButton, self).__init__(driver)
        self.locator = self.Locator.text_part_selector('ALWAYS')


class BrowserCrossIcon(BaseButton):

    def __init__(self, driver):
        super(BrowserCrossIcon, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="icon"])[1]')

    def navigate(self):
        from views.home_view import HomeView
        return HomeView(self.driver)


class BaseWebView(BaseView):

    def __init__(self, driver):
        super(BaseWebView, self).__init__(driver)
        self.driver = driver

        self.progress_bar_icon = ProgressBarIcon(self.driver)

        self.web_link_edit_box = WebLinkEditBox(self.driver)
        self.back_to_home_button = BackToHomeButton(self.driver)
        self.browser_previous_page_button = BrowserPreviousPageButton(self.driver)
        self.browser_next_page_button = BrowserNextPageButton(self.driver)

        self.web_view_browser = WebViewBrowserButton(self.driver)
        self.always_button = AlwaysButton(self.driver)
        self.browser_cross_icon = BrowserCrossIcon(self.driver)
        self.browser_refresh_page_button = BrowserRefreshPageButton(self.driver)

    def wait_for_d_aap_to_load(self, wait_time=35):
        counter = 0
        while self.progress_bar_icon.is_element_present(5):
            time.sleep(1)
            counter += 1
            if counter > wait_time:
                pytest.fail("Page is not loaded during %s seconds" % wait_time)

    def open_in_webview(self):
        self.web_view_browser.click()
        self.always_button.click()
