import logging
from selenium.common.exceptions import WebDriverException


def start_web_browser(driver):
    logging.info('Start web browser')
    try:
        driver.start_activity('org.chromium.webview_shell', 'WebViewBrowserActivity')
    except WebDriverException:
        pass
