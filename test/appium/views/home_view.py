import time

from selenium.common.exceptions import TimeoutException, NoSuchElementException

from tests import test_dapp_url
from views.base_element import Button, Text, BaseElement, SilentButton, CheckBox
from views.base_view import BaseView


class ChatButton(Button):
    def __init__(self, driver, **kwargs):
        super().__init__(driver, **kwargs)

    def navigate(self):
        from views.chat_view import ChatView
        return ChatView(self.driver)


class ChatElement(SilentButton):
    def __init__(self, driver, username_part, community=False):
        self.username = username_part
        self.community = community
        super().__init__(driver,
                         xpath="//*[@content-desc='chat-name-text'][starts-with(@text,'%s')]/.." % username_part)

    def navigate(self):
        if self.community:
            from views.chat_view import CommunityView
            return CommunityView(self.driver)
        else:
            from views.chat_view import ChatView
            return ChatView(self.driver)

    def click(self):
        if self.community:
            from views.chat_view import CommunityView
            desired_element = CommunityView(self.driver).community_options_button
        else:
            from views.chat_view import ChatView
            desired_element = ChatView(self.driver).chat_message_input
        self.click_until_presence_of_element(desired_element=desired_element)

        return self.navigate()

    def find_element(self):
        for i in range(2):
            try:
                return super(ChatElement, self).find_element()
            except NoSuchElementException as e:
                if i == 0:
                    self.wait_for_visibility_of_element(20)
                else:
                    e.msg = 'Device %s: Unable to find chat with name %s' % (self.driver.number, self.username)
                    raise e

    @property
    def new_messages_counter(self):
        class UnreadMessagesCountText(Text):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, xpath="(%s//android.widget.TextView)[last()]" % parent_locator)

        return UnreadMessagesCountText(self.driver, self.locator)

    @property
    def chat_preview(self):
        class PreveiewMessageText(Text):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, xpath="%s//*[@content-desc='chat-message-text']" % parent_locator)

        return PreveiewMessageText(self.driver, self.locator)

    @property
    def no_message_preview(self):
        class NoMessageText(Text):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, xpath="%s//*[@content-desc='no-messages-text']" % parent_locator)
        return NoMessageText(self.driver, self.locator)

    @property
    def new_messages_public_chat(self):
        class UnreadMessagesPublicChat(BaseElement):
            def __init__(self, driver):
                super().__init__(driver, accessibility_id="unviewed-messages-public")

        return UnreadMessagesPublicChat(self.driver)

    @property
    def new_messages_community(self):
        class UnreadMessagesCommunity(BaseElement):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator, xpath="%s/android.view.ViewGroup" % parent_locator)

        return UnreadMessagesCommunity(self.driver, self.locator)

    @property
    def chat_image(self):
        class ChatImage(BaseElement):
            def __init__(self, driver):
                super().__init__(driver, xpath="//*[@content-desc='chat-icon']")

        return ChatImage(self.driver)


class ActivityCenterElement(SilentButton):
    def __init__(self, driver, username):
        self.chat_name = username
        super().__init__(driver,
                         xpath="//*[contains(@text, '%s')]/ancestor::*[@content-desc='activity']" % username)

    def accept_contact_request(self):
        try:
            accept_element = Button(self.driver, xpath=self.locator + '/*[@content-desc="accept-contact-request"]').find_element()
        except NoSuchElementException:
            return ''
        if accept_element:
            accept_element.click()

    def decline_contact_request(self):
        try:
            decline_element = Button(self.driver, xpath=self.locator + '/*[@content-desc="decline-contact-request"]').find_element()
        except NoSuchElementException:
            return ''
        if decline_element:
            decline_element.click()


#    Old UI
#     def __init__(self, driver, chat_name):
#         self.chat_name = chat_name
#         super().__init__(driver,
#                          xpath="//*[@content-desc='chat-name-or-sender-text'][starts-with(@text,'%s')]/../.." % chat_name)

