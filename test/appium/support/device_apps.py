from selenium.common.exceptions import WebDriverException

from tests import info


def start_web_browser(driver):
    info('Start web browser')
    try:
        driver.start_activity('org.chromium.webview_shell', 'WebViewBrowserActivity')
    except WebDriverException:
        pass
