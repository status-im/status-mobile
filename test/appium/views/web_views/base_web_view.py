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


class BaseWebView(BaseView):

    def __init__(self, driver):
        super(BaseWebView, self).__init__(driver)
        self.driver = driver

        self.progress_bar_icon = ProgressBarIcon(self.driver)

        self.web_link_edit_box = WebLinkEditBox(self.driver)
        self.back_to_home_button = BackToHomeButton(self.driver)

    def wait_for_d_aap_to_load(self, wait_time=35):
        counter = 0
        while self.progress_bar_icon.is_element_present(5):
            time.sleep(1)
            counter += 1
            if counter > wait_time:
                pytest.fail("Page is not loaded during %s seconds" % wait_time)