#     def navigate(self):
#         from views.chat_view import ChatView
#         return ChatView(self.driver)
#
#     def click(self):
#         from views.chat_view import ChatView
#         desired_element = ChatView(self.driver).chat_message_input
#         self.click_until_presence_of_element(desired_element=desired_element)
#
#         return self.navigate()
#
#     @property
#     def chat_image(self):
#         class ChatImage(BaseElement):
#             def __init__(self, driver, parent_locator: str):
#                 super().__init__(driver, xpath="%s//*[@content-desc='current-account-photo']" % parent_locator)
#
#         return ChatImage(self.driver, self.locator)
#
#     @property
#     def chat_message_preview(self):
#         class ChatMessagePreview(BaseElement):
#             def __init__(self, driver, parent_locator: str):
#                 super().__init__(driver, xpath="%s//*[@content-desc='chat-message-text']" % parent_locator)
#
#         return ChatMessagePreview(self.driver, self.locator).text
#
#     @property
#     def chat_name_indicator_text(self):
#         class ChatNameIndicatorText(BaseElement):
#             def __init__(self, driver, parent_locator: str):
#                 super().__init__(driver,
#                                  xpath="(%s//*[@content-desc='chat-name-container']//android.widget.TextView)[last()]" % parent_locator)
#
#         try:
#             return ChatNameIndicatorText(self.driver, self.locator).text
#         except NoSuchElementException:
#             return ''
#
#     def accept_contact_request(self):
#         try:
#             accept_element = Button(self.driver, xpath=self.locator + '/*[@content-desc="accept-cr"]').find_element()
#         except NoSuchElementException:
#             return ''
#         if accept_element:
#             accept_element.click()
#
#     def decline_contact_request(self):
#         try:
#             decline_element = Button(self.driver, xpath=self.locator + '/*[@content-desc="decline-cr"]').find_element()
#         except NoSuchElementException:
#             return ''
#         if decline_element:
#             decline_element.click()
#
#
# class PushNotificationElement(SilentButton):
#     def __init__(self, driver, pn_text):
#         self.pn_text = pn_text
#         super().__init__(driver, xpath="//*[@text='%s']" % pn_text)
#
#     @property
#     def icon(self):
#         class PnIconElement(BaseElement):
#             def __init__(self, driver, parent_locator):
#                 super().__init__(driver,
#                                  xpath="%s/../../../../*/*[@resource-id='android:id/message_icon']" % parent_locator)
#
#         return PnIconElement(self.driver, self.locator)
#
#     @property
#     def username(self):
#         class PnUsername(BaseElement):
#             def __init__(self, driver, parent_locator):
#                 super().__init__(driver,
#                                  xpath="%s/../../*[@resource-id='android:id/message_name']" % parent_locator)
#
#         return PnUsername(self.driver, self.locator).text
#
#     @property
#     def group_chat_icon(self):
#         class GroupChatIconElement(BaseElement):
#             def __init__(self, driver, parent_locator):
#                 super().__init__(driver,
#                                  xpath="%s/../../../../*[@resource-id='android:id/right_icon_container']" % parent_locator)
#
#         return GroupChatIconElement(self.driver, self.locator)



class PushNotificationElement(SilentButton):
    def __init__(self, driver, pn_text):
        self.pn_text = pn_text
        super().__init__(driver, xpath="//*[@text='%s']" % pn_text)

    @property
    def icon(self):
        class PnIconElement(BaseElement):
            def __init__(self, driver, parent_locator):
                super().__init__(driver,
                                 xpath="%s/../../../../*/*[@resource-id='android:id/message_icon']" % parent_locator)

        return PnIconElement(self.driver, self.locator)

    @property
    def username(self):
        class PnUsername(BaseElement):
            def __init__(self, driver, parent_locator):
                super().__init__(driver,
                                 xpath="%s/../../*[@resource-id='android:id/message_name']" % parent_locator)

        return PnUsername(self.driver, self.locator).text

    @property
    def group_chat_icon(self):
        class GroupChatIconElement(BaseElement):
            def __init__(self, driver, parent_locator):
                super().__init__(driver,
                                 xpath="%s/../../../../*[@resource-id='android:id/right_icon_container']" % parent_locator)

        return GroupChatIconElement(self.driver, self.locator)


