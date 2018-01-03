from views.base_view import *


class ProgressBarIcon(BaseElement):

    def __init__(self, driver):
        super(ProgressBarIcon, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.ProgressBar")


class BaseWebView(BaseView):

    def __init__(self, driver):
        super(BaseWebView, self).__init__(driver)
        self.driver = driver

        self.progress_bar_icon = ProgressBarIcon(self.driver)

    def wait_for_page_loaded(self, wait_time=20):
        counter = 0
        while self.progress_bar_icon.is_element_present(5):
            time.sleep(1)
            counter += 1
            if counter > wait_time:
                pytest.fail("Page is not loaded during %s seconds" % wait_time)

    def find_full_text(self, text, wait_time=60):
        element = BaseElement(self.driver)
        element.locator = element.Locator.xpath_selector('//*[@content-desc="' + text + '"]')
        return element.wait_for_element(wait_time)
