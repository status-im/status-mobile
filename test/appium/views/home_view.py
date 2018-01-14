import logging

from selenium.common.exceptions import TimeoutException

from views.base_element import BaseButton
from views.base_view import BaseView


class PlusButton(BaseButton):
    def __init__(self, driver):
        super(PlusButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='+']")

    def navigate(self):
        from views.start_new_chat_view import StarNewChatView
        return StarNewChatView(self.driver)


class ConsoleButton(BaseButton):
    def __init__(self, driver):
        super(ConsoleButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Console']")


class ChatElement(BaseButton):
    def __init__(self, driver, username):
        super(ChatElement, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='%s']" % username)

    def navigate(self):
        from views.chat_view import ChatView
        return ChatView(self.driver)


class HomeView(BaseView):
    def __init__(self, driver):
        super(HomeView, self).__init__(driver)

        self.plus_button = PlusButton(self.driver)
        self.console_button = ConsoleButton(self.driver)

    def wait_for_syncing_complete(self):
        logging.info('Waiting for syncing complete:')
        while True:
            try:
                sync = self.find_text_part('Syncing', 10)
                logging.info(sync.text)
            except TimeoutException:
                break

    def get_chat_with_user(self, username):
        return ChatElement(self.driver, username)
