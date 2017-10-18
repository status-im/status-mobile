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


class ContactsButton(BaseButton):

    def __init__(self, driver):
        super(ContactsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Contacts']")

    def navigate(self):
        from views.contacts import ContactsViewObject
        return ContactsViewObject(self.driver)


class BaseViewObject(object):

    def __init__(self, driver):
        self.driver = driver
        self.back_button = BackButton(self.driver)
        self.contacts_button = ContactsButton(self.driver)

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

    def get_balance(self, address):
        url = 'http://ropsten.etherscan.io/api?module=account&action=balance&address=0x%s&tag=latest' % address
        return int(requests.request('GET', url).json()["result"])

    def get_donate(self, address, wait_time=300):
        initial_balance = self.get_balance(address)
        response = requests.request('GET', 'http://46.101.129.137:3001/donate/0x%s' % address).json()
        counter = 0
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
