import random
import string
import time

from itertools import cycle
from timeit import timeit

from appium.webdriver.common.mobileby import MobileBy
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.support import expected_conditions
from selenium.webdriver.support.wait import WebDriverWait

from tests import marks
from tests.base_test_case import MessageReliabilityTestCase
from views.base_element import BaseButton
from views.sign_in_view import SignInView


def wrapper(func, *args, **kwargs):
    def wrapped():
        return func(*args, **kwargs)

    return wrapped


@marks.message_reliability
class TestMessageReliability(MessageReliabilityTestCase):

    def test_message_reliability_1_1_chat(self, messages_number, message_wait_time):
        user_a_sent_messages = 0
        user_a_received_messages = 0
        user_b_sent_messages = 0
        user_b_received_messages = 0
        user_a_message_time = dict()
        user_b_message_time = dict()
        try:
            self.create_drivers(2, max_duration=10800, custom_implicitly_wait=2)
            device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
            device_1_home, device_2_home = device_1.create_user(username='user_a'), device_2.create_user(
                username='user_b')
            device_2_public_key = device_2_home.get_public_key()
            device_2_home.home_button.click()
            device_1_chat = device_1_home.add_contact(device_2_public_key)
            device_1_chat.chat_message_input.send_keys('hello')
            device_1_chat.send_message_button.click()
            device_2_home.element_by_text('hello').click()
            device_2_chat = device_2_home.get_chat_view()
            device_2_chat.add_to_contacts.click()

            start_time = time.time()
            for i in range(int(messages_number / 2)):
                message_1 = ''.join(random.sample(string.ascii_lowercase, k=10))
                device_1_chat.chat_message_input.send_keys(message_1)
                device_1_chat.send_message_button.click()
                user_a_sent_messages += 1
                try:
                    user_b_receive_time = timeit(wrapper(device_2_chat.wait_for_element_starts_with_text,
                                                         message_1, message_wait_time),
                                                 number=1)
                    duration_time = round(time.time() - start_time, ndigits=2)
                    user_b_message_time[duration_time] = user_b_receive_time
                    user_b_received_messages += 1
                except TimeoutException:
                    device_2_chat.driver.info("Message with text '%s' was not received by user_b" % message_1)
                message_2 = ''.join(random.sample(string.ascii_lowercase, k=10))
                device_2_chat.chat_message_input.send_keys(message_2)
                device_2_chat.send_message_button.click()
                user_b_sent_messages += 1
                try:
                    user_a_receive_time = timeit(wrapper(device_1_chat.wait_for_element_starts_with_text,
                                                         message_2, message_wait_time),
                                                 number=1)
                    duration_time = round(time.time() - start_time, ndigits=2)
                    user_a_message_time[duration_time] = user_a_receive_time
                    user_a_received_messages += 1
                except TimeoutException:
                    device_1_chat.driver.info("Message with text '%s' was not received by user_a" % message_2)
        finally:
            self.one_to_one_chat_data['user_a'] = {'sent_messages': user_a_sent_messages,
                                                   'message_time': user_a_message_time}
            self.one_to_one_chat_data['user_b'] = {'sent_messages': user_b_sent_messages,
                                                   'message_time': user_b_message_time}

    def test_message_reliability_1_1_chat_with_predefined_user(self, messages_number, user_public_key):
        self.create_drivers(1, max_duration=10800, custom_implicitly_wait=2)
        sign_in_view = SignInView(self.drivers[0])
        home_view = sign_in_view.create_user(username='user_a')
        home_view.add_contact(user_public_key)
        chat_view = home_view.get_chat_view()
        for i in range(messages_number):
            message_text = '%s %s' % (i + 1, ''.join(random.sample(string.ascii_lowercase, k=10)))
            chat_view.chat_message_input.send_keys(message_text)
            chat_view.send_message_button.click()

    def test_message_reliability_public_chat(self, messages_number, message_wait_time, participants_number, chat_name):
        self.public_chat_data['sent_messages'] = int()
        self.public_chat_data['message_time'] = dict()

        self.create_drivers(participants_number, max_duration=10800, custom_implicitly_wait=2)
        users = ['user_%s' % i for i in range(participants_number)]
        chat_views = list()
        chat_name = chat_name if chat_name else ''.join(random.choice(string.ascii_lowercase) for _ in range(7))
        for i in range(participants_number):
            device = SignInView(self.drivers[i])
            home_view = device.create_user(username=users[i])
            home_view.join_public_chat(chat_name)
            chat_views.append(home_view.get_chat_view())

        start_time = time.time()
        repeat = cycle(range(participants_number))
        next_user = next(repeat)
        while self.public_chat_data['sent_messages'] <= messages_number:
            this_user, next_user = next_user, next(repeat)
            message_text = '%s %s' % (self.public_chat_data['sent_messages'] + 1,
                                      ''.join(random.sample(string.ascii_lowercase, k=10)))
            chat_views[this_user].chat_message_input.send_keys(message_text)
            chat_views[this_user].send_message_button.click()
            self.public_chat_data['sent_messages'] += 1
            try:
                receive_time = timeit(wrapper(chat_views[next_user].wait_for_element_starts_with_text,
                                              message_text, message_wait_time),
                                      number=1)
                duration_time = round(time.time() - start_time, ndigits=2)
                self.public_chat_data['message_time'][duration_time] = receive_time
            except TimeoutException:
                pass
            if self.public_chat_data['sent_messages'] == messages_number:
                break

    def test_message_reliability_offline_public_chat(self, messages_number, message_wait_time, chat_name):
        self.public_chat_data['sent_messages'] = int()
        self.public_chat_data['message_time'] = dict()

        self.create_drivers(1, max_duration=10800, custom_implicitly_wait=2, offline_mode=True)
        sign_in_view = SignInView(self.drivers[0])
        home_view = sign_in_view.create_user()
        chat_name = chat_name if chat_name else home_view.get_public_chat_name()
        home_view.join_public_chat(chat_name)

        start_time = time.time()
        iterations = int(messages_number / 10 if messages_number > 10 else messages_number)
        for _ in range(iterations):
            home_view.get_back_to_home_view()
            home_view.driver.set_network_connection(1)  # airplane mode

            sent_messages_texts = self.network_api.start_chat_bot(chat_name=chat_name, messages_number=10)
            self.public_chat_data['sent_messages'] += 10

            home_view.driver.set_network_connection(2)  # turning on WiFi connection

            home_view.get_chat_with_user('#' + chat_name).click()
            chat_view = home_view.get_chat_view()
            for message in sent_messages_texts:
                try:
                    receive_time = timeit(wrapper(chat_view.wait_for_element_starts_with_text,
                                                  message, message_wait_time),
                                          number=1)
                    duration_time = round(time.time() - start_time, ndigits=2)
                    self.public_chat_data['message_time'][duration_time] = receive_time
                except TimeoutException:
                    pass

    def test_message_reliability_offline_1_1_chat(self, messages_number, message_wait_time):
        user_a_sent_messages = 0
        user_a_received_messages = 0
        user_b_sent_messages = 0
        user_b_received_messages = 0
        user_a_message_time = dict()
        user_b_message_time = dict()
        try:
            self.create_drivers(2, max_duration=10800, custom_implicitly_wait=2, offline_mode=True)
            sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
            sign_in_1.create_user(username='user_a')
            sign_in_2.create_user(username='user_b')
            device_1_home, device_2_home = sign_in_1.get_home_view(), sign_in_2.get_home_view()
            device_2_public_key = device_2_home.get_public_key()
            device_2_home.home_button.click()
            device_1_home.add_contact(device_2_public_key)
            device_1_chat = device_1_home.get_chat_view()
            device_1_chat.chat_message_input.send_keys('hello')
            device_1_chat.send_message_button.click()
            device_2_home.element_by_text('hello').click()
            device_2_chat = device_2_home.get_chat_view()
            device_2_chat.add_to_contacts.click()

            iterations = int((messages_number / 10 if messages_number > 10 else messages_number) / 2)
            start_time = time.time()
            for i in range(iterations):
                device_2_home.driver.set_network_connection(1)  # airplane mode

                messages_1 = list()
                for _ in range(10):
                    message_1 = '%s %s' % (user_a_sent_messages + 1, ''.join(random.sample(string.ascii_lowercase,
                                                                                           k=10)))
                    device_1_chat.chat_message_input.send_keys(message_1)
                    device_1_chat.send_message_button.click()
                    messages_1.append(messages_1)
                    user_a_sent_messages += 1

                device_2_home.driver.set_network_connection(2)  # turning on WiFi connection

                for message in messages_1:
                    try:
                        user_b_receive_time = timeit(wrapper(device_2_chat.wait_for_element_starts_with_text,
                                                             message, message_wait_time),
                                                     number=1)
                        duration_time = round(time.time() - start_time, ndigits=2)
                        user_b_message_time[duration_time] = user_b_receive_time
                        user_b_received_messages += 1
                    except TimeoutException:
                        device_2_home.driver.info("Message with text '%s' was not received by user_b" % message)

                messages_2 = list()
                for _ in range(10):
                    message_2 = '%s %s' % (user_b_sent_messages + 1, ''.join(random.sample(string.ascii_lowercase,
                                                                                           k=10)))
                    device_2_chat.chat_message_input.send_keys(message_2)
                    device_2_chat.send_message_button.click()
                    messages_2.append(message_2)
                    user_b_sent_messages += 1
                    for message in messages_2:
                        try:
                            user_a_receive_time = timeit(wrapper(device_1_chat.wait_for_element_starts_with_text,
                                                                 message, message_wait_time),
                                                         number=1)
                            duration_time = round(time.time() - start_time, ndigits=2)
                            user_a_message_time[duration_time] = user_a_receive_time
                            user_a_received_messages += 1
                        except TimeoutException:
                            device_1_home.driver.info("Message with text '%s' was not received by user_a" % message)
        finally:
            self.one_to_one_chat_data['user_a'] = {'sent_messages': user_a_sent_messages,
                                                   'message_time': user_a_message_time}
            self.one_to_one_chat_data['user_b'] = {'sent_messages': user_b_sent_messages,
                                                   'message_time': user_b_message_time}

    def test_message_reliability_push_notifications(self, message_wait_time):
        self.create_drivers(2, max_duration=10800, custom_implicitly_wait=2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        sign_in_1.create_user(username='user_a')
        sign_in_2.create_user(username='user_b')
        device_1_home, device_2_home = sign_in_1.get_home_view(), sign_in_2.get_home_view()
        device_2_public_key = device_2_home.get_public_key()
        device_2_home.home_button.click()

        device_2_home.driver.close_app()

        device_1_home.add_contact(device_2_public_key)
        device_1_chat = device_1_home.get_chat_view()
        device_1_chat.chat_message_input.send_keys('hello')
        device_1_chat.send_message_button.click()

        device_2_home.driver.open_notifications()
        try:
            WebDriverWait(device_2_home.driver, message_wait_time) \
                .until(
                expected_conditions.presence_of_element_located((MobileBy.XPATH, '//*[contains(@text, "Status")]')))
            element = BaseButton(device_2_home.driver)
            element.locator = element.Locator.xpath_selector("//*[contains(@text,'Status')]")
            element.click()
        except TimeoutException as exception:
            exception.msg = "Push notification is not received during '%s' seconds" % message_wait_time
            raise exception

        sign_in_2.sign_in()
        device_2_home.element_by_text('hello').click()
        device_2_chat = device_2_home.get_chat_view()
        device_2_chat.wait_for_element_starts_with_text('hello')
