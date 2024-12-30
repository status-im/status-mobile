import time

from views.base_element import EditBox, Button, BaseElement
from views.base_view import BaseView
from appium.webdriver.common.touch_action import TouchAction


class BaseWebView(BaseView):

    def __init__(self, driver):
        super().__init__(driver)

        self.progress_bar_icon = Button(self.driver, xpath="//android.widget.ProgressBar")
        self.options_button = Button(self.driver, accessibility_id="browser-options")
        self.open_tabs_button = Button(self.driver, accessibility_id="browser-open-tabs")
        self.open_new_tab_plus_button = Button(self.driver, accessibility_id="plus-button")


    def wait_for_d_aap_to_load(self, wait_time=35):
        self.driver.info("Waiting %ss for dapp to load" % wait_time)
        counter = 0
        while self.progress_bar_icon.is_element_displayed(5):
            time.sleep(1)
            counter += 1
            if counter > wait_time:
                self.driver.fail("Page is not loaded during %s seconds" % wait_time)


