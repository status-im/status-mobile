from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


class ChromeBrowserHandler:
    """
    Handles Chrome browser activities such as dismissing notifications,
    switching to WebView context, and opening URLs.
    """

    def __init__(self, driver):
        """
        Initializes the ChromeBrowserHandler and dismisses Chrome notifications if not already dismissed.
        :param driver: The Appium driver instance.
        """
        self.driver = driver
        self.notifications_dismissed = False  # Tracks whether notifications have been dismissed.

        # Dismiss notifications the first time this class is initialized.
        if not self.notifications_dismissed:
            self._dismiss_notifications()
            self.notifications_dismissed = True

    def _dismiss_notifications(self):
        """
        Dismisses the Chrome notifications popup if it is displayed.
        Called automatically during initialization.
        """
        try:
            self.driver.info("Switching to Chrome browser activity...")
            self.driver.start_activity("com.android.chrome", "com.google.android.apps.chrome.Main")

            # Wait for the notifications popup and click the "No Thanks" or negative button
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located((By.ID, "com.android.chrome:id/negative_button"))
            ).click()
            self.driver.info("Chrome notifications popup dismissed.")

        except Exception as e:
            self.driver.info(f"No notifications popup detected or error occurred: {e}")

    def open_url(self, url):
        """
        Opens a URL in the Chrome WebView and switches to the proper context.
        :param url: The URL to open in the browser.
        """
        try:
            # Wait for WebView context and switch to it
            WebDriverWait(self.driver, 10).until(
                lambda d: "WEBVIEW_chrome" in d.contexts
            )
            contexts = self.driver.contexts
            self.driver.info(f"Available contexts: {contexts}")
            self.driver.switch_to.context("WEBVIEW_chrome")
            self.driver.info("Switched to Chrome WebView context.")

            # Open the URL
            self.driver.info(f"Opening URL: {url}")
            self.driver.get(url)

        except Exception as e:
            self.driver.info(f"Error occurred while opening URL {url}: {e}")
            raise

    def wait_for_text(self, text, timeout=30):
        """
        Waits until the specified text appears on the page.
        :param text: The text to wait for on the page (exact match).
        :param timeout: Timeout in seconds for waiting. Default is 30 seconds.
        """
        try:
            self.driver.info(f"Waiting for text '{text}' to appear on the page...")
            WebDriverWait(self.driver, timeout).until(
                EC.visibility_of_element_located((By.XPATH, f"//span[text()='{text}']"))
            )
            self.driver.info(f"Text '{text}' found on the page.")

        except Exception as e:
            self.driver.info(f"An error occurred while waiting for text '{text}': {e}")
            raise

    def switch_to_native(self):
        """
        Switches the driver context back to the native app.
        """
        try:
            self.driver.info("Switching back to native app context...")

            # Wait until the native app context is available
            WebDriverWait(self.driver, 10).until(
                lambda d: "NATIVE_APP" in d.contexts
            )
            self.driver.switch_to.context("NATIVE_APP")
            self.driver.info("Switched back to native app context successfully.")

        except Exception as e:
            self.driver.info(f"An error occurred while switching back to the native app context: {e}")
            raise



