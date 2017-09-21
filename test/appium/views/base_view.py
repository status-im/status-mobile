from views.base_element import BaseElement, BaseButton, BaseEditBox, BaseText
import logging
import time
import pytest
import requests


class BackButton(BaseButton):

    def __init__(self, driver):
        super(BackButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@content-desc='toolbar-back-button']")


class ContactsButton(BaseButton):

    def __init__(self, driver):
        super(ContactsButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Contacts']")


class BaseViewObject(object):

    def __init__(self, driver):
        self.driver = driver
        self.back_button = BackButton(self.driver)
        self.contacts_button = ContactsButton(self.driver)

    def confirm(self):
        self.driver.keyevent(66)

    def send_int_as_keyevent(self, integer):
        keys = {0: 7, 1: 8, 2: 9, 3: 10, 4: 11,
                5: 12, 6: 13, 7: 14, 8: 15, 9: 16}
        self.driver.keyevent(keys[integer])

    def send_dot_as_keyevent(self):
        self.driver.keyevent(55)

    def find_full_text(self, text, wait_time=60):
        element = BaseElement(self.driver)
        element.locator = element.Locator.xpath_selector('//*[@text="' + text + '"]')
        return element.wait_for_element(wait_time)

    def find_text_part(self, text, wait_time=60):
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
        return requests.request('GET', url).json()["result"]

    def get_donate(self, address, wait_time=300):
        response = requests.request('GET', 'http://46.101.129.137:3001/donate/0x%s' % address).json()
        counter = 0
        while True:
            if counter == wait_time:
                logging.info("Donation was not received during %s seconds!" % wait_time)
                break
            elif self.get_balance(address) != '1000000000000000000':
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
                logging.info('Balance is not changed during %s seconds, funds were not received!')
                break
            elif initial_balance == self.get_balance(recipient_address):
                counter += 10
                time.sleep(10)
                logging.info('Waiting %s seconds for funds' % counter)
            else:
                logging.info('Transaction was received and verified on ropsten.etherscan.io')
                break
