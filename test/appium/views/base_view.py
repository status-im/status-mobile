from views.base_element import BaseElement, BaseButton, BaseEditBox, BaseText
import logging
import time
import pytest
import requests


class BackButton(BaseButton):
    def __init__(self, driver):
        super(BackButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='toolbar-back-button']")

    def click(self):
        self.wait_for_element(30)
        self.find_element().click()
        logging.info('Tap on %s' % self.name)
        return self.navigate()


class DenyButton(BaseButton):
    def __init__(self, driver):
        super(DenyButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Deny']")


class ContactsButton(BaseButton):
    def __init__(self, driver):
        super(ContactsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Contacts']")

    def navigate(self):
        from views.contacts import ContactsViewObject
        return ContactsViewObject(self.driver)


class WalletButton(BaseButton):
    def __init__(self, driver):
        super(WalletButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Wallet']")

    def navigate(self):
        from views.wallet import WalletViewObject
        return WalletViewObject(self.driver)


class DiscoverButton(BaseButton):
    def __init__(self, driver):
        super(DiscoverButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Discover']")

    def navigate(self):
        from views.discover import DiscoverView
        return DiscoverView(self.driver)


class YesButton(BaseButton):
    def __init__(self, driver):
        super(YesButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Yes']")


class NoButton(BaseButton):
    def __init__(self, driver):
        super(NoButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='No']")


class OkButtonAPK(BaseButton):
    def __init__(self, driver):
        super(OkButtonAPK, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='OK']")


class ContinueButtonAPK(BaseButton):
    def __init__(self, driver):
        super(ContinueButtonAPK, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Continue']")


def get_ethereum_price_in_usd() -> float:
    url = 'https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=USD'
    return float(requests.request('GET', url).json()['USD'])


def get_transactions(address: str) -> dict:
    url = 'http://ropsten.etherscan.io/api?module=account&action=txlist&address=0x%s&sort=desc' % address
    return requests.request('GET', url=url).json()['result']


def is_transaction_successful(transaction_hash: str) -> int:
    url = "https://ropsten.etherscan.io/api?module=transaction&action=getstatus&txhash=%s" % transaction_hash
    return not int(requests.request('GET', url=url).json()['result']['isError'])


def verify_transaction_in_ropsten(address: str, transaction_hash: str):
    transactions = get_transactions(address=address)
    for transaction in transactions:
        if transaction['hash'] == transaction_hash:
            logging.info('Transaction is found in Ropsten network')
            if not is_transaction_successful(transaction_hash=transaction_hash):
                pytest.fail('Transaction is not successful')
            return
    pytest.fail('Transaction was not found via Ropsten API')


class SaveButton(BaseButton):
    def __init__(self, driver):
        super(SaveButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='SAVE']")


class ChatRequestInput(BaseEditBox):

    def __init__(self, driver):
        super(ChatRequestInput, self).__init__(driver)
        self.locator = \
            self.Locator.xpath_selector("//android.widget.EditText[@content-desc!='chat-message-input']")


class AppsButton(BaseButton):
    def __init__(self, driver):
        super(AppsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("Apps")


class StatusAppIcon(BaseButton):
    def __init__(self, driver):
        super(StatusAppIcon, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//*[@text='Status']")


class BaseViewObject(object):
    def __init__(self, driver):
        self.driver = driver

        self.yes_button = YesButton(self.driver)
        self.no_button = NoButton(self.driver)
        self.back_button = BackButton(self.driver)
        self.deny_button = DenyButton(self.driver)
        self.continue_button_apk = ContinueButtonAPK(self.driver)
        self.ok_button_apk = OkButtonAPK(self.driver)
        self.apps_button = AppsButton(self.driver)
        self.status_app_icon = StatusAppIcon(self.driver)

        self.contacts_button = ContactsButton(self.driver)
        self.wallet_button = WalletButton(self.driver)
        self.discover_button = DiscoverButton(self.driver)

        self.save_button = SaveButton(self.driver)

        self.chat_request_input = ChatRequestInput(self.driver)

    @property
    def logcat(self):
        return self.driver.get_log("logcat")

    def confirm(self):
        logging.info("Tap 'Confirm' on native keyboard")
        self.driver.keyevent(66)

    def send_as_keyevent(self, string):
        keys = {'0': 7, '1': 8, '2': 9, '3': 10, '4': 11, '5': 12, '6': 13, '7': 14, '8': 15, '9': 16,
                ',': 55, '-': 69}
        for i in string:
            logging.info("Tap '%s' on native keyboard" % i)
            time.sleep(1)
            self.driver.keyevent(keys[i])

    def find_full_text(self, text, wait_time=60):
        logging.info("Looking for full text: '%s'" % text)
        element = BaseElement(self.driver)
        element.locator = element.Locator.xpath_selector('//*[@text="' + text + '"]')
        return element.wait_for_element(wait_time)

    def find_text_part(self, text, wait_time=60):
        logging.info("Looking for a text part: '%s'" % text)
        element = BaseElement(self.driver)
        element.locator = element.Locator.xpath_selector('//*[contains(@text, "' + text + '")]')
        return element.wait_for_element(wait_time)

    def element_by_text(self, text, element_type='base'):

        element_types = {
            'base': BaseElement,
            'button': BaseButton,
            'edit_box': BaseEditBox,
            'text': BaseText
        }

        element = element_types[element_type](self.driver)
        element.locator = element.Locator.xpath_selector('//*[@text="' + text + '"]')
        return element

    def get_chats(self):
        from views.chats import ChatsViewObject
        return ChatsViewObject(self.driver)

    def get_login(self):
        from views.login import LoginView
        return LoginView(self.driver)

    def get_balance(self, address):
        url = 'http://ropsten.etherscan.io/api?module=account&action=balance&address=0x%s&tag=latest' % address
        for i in range(5):
            try:
                return int(requests.request('GET', url).json()["result"])
            except ValueError:
                pass

    def get_donate(self, address, wait_time=300):
        initial_balance = self.get_balance(address)
        counter = 0
        if initial_balance < 1000000000000000000:
            response = requests.request('GET', 'http://46.101.129.137:3001/donate/0x%s' % address).json()
            while True:
                if counter == wait_time:
                    pytest.fail("Donation was not received during %s seconds!" % wait_time)
                elif self.get_balance(address) == initial_balance:
                    counter += 10
                    time.sleep(10)
                    logging.info('Waiting %s seconds for donation' % counter)
                else:
                    logging.info('Got %s for %s' % (response["amount"], address))
                    break

    def verify_balance_is_updated(self, initial_balance, recipient_address, wait_time=120):
        counter = 0
        while True:
            if counter == wait_time:
                pytest.fail('Balance is not changed during %s seconds, funds were not received!' % wait_time)
            elif initial_balance == self.get_balance(recipient_address):
                counter += 10
                time.sleep(10)
                logging.info('Waiting %s seconds for funds' % counter)
            else:
                logging.info('Transaction was received and verified on ropsten.etherscan.io')
                break
