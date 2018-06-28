from views.base_element import BaseButton
from views.chat_view import ChatView


class FaucetCommand(BaseButton):
    def __init__(self, driver):
        super(FaucetCommand, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[contains(@text,'Get some ETH')]/preceding-sibling::*[@text='/faucet']")


class FaucetSendCommand(BaseButton):
    def __init__(self, driver):
        super(FaucetSendCommand, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Status Testnet Faucet']")


class ConsoleView(ChatView):
    def __init__(self, driver):
        super(ConsoleView, self).__init__(driver)
        self.faucet_command = FaucetCommand(self.driver)
        self.faucet_send_command = FaucetSendCommand(self.driver)

    def send_faucet_request(self):
        self.commands_button.click()
        self.faucet_command.click()
        self.faucet_send_command.click()
        self.send_message_button.click()
