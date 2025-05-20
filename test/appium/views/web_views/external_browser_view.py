import time

from appium.webdriver.applicationstate import ApplicationState
from selenium.common import TimeoutException

from base_test_case import Driver
from views.base_element import Button, EditBox
from views.base_view import BaseView

browser_app_package = 'com.android.chrome'


class BaseBrowserView(BaseView):
    def __init__(self, driver: Driver):
        super().__init__(driver)
        self.search_box_element = EditBox(self.driver, id='com.android.chrome:id/search_box_text')
        self.url_input = EditBox(self.driver, id='com.android.chrome:id/url_bar')
        self.no_thanks_button = Button(self.driver, id='com.android.chrome:id/negative_button')
        self.search_wallet_input = EditBox(
            self.driver, xpath="//*[@text='All Wallets']/../../../following-sibling::*//android.widget.EditText")
        self.status_app_button = Button(self.driver, xpath="//*[starts-with(@text,'Status Status')]")

    def wait_for_app_to_run(self, app_package: str, wait_time: int = 3):
        for _ in range(wait_time):
            if self.driver.query_app_state(app_package) == ApplicationState.RUNNING_IN_FOREGROUND:
                return
            time.sleep(1)
        raise TimeoutException(
            msg="Application %s is not running in foreground after %s sec" % (app_package, wait_time))

    def open_browser(self):
        self.driver.activate_app(browser_app_package)
        self.wait_for_app_to_run(browser_app_package)
        self.no_thanks_button.click_if_shown(2)

    def open_url(self, url: str):
        if self.url_input.is_element_displayed():
            self.url_input.click()
            self.url_input.clear()
            self.url_input.send_keys(url)
        else:
            self.search_box_element.send_keys(url)
        self.driver.press_keycode(66)


class BridgeStatusNetworkView(BaseBrowserView):
    def __init__(self, driver):
        super().__init__(driver)
        self.connect_wallet_button = Button(self.driver, xpath="//*[@resource-id='wallet-connect-btn']")
        self.all_wallets_button = Button(self.driver, xpath="//*[starts-with(@text,'All Wallets')]")
        self.amount_input = EditBox(self.driver, xpath="//*[@resource-id='amount-input']")
        self.bridge_button = Button(self.driver, xpath="//*[@text='BRIDGE']")

    def connect_status_wallet(self, refresh=True):
        status_app_package = self.driver.current_package
        self.open_browser()
        if refresh:
            self.open_url('https://bridge.status.network/')
            self.element_by_text('GOT IT').click_if_shown(2)
        self.connect_wallet_button.click()
        if self.status_app_button.is_element_displayed():
            self.status_app_button.click()
        else:
            self.all_wallets_button.click()
            self.search_wallet_input.wait_for_visibility_of_element(3)
            self.search_wallet_input.send_keys('Status')
            self.status_app_button.wait_and_click()
        self.wait_for_app_to_run(status_app_package)


class UniswapView(BaseBrowserView):

    def connect_status_wallet(self):
        status_app_package = self.driver.current_package
        self.open_browser()
        self.open_url('https://app.uniswap.org/swap')
        self.element_by_text('Connect wallet').wait_and_click()
        self.element_by_text('Other wallets').wait_and_click()
        self.element_by_text('WalletConnect').click()
        self.search_wallet_input.wait_for_visibility_of_element(3)
        self.search_wallet_input.send_keys('Status')
        self.status_app_button.wait_and_click()
        self.wait_for_app_to_run(status_app_package)