class HomeView(BaseView):
    def __init__(self, driver):
        super().__init__(driver)

        self.plus_button = Button(self.driver, accessibility_id="new-chat-button")
        self.chat_name_text = Text(self.driver, accessibility_id="chat-name-text")
        self.start_new_chat_button = ChatButton(self.driver, accessibility_id="start-1-1-chat-button")
        self.new_group_chat_button = ChatButton(self.driver, accessibility_id="start-group-chat-button")
        self.join_public_chat_button = ChatButton(self.driver, accessibility_id="join-public-chat-button")
        self.universal_qr_scanner_button = Button(self.driver, accessibility_id="universal-qr-scanner")
        self.invite_friends_button = Button(self.driver, accessibility_id="invite-friends-button")
        self.stop_status_service_button = Button(self.driver, accessibility_id="STOP")
        self.my_profile_on_start_new_chat_button = Button(self.driver,
                                                          xpath="//*[@content-desc='current-account-photo']")
        self.communities_button = ChatButton(self.driver, accessibility_id="communities-button")
        self.ens_banner_close_button = Button(self.driver, accessibility_id=":ens-banner-close-button")

        # Notification centre
        self.notifications_button = Button(self.driver, accessibility_id="notifications-button")
        self.notifications_unread_badge = BaseElement(self.driver, accessibility_id="notifications-unread-badge") 
        self.open_activity_center_button = Button(self.driver, accessibility_id="open-activity-center-button")
        self.close_activity_centre = Button(self.driver, accessibility_id="close-activity-center")
        
        self.notifications_select_button = Button(self.driver, translation_id="select")
        self.notifications_reject_and_delete_button = Button(self.driver, accessibility_id="reject-and-delete"
                                                                                           "-activity-center")
        self.notifications_accept_and_add_button = Button(self.driver,
                                                          accessibility_id="accept-and-add-activity-center")
        self.notifications_select_all = Button(self.driver, xpath="(//android.widget.CheckBox["
                                                                  "@content-desc='checkbox-off'])[1]")

        # Options on long tap
        self.chats_menu_invite_friends_button = Button(self.driver, accessibility_id="chats-menu-invite-friends-button")
        self.delete_chat_button = Button(self.driver, accessibility_id="delete-chat-button")
        self.clear_history_button = Button(self.driver, accessibility_id="clear-history-button")
        self.mark_all_messages_as_read_button = Button(self.driver, accessibility_id="mark-all-read-button")

        # Connection icons
        self.mobile_connection_off_icon = Button(self.driver, accessibility_id="conn-button-mobile-sync-off")
        self.mobile_connection_on_icon = Button(self.driver, accessibility_id="conn-button-mobile-sync")
        self.connection_offline_icon = Button(self.driver, accessibility_id="conn-button-offline")

        # Sync using mobile data bottom sheet
        self.continue_syncing_button = Button(self.driver, accessibility_id="mobile-network-continue-syncing")
        self.stop_syncing_button = Button(self.driver, accessibility_id="mobile-network-stop-syncing")
        self.remember_my_choice_checkbox = CheckBox(self.driver, accessibility_id=":checkbox-on")

        # Connection status bottom sheet
        self.connected_to_n_peers_text = Text(self.driver, accessibility_id="connected-to-n-peers")
        self.connected_to_node_text = Text(self.driver, accessibility_id="connected-to-mailserver")
        self.waiting_for_wi_fi = Text(self.driver, accessibility_id="waiting-wi-fi")
        self.use_mobile_data_switch = Button(self.driver, accessibility_id="mobile-network-use-mobile")
        self.connection_settings_button = Button(self.driver, accessibility_id="settings")
        self.not_connected_to_node_text = Text(self.driver, accessibility_id="not-connected-nodes")
        self.not_connected_to_peers_text = Text(self.driver, accessibility_id="not-connected-to-peers")

        # New UI
        self.new_chat_button = Button(self.driver, accessibility_id="new-chat-button")

    def wait_for_syncing_complete(self):
        self.driver.info('Waiting for syncing to complete')
        while True:
            try:
                sync = self.element_by_text_part('Syncing').wait_for_element(10)
                self.driver.info(sync.text)
            except TimeoutException:
                break

    def get_chat(self, username, community=False, wait_time=30):
        self.driver.info("Looking for chat: '%s'" % username)
        chat_element = ChatElement(self.driver, username[:25], community=community)
        if not chat_element.is_element_displayed(10):
            if self.notifications_unread_badge.is_element_displayed(30):
                self.open_activity_center_button.click()
            chat_in_ac = ActivityCenterElement(self.driver, username[:25])
            chat_in_ac.wait_for_element(20)
            chat_in_ac.click()
        return chat_element

    def get_chat_from_home_view(self, username):
        self.driver.info("Looking for chat: '%s'" % username)
        chat_element = ChatElement(self.driver, username[:25])
        return chat_element

    def get_chat_from_activity_center_view(self, chat_name):
        self.driver.info("Looking for chat: '%s'" % chat_name)
        chat_element = ActivityCenterElement(self.driver, chat_name[:25])
        return chat_element

    def handle_contact_request(self, username: str, accept=True):
        if self.notifications_unread_badge.is_element_displayed(30):
            self.open_activity_center_button.click()
        chat_element = ActivityCenterElement(self.driver, username[:25])
        if accept:
            self.driver.info("Accepting contact request for %s" % username)
            chat_element.accept_contact_request()
        else:
            self.driver.info("Rejecting contact request for %s" % username)
            chat_element.decline_contact_request()
        self.close_activity_centre.click()
        self.chats_tab.wait_for_visibility_of_element()

    def get_username_below_start_new_chat_button(self, username_part):
        return Text(self.driver,
                    xpath="//*[@content-desc='enter-contact-code-input']/../..//*[starts-with(@text,'%s')]" % username_part)

    def add_contact(self, public_key, add_in_contacts=True, nickname=''):
        self.driver.info("## Starting 1-1 chat, add in contacts:%s" % str(add_in_contacts), device=False)
        self.plus_button.click_until_presence_of_element(self.start_new_chat_button)
        chat = self.start_new_chat_button.click()
        chat.public_key_edit_box.click()
        chat.public_key_edit_box.send_keys(public_key)
        one_to_one_chat = self.get_chat_view()
        chat.confirm()
        # chat.confirm_until_presence_of_element(one_to_one_chat.chat_message_input)
        if add_in_contacts and one_to_one_chat.add_to_contacts.is_element_displayed():
            one_to_one_chat.add_to_contacts.click()
        if nickname:
            one_to_one_chat.chat_options.click()
            one_to_one_chat.view_profile_button.click()
            one_to_one_chat.set_nickname(nickname)
        self.driver.info("## 1-1 chat is created successfully!", device=False)
        return one_to_one_chat

    def create_group_chat(self, user_names_to_add: list, group_chat_name: str = 'new_group_chat', new_ui=False):
        self.driver.info("## Creating group chat '%s'" % group_chat_name, device=False)
        if new_ui:
            self.new_chat_button.click()
        else:
            self.plus_button.click()
        chat_view = self.new_group_chat_button.click()
        if user_names_to_add:
            for user_name in user_names_to_add:
                if len(user_names_to_add) > 5:
                    chat_view.search_by_keyword(user_name[:5])
                    chat_view.get_username_checkbox(user_name).click()
                    chat_view.search_input.clear()
                else:
                    chat_view.get_username_checkbox(user_name).click()
        chat_view.next_button.click()
        chat_view.chat_name_editbox.send_keys(group_chat_name)
        chat_view.create_button.click()
        self.driver.info("## Group chat %s is created successfully!" % group_chat_name, device=False)
        return chat_view

    def create_community(self, name: str, description="some_description", set_image=False, file_name='sauce_logo.png', require_approval=True):
        self.driver.info("## Creating community '%s', set image is set to '%s'" % (name, str(set_image)), device=False)
        self.plus_button.click()
        chat_view = self.communities_button.click()
        chat_view.create_community_button.click()
        chat_view.community_name_edit_box.set_value(name)
        chat_view.community_description_edit_box.set_value(description)
        if set_image:
            from views.profile_view import ProfileView
            set_picture_view = ProfileView(self.driver)
            set_picture_view.element_by_translation_id("community-thumbnail-upload").scroll_and_click()
            set_picture_view.element_by_translation_id("community-image-pick").scroll_and_click()
            set_picture_view.select_photo_from_gallery(file_name)
            set_picture_view.crop_photo_button.click()
        if require_approval:
            self.element_by_translation_id("membership-title").scroll_and_click()
            self.element_by_translation_id("membership-approval").click()
            self.done_button.click()

        chat_view.confirm_create_in_community_button.wait_and_click()
        self.driver.info("## Community is created successfully!", device=False)
        return chat_view.get_community_by_name(name)

    def join_public_chat(self, chat_name: str):
        self.driver.info("## Creating public chat %s" % chat_name, device=False)
        self.plus_button.click_until_presence_of_element(self.join_public_chat_button, attempts=5)
        self.join_public_chat_button.wait_for_visibility_of_element(5)
        chat_view = self.join_public_chat_button.click()
        chat_view.chat_name_editbox.wait_for_visibility_of_element(20)
        chat_view.chat_name_editbox.click()
        chat_view.chat_name_editbox.send_keys(chat_name)
        time.sleep(2)
        self.confirm_until_presence_of_element(chat_view.chat_message_input)
        self.driver.info("## Public chat '%s' is created successfully!" % chat_name, device=False)
        return self.get_chat_view()

    def open_status_test_dapp(self, url=test_dapp_url, allow_all=True):
        self.driver.info("Opening dapp '%s', allow all:'%s'" % (test_dapp_url, str(allow_all)))
        dapp_view = self.dapp_tab_button.click()
        dapp_view.open_url(url)
        status_test_dapp = dapp_view.get_status_test_dapp_view()
        if allow_all:
            if status_test_dapp.allow_button.is_element_displayed(20):
                status_test_dapp.allow_button.click_until_absense_of_element(status_test_dapp.allow_button)
        else:
            status_test_dapp.deny_button.click_until_absense_of_element(status_test_dapp.deny_button)
        return status_test_dapp

    def delete_chat_long_press(self, username):
        self.driver.info("Deleting chat '%s' by long press" % username)
        self.get_chat(username).long_press_element()
        self.delete_chat_button.click()
        self.delete_button.click()

    def leave_chat_long_press(self, username):
        self.driver.info("Leaving chat '%s' by long press" % username)
        self.get_chat(username).long_press_element()
        from views.chat_view import ChatView
        ChatView(self.driver).leave_chat_button.click()
        ChatView(self.driver).leave_button.click()

    def clear_chat_long_press(self, username):
        self.driver.info("Clearing history in chat '%s' by long press" % username)
        self.get_chat(username).long_press_element()
        self.clear_history_button.click()
        from views.chat_view import ChatView
        ChatView(self.driver).clear_button.click()

    def get_pn(self, pn_text: str):
        self.driver.info("Getting PN by '%s'" % pn_text)
        expected_element = PushNotificationElement(self.driver, pn_text)
        return expected_element if expected_element.is_element_displayed(60) else False
