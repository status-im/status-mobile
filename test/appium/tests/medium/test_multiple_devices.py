import random
import emoji
import time
from tests import background_service_message
from views.chat_view import CommunityView


from tests import bootnode_address, mailserver_address, mailserver_ams,  mailserver_hk, used_fleet, common_password
from tests.users import transaction_senders, basic_user, ens_user
from tests import marks
from tests.base_test_case import MultipleDeviceTestCase
from views.sign_in_view import SignInView
from views.chat_view import ChatView


@marks.medium
class TestChatManagementMultipleDevice(MultipleDeviceTestCase):
    @marks.testrail_id(5763)
    # TODO: check main e2e about block; if the difference is only in place where user is blocked, split it and remove
    # duplicates
    def test_contact_block_user_from_one_to_one_header_check_push_notification_service(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        message_before_block_1 = "Before block from %s" % device_1.driver.number
        message_before_block_2 = "Before block from %s" % device_2.driver.number
        message_after_block_2 = "After block from %s" % device_2.driver.number
        home_1, home_2 = device_1.create_user(enable_notifications=True), device_2.create_user()
        profile_1 = home_1.profile_button.click()
        device_2_public_key = home_2.get_public_key_and_username()
        home_2.home_button.click()
        default_username_1 = profile_1.default_username_text.text
        profile_1.home_button.click()

        device_1.just_fyi('both devices joining the same public chat and send messages')
        chat_name = device_1.get_random_chat_name()
        for home in home_1, home_2:
            home.join_public_chat(chat_name)
        chat_public_1, chat_public_2 = home_1.get_chat_view(), home_2.get_chat_view()
        for chat in chat_public_1, chat_public_2:
            chat.chat_message_input.send_keys("Before block from %s" % chat.driver.number)
            chat.send_message_button.click()

        chat_public_1.get_back_to_home_view()
        chat_public_2.get_back_to_home_view()

        device_1.just_fyi('both devices joining 1-1 chat and exchanging several messages')
        chat_1 = home_1.add_contact(device_2_public_key)
        for _ in range(2):
            chat_1.chat_message_input.send_keys(message_before_block_1)
            chat_1.send_message_button.click()

        chat_2 = home_2.get_chat(default_username_1).click()
        for _ in range(2):
            chat_2.chat_message_input.send_keys(message_before_block_2)
            chat_2.send_message_button.click()

        device_1.just_fyi('block user')
        chat_1.chat_options.click()
        chat_1.view_profile_button.click()
        chat_1.block_contact()
        chat_1.get_back_to_home_view()
        chat_1.home_button.click()

        device_1.just_fyi('no 1-1, messages from blocked user are hidden in public chat')
        from views.home_view import ChatElement
        blocked_chat_user = ChatElement(self.drivers[0], basic_user['username'])

        if blocked_chat_user.is_element_displayed():
            home_1.driver.fail("Chat with blocked user '%s' is not deleted" % device_2.driver.number)
        public_chat_after_block_1 = home_1.join_public_chat(chat_name)
        if public_chat_after_block_1.chat_element_by_text(message_before_block_2).is_element_displayed():
            self.errors.append(
                "Messages from blocked user '%s' are not cleared in public chat '%s'" % (device_2.driver.number,
                                                                                         chat_name))
        device_1.click_system_home_button()

        device_2.just_fyi('send messages to 1-1 and public chat')
        for _ in range(2):
            chat_2.chat_message_input.send_keys(message_after_block_2)
            chat_2.send_message_button.click()
        chat_2.get_back_to_home_view()
        home_2.join_public_chat(chat_name)
        chat_public_2 = home_2.get_chat_view()
        [chat_public_2.send_message(message_after_block_2) for _ in range(2)]

        device_1.just_fyi("check that new messages and push notifications don't arrive from blocked user")
        device_1.open_notification_bar()
        if device_1.element_by_text_part(message_after_block_2).is_element_displayed():
            self.errors.append("Push notification is received from blocked user")
        device_1.element_by_text_part(background_service_message).click()

        if public_chat_after_block_1.chat_element_by_text(message_after_block_2).is_element_displayed():
            self.errors.append("Message from blocked user '%s' is received" % device_2.driver.number)
        if home_1.notifications_unread_badge.is_element_displayed():
            device_1.driver.fail("Unread badge is shown after receiving new message from blocked user")
        if blocked_chat_user.is_element_displayed():
            device_2.driver.fail("Chat with blocked user is reappeared after receiving new messages in home view")
        device_1.open_notification_bar()
        home_1.stop_status_service_button.click()

        device_2.just_fyi("send messages when device 1 is offline")
        for _ in range(2):
            chat_public_2.chat_message_input.send_keys(message_after_block_2)
            chat_public_2.send_message_button.click()
        chat_public_2.get_back_to_home_view()
        home_2.get_chat(default_username_1).click()
        for _ in range(2):
            chat_2.chat_message_input.send_keys(message_after_block_2)
            chat_2.send_message_button.click()

        device_1.just_fyi("reopen app and check that messages from blocked user are not fetched")
        device_1.click_system_home_button()
        self.drivers[0].launch_app()
        device_1.sign_in()
        public_chat_after_block_1.home_button.double_click()
        if home_1.notifications_unread_badge.is_element_displayed():
            device_1.driver.fail("Unread badge is shown after after fetching new messages from offline")
        if blocked_chat_user.is_element_displayed():
            self.errors.append("Chat with blocked user is reappeared after fetching new messages from offline")
        home_1.join_public_chat(chat_name)
        home_1.get_chat_view()
        if chat_public_1.chat_element_by_text(message_after_block_2).is_element_displayed():
            self.errors.append("Message from blocked user '%s' is received after fetching new messages from offline"
                               % device_2.driver.number)

        device_1.just_fyi("check that PNs are still enabled in profile after closing 'background notification centre' "
                          "message and relogin")
        device_1.open_notification_bar()
        if not device_1.element_by_text_part(background_service_message).is_element_displayed():
            self.errors.append("Background notification service is not started after relogin")

        self.errors.verify_no_errors()

    @marks.testrail_id(6326)
    # TODO: check if no duplicate checks 702155
    def test_chat_mention_users_if_not_in_contacts(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        sender = ens_user
        home_1, home_2 = device_1.create_user(), device_2.recover_access(passphrase=sender['passphrase'])
        profile_2 = home_2.profile_button.click()
        profile_2.connect_existing_ens(sender['ens'])
        profile_2.home_button.double_click()

        device_1.just_fyi('Both devices joining the same public chat and send messages')
        chat_name = device_1.get_random_chat_name()
        [chat_1, chat_2] = [home.join_public_chat(chat_name) for home in (home_1, home_2)]
        message = 'From ' + sender['ens'] + ' message'
        chat_2.send_message(message)
        username_value = '@' + sender['ens']

        self.drivers[1].close_app()
        self.drivers[1].launch_app()
        device_2.back_button.click()
        device_2.your_keys_more_icon.click()
        device_2.generate_new_key_button.click()
        device_2.create_user(second_user=True)
        home_2.join_public_chat(chat_name)
        newusermessage = 'Newusermessage2'
        chat_2.send_message(newusermessage)
        random_username = chat_1.chat_element_by_text(newusermessage).username.text
        chat_1.wait_ens_name_resolved_in_chat(message=message, username_value=username_value)

        device_1.just_fyi('Set nickname for ENS user')
        chat_1.view_profile_long_press(message)
        nickname = 'nicknamefortestuser'
        chat_1.set_nickname(nickname)
        ens_nickname_value = nickname + " @" + sender['ens']
        chat_1.wait_ens_name_resolved_in_chat(message=message, username_value=ens_nickname_value)

        device_1.just_fyi('Check there is ENS+Nickname user in separate 1-1 chat')
        chat_1.get_back_to_home_view()
        home_1.add_contact(public_key=basic_user['public_key'])
        chat_1.chat_message_input.send_keys('@')
        if (chat_1.search_user_in_mention_suggestion_list(ens_nickname_value).is_element_displayed() or
                chat_1.search_user_in_mention_suggestion_list(
                    sender['username']).is_element_displayed()):
            self.errors.append('ENS-owner user who is not in 1-1 chat is  available in mention suggestion list')

        device_1.just_fyi('Check there is no random user in different public chat')
        chat_1.get_back_to_home_view()
        chat_1 = home_1.join_public_chat(chat_name + "2")
        chat_1.chat_message_input.send_keys('@')
        if chat_1.search_user_in_mention_suggestion_list(random_username).is_element_displayed():
            self.errors.append('Random user from public chat is in mention suggestion list another public chat')

        device_1.just_fyi('Check there is ENS+Nickname user in Group chat and no random user')
        chat_1.get_back_to_home_view()
        home_1.add_contact(sender['public_key'])
        chat_1.get_back_to_home_view()
        home_1.create_group_chat(user_names_to_add=[nickname])
        chat_1.chat_message_input.send_keys('@')
        if chat_1.search_user_in_mention_suggestion_list(random_username).is_element_displayed():
            self.errors.append('Random user from public chat is in mention suggestion list of Group chat')
        if not (chat_1.search_user_in_mention_suggestion_list(ens_nickname_value).is_element_displayed() or
                chat_1.search_user_in_mention_suggestion_list(sender['username']).is_element_displayed()):
            self.errors.append('ENS-owner user is not available in mention suggestion list of Group chat')

        device_1.just_fyi('Check there is no blocked user in mentions Group/Public chat ')
        home_1.home_button.click()
        public_1 = home_1.join_public_chat(chat_name)
        public_1.chat_element_by_text(message).member_photo.click()
        public_1.block_contact()
        public_1.chat_message_input.send_keys('@')
        if (chat_1.search_user_in_mention_suggestion_list(ens_nickname_value).is_element_displayed() or
                chat_1.search_user_in_mention_suggestion_list(sender['username']).is_element_displayed()):
            self.errors.append('Blocked user is available in mention suggestion list')

        self.errors.verify_no_errors()

    @marks.testrail_id(5362)
    # TODO: can be moved to TestOneToOneChatMultipleSharedDevices - should be quick e2e
    def test_1_1_chat_unread_counter_preview_highlited(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        profile_2 = home_2.profile_button.click()
        default_username_2 = profile_2.default_username_text.text
        home_2 = profile_2.home_button.click()
        public_key_1 = home_1.get_public_key_and_username()
        home_1.home_button.click()
        chat_2 = home_2.add_contact(public_key_1)

        message, message_2, message_3 = 'test message', 'test message2', 'test'
        chat_2.send_message(message)
        home_1.get_chat(default_username_2).click()
        home_1.home_button.click()
        home_1.dapp_tab_button.click()
        chat_2.send_message(message_2)

        home_1.home_button.counter.wait_for_element(30)
        if home_1.home_button.counter.text != '1':
            self.errors.append('New messages counter is not shown on Home button')
        device_1.home_button.click()
        if home_1.get_chat(default_username_2).new_messages_counter.text != '1':
            self.errors.append('New messages counter is not shown on chat element')
        chat_1 = home_1.get_chat(default_username_2).click()
        chat_1.add_to_contacts.click()

        home_1.home_button.double_click()

        if home_1.home_button.counter.is_element_displayed():
            self.errors.append('New messages counter is shown on Home button for already seen message')
        if home_1.get_chat(default_username_2).new_messages_counter.text == '1':
            self.errors.append('New messages counter is shown on chat element for already seen message')
        home_1.delete_chat_long_press(default_username_2)

        home_1.just_fyi("Checking preview of message and chat highlighting")
        chat_2.send_message(message_3)
        chat_1_element = home_1.get_chat(default_username_2)
        if chat_1_element.chat_preview.is_element_differs_from_template('highligted_preview.png', 0):
            self.errors.append("Preview message is not hightligted or text is not shown! ")
        home_1.get_chat(default_username_2).click()
        home_1.home_button.double_click()
        if not home_1.get_chat(default_username_2).chat_preview.is_element_differs_from_template('highligted_preview.png', 0):
            self.errors.append("Preview message is still highlighted after opening ")
        self.errors.verify_no_errors()

    @marks.testrail_id(6294)
    # TODO: may be merged with 6295 to group and add more tx tests
    def test_keycard_1_1_chat_command_request_and_send_tx_stt_in_1_1_chat_offline_opened_from_push(self):
        sender = transaction_senders['ETH_STT_1']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        device_1.just_fyi('Grab user data for transactions and public chat, set up wallets')
        home_1 = device_1.create_user(keycard=True)
        device_2.put_app_to_background_and_back()
        recipient_public_key, recipient_username = home_1.get_public_key_and_username(return_username=True)
        amount = device_1.get_unique_amount()
        asset_name = 'STT'
        wallet_1 = home_1.wallet_button.click()
        wallet_1.select_asset(asset_name)
        wallet_1.home_button.click()

        home_2 = device_2.recover_access(passphrase=sender['passphrase'], keycard=True, enable_notifications=True)
        wallet_2 = home_2.wallet_button.click()
        initial_amount_stt = wallet_2.get_asset_amount_by_name('STT')
        wallet_2.home_button.click()

        device_2.just_fyi('Add recipient to contact and send 1 message')
        chat_2 = home_2.add_contact(recipient_public_key)
        chat_2.send_message("Hey there!")

        profile_2 = wallet_2.profile_button.click()
        profile_2.airplane_mode_button.click()
        device_2.home_button.double_click()
        chat_element = home_1.get_chat(sender['username'])
        chat_element.wait_for_visibility_of_element(30)
        chat_1 = chat_element.click()

        home_1.just_fyi('Request %s STT in 1-1 chat and check it is visible for sender and receiver' % amount)
        chat_1.commands_button.click()
        request_transaction = chat_1.request_command.click()
        request_transaction.amount_edit_box.set_value(amount)
        request_transaction.confirm()
        asset_button = request_transaction.asset_by_name(asset_name)
        request_transaction.select_asset_button.click_until_presence_of_element(asset_button)
        asset_button.click()
        request_transaction.request_transaction_button.click()
        chat_1_request_message = chat_1.get_incoming_transaction()
        if not chat_1_request_message.is_element_displayed():
            self.drivers[0].fail('No incoming transaction in 1-1 chat is shown for recipient after requesting STT')

        home_2.just_fyi('Check that transaction message is fetched from offline and sign transaction')
        profile_2.airplane_mode_button.click()
        transaction_request_pn = 'Request transaction'
        device_2.open_notification_bar()
        if not device_2.element_by_text(transaction_request_pn).is_element_displayed(60):
            self.errors.append("Push notification is not received after going back from offline")
        device_2.element_by_text(transaction_request_pn).click()
        home_2.connection_offline_icon.wait_for_invisibility_of_element(120)
        home_2.get_chat(recipient_username).click()
        chat_2_sender_message = chat_2.get_outgoing_transaction()
        chat_2_sender_message.wait_for_visibility_of_element(60)
        chat_2_sender_message.transaction_status.wait_for_element_text(chat_2_sender_message.address_received)
        send_message = chat_2_sender_message.sign_and_send.click()
        send_message.next_button.click()
        send_message.sign_transaction(keycard=True)
        chat_1.toggle_airplane_mode()

        home_2.just_fyi('Check that transaction message is updated with new status after offline')
        [chat.toggle_airplane_mode() for chat in (chat_1, chat_2)]
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount, token=True)
        for home in (home_1, home_2):
            home.toggle_airplane_mode()
            home.home_button.double_click()
        home_1.get_chat(sender['username']).click()
        home_2.get_chat(recipient_username).click()
        chat_2_sender_message.transaction_status.wait_for_element_text(chat_2_sender_message.confirmed, wait_time=120)

        home_1.just_fyi('Check that can find tx in history and balance is updated after offline')
        [home.wallet_button.click() for home in (home_1, home_2)]
        wallet_2.wait_balance_is_changed('STT', initial_amount_stt)
        wallet_1.wait_balance_is_changed('STT', scan_tokens=True)
        [wallet.find_transaction_in_history(amount=amount, asset='STT') for wallet in (wallet_1, wallet_2)]
        self.errors.verify_no_errors()

    @marks.testrail_id(6257)
    # TODO: may be removed and couple of checks from it added to TestCommandsMultipleDevicesMerged;
    #  doesn't make a lot of sense as separate e2e
    def test_1_1_chat_command_network_mismatch_for_send_tx_request_in_1_1_chat(self):
        sender = transaction_senders['ETH_1']
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        sign_in_1.recover_access(passphrase=sender['passphrase'])
        sign_in_2.create_user()
        home_1, home_2 = sign_in_1.get_home_view(), sign_in_2.get_home_view()
        wallet_1 = home_1.wallet_button.click()
        wallet_1.home_button.click()
        profile_2 = home_2.profile_button.click()
        username_2 = profile_2.default_username_text.text
        profile_2.switch_network()

        chat_2 = home_2.add_contact(sender['public_key'])
        chat_2.send_message("Hey there!")
        amount = chat_2.get_unique_amount()

        chat_2.commands_button.click()
        request_transaction = chat_2.request_command.click()
        request_transaction.amount_edit_box.set_value(amount)
        request_transaction.confirm()
        request_transaction.request_transaction_button.click()
        chat_2_request_message = chat_2.get_incoming_transaction()

        chat_2_request_message.long_press_element()
        if chat_2.reply_message_button.is_element_displayed():
            self.errors.append('Reply is available on long-tap on Incoming transaction message!')

        chat_1 = home_1.get_chat(username_2).click()
        chat_1_sender_message = chat_1.get_outgoing_transaction()
        chat_1_sender_message.long_press_element()
        if chat_1.reply_message_button.is_element_displayed():
            self.errors.append('Reply is available on long-tap on Outgoing transaction message!')
        send_message = chat_1_sender_message.sign_and_send.click()
        send_message.next_button.click()
        send_message.sign_transaction()
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount)
        chat_1_sender_message.transaction_status.wait_for_element_text(chat_1_sender_message.confirmed)
        wallet_2 = chat_2.wallet_button.click()
        wallet_2.accounts_status_account.click()
        wallet_2.swipe_down()
        wallet_2.home_button.click(desired_view="chat")
        if chat_2_request_message.transaction_status == chat_1_sender_message.confirmed:
            self.errors.append("Transaction is shown as confirmed on mainnet, but was sent on ropsten!")
        self.errors.verify_no_errors()

    @marks.testrail_id(6330)
    # TODO: can be re-done to 1-driver test (sending between different account)
    def test_wallet_can_send_tx_all_tokens_via_max_option(self):
        sender = transaction_senders['ETH_STT_2']
        receiver = transaction_senders['ETH_1']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.recover_access(sender['passphrase']), device_2.recover_access(receiver['passphrase'])
        wallet_sender = home_1.wallet_button.click()
        wallet_receiver = home_2.wallet_button.click()

        if wallet_receiver.asset_by_name('STT').is_element_present(10):
            initial_balance = wallet_receiver.get_asset_amount_by_name("STT")
        else:
            initial_balance = '0'

        device_1.just_fyi("Sending token amount to device who will use Set Max option for token")
        amount = '0.012345678912345678'
        wallet_sender.send_transaction(asset_name='STT', amount=amount, recipient=receiver['address'])
        wallet_receiver.wait_balance_is_changed(asset='STT', initial_balance=initial_balance, scan_tokens=True)
        wallet_receiver.accounts_status_account.click()

        device_1.just_fyi("Send all tokens via Set Max option")
        send_transaction = wallet_receiver.send_transaction_button.click()
        send_transaction.select_asset_button.click()
        asset_name = 'STT'
        asset_button = send_transaction.asset_by_name(asset_name)
        send_transaction.select_asset_button.click_until_presence_of_element(
            send_transaction.eth_asset_in_select_asset_bottom_sheet_button)
        asset_button.click()
        send_transaction.set_max_button.click()
        send_transaction.set_recipient_address(sender['address'])
        send_transaction.sign_transaction_button.click()
        send_transaction.sign_transaction()
        wallet_receiver.close_button.click()
        initial_balance = float(initial_balance) + float(amount)
        wallet_receiver.wait_balance_is_changed(asset='STT', initial_balance=str(initial_balance), scan_tokens=True)


