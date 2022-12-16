import logging
from selenium.common.exceptions import WebDriverException


def start_web_browser(driver):
    logging.info('Start web browser')
    try:
        driver.start_activity('com.android.chrome', 'com.google.android.apps.chrome.Main')
    except WebDriverException:
        pass
