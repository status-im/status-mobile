import re
import time
from datetime import datetime, timedelta

import dateutil.parser
import pytest
from appium.webdriver.common.appiumby import AppiumBy
from selenium.common.exceptions import NoSuchElementException, TimeoutException, StaleElementReferenceException, \
    InvalidElementStateException
from selenium.webdriver import ActionChains

from tests import emojis, common_password
from views.base_element import Button, EditBox, Text, BaseElement, SilentButton
from views.base_view import BaseView
from views.home_view import HomeView


class OpenInStatusButton(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="browsing-open-in-status")

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class ViewProfileButton(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="view-profile")

    def navigate(self):
        return ChatView(self.driver)


class ChatOptionsButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="chat-menu-button")

    def click(self):
        self.click_until_presence_of_element(HomeView(self.driver).mark_all_messages_as_read_button)

    def navigate(self):
        return ChatView(self.driver)


class ProfileSendMessageButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="icon, Send message")

    def navigate(self):
        return ChatView(self.driver)


class ChatElementByText(Text):
    def __init__(self, driver, text):
        self.message_text = text
        self.chat_item_locator = "android.view.ViewGroup[@content-desc='chat-item']"
        if text in ["image", "sticker", "audio"]:
            self.message_locator = "//android.view.ViewGroup[@content-desc='%s-message']" % text
        else:
            self.message_locator = "//*[starts-with(@text,'%s')]" % text
        super().__init__(driver, prefix=self.message_locator, xpath="/ancestor::%s" % self.chat_item_locator)

    def find_element(self):
        for _ in range(2):
            try:
                return super(ChatElementByText, self).find_element()
            except NoSuchElementException:
                self.wait_for_visibility_of_element(20)

    @property
    def image_in_reply(self):
        class ImageInReply(BaseElement):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator, xpath="//android.widget.ImageView")

        try:
            return ImageInReply(self.driver, self.locator)
        except NoSuchElementException:
            return ''

    @property
    def timestamp_command_message(self):
        class TimeStampText(Button):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, xpath="(%s//android.widget.TextView)[last()]" % parent_locator)

        return TimeStampText(self.driver, self.locator)

    @property
    def timestamp(self):
        class TimeStampText(Button):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, xpath="%s//*[@content-desc='message-timestamp']" % parent_locator)

        return TimeStampText(self.driver, self.locator).text

    @property
    def member_photo(self):
        class MemberPhoto(Button):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator, xpath="//*[@content-desc='user-avatar']")

        return MemberPhoto(self.driver, self.locator)

    @property
    def username(self):
        class Username(Text):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator,
                                 xpath="/android.view.ViewGroup/android.widget.TextView[1]")

        return Username(self.driver, self.locator)

    @property
    def message_body(self):
        return Text(
            self.driver,
            xpath="//%s//android.widget.TextView[contains(@text,'%s')]" % (self.chat_item_locator, self.message_text)
        )

    @property
    def message_body_with_mention(self):
        return Text(self.driver,
                    xpath=self.message_body.locator + "/../following-sibling::android.widget.TextView")

    def click_on_link_inside_message_body(self):
        self.message_body.wait_for_visibility_of_element(30)
        self.message_body.click_inside_element_by_coordinate(rel_x=0.1, rel_y=0.9)

    def wait_for_sent_state(self, wait_time=30):
        return BaseElement(self.driver, prefix=self.locator,
                           xpath="//*[@content-desc='message-sent']").is_element_displayed(wait_time)

    @property
    def uncollapse(self) -> bool:
        class Collapse(Button):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator,
                                 xpath="/../../..//android.widget.ImageView[@content-desc='icon']")

        return Collapse(self.driver, self.locator).is_element_displayed()

    @property
    def status(self) -> str:
        Text(self.driver, xpath=self.locator).click()
        status_element = Text(self.driver, prefix=self.locator,
                              xpath="//*[@content-desc='message-status']/android.widget.TextView")
        status = ''
        i = 1

        while i < 5:
            i += 1
            if Text(self.driver, prefix=self.locator,
                    xpath="//*[@content-desc='message-sending']").is_element_displayed(2):
                status = "Sending"
                break
            else:
                Text(self.driver, xpath=self.locator).click()
                try:
                    if status_element.is_element_displayed(2):
                        status = status_element.text
                        break
                except StaleElementReferenceException:
                    pass
                time.sleep(2)
        return status

    def wait_for_status_to_be(self, expected_status: str, timeout: int = 30):
        self.driver.info("Waiting for message to be sent for %s sec" % timeout)
        start_time = time.time()
        current_status = 'not set'
        while time.time() - start_time <= timeout:
            current_status = self.status
            if current_status == expected_status:
                return
            time.sleep(1)
        raise TimeoutException("Message status was not changed to %s, it's %s" % (expected_status, current_status))

    @property
    def sent_status_checkmark(self) -> object:
        return Text(self.driver, prefix=self.locator, xpath="//*[@content-desc='sent']")

    @property
    def replied_message_text(self):
        class RepliedMessageText(Text):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver,
                                 # prefix=parent_locator,
                                 # xpath="/preceding::android.widget.TextView[@content-desc='quoted-message']"
                                 accessibility_id='quoted-message')

        try:
            # return RepliedMessageText(self.driver, self.message_locator).text
            return self.find_element().find_element(by=AppiumBy.ACCESSIBILITY_ID, value='quoted-message').text
        except NoSuchElementException:
            return ''

    @property
    def replied_to_username_text(self):
        class RepliedToUsernameText(Text):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator,
                                 xpath="/preceding-sibling::*[1]/android.widget.TextView[1]")

        try:
            return RepliedToUsernameText(self.driver, self.message_locator).text
        except NoSuchElementException:
            return ''

    def emojis_below_message(self, emoji: str = 'thumbs-up'):
        class EmojisNumber(Text):
            def __init__(self, driver, parent_locator: str):
                self.emoji = emoji
                self.emojis_id = 'emoji-reaction-%s' % str(emojis[self.emoji])
                super().__init__(driver, prefix=parent_locator,
                                 xpath="/../..//*[@content-desc='%s']/android.widget.TextView[2]" % self.emojis_id)

            @property
            def text(self):
                try:
                    text = self.find_element().text
                    self.driver.info("%s is '%s' for '%s'" % (self.name, text, self.emoji))
                    return int(text.strip())
                except NoSuchElementException:
                    return 0

        return EmojisNumber(self.driver, self.locator)

    @property
    def image_in_message(self):
        try:
            self.driver.info("Trying to access image inside message with text '%s'" % self.message_text)
            ChatElementByText(self.driver, self.message_text).wait_for_sent_state(60)
            return Button(self.driver,
                          xpath="%s//android.widget.ImageView[@content-desc='image-message']" % self.locator)
        except NoSuchElementException:
            self.driver.fail("No image is found in message!")

    class ImageContainer(Button):
        def __init__(self, driver, parent_locator):
            super().__init__(driver, xpath='%s//*[@content-desc="image-container"]' % parent_locator)

        def image_by_index(self, index: int):
            return BaseElement(self.driver, xpath="(%s//android.widget.ImageView)[%s]" % (self.locator, index))

    @property
    def image_container_in_message(self):
        try:
            self.driver.info(
                "Trying to access images (image container) inside message with text '%s'" % self.message_text)
            ChatElementByText(self.driver, self.message_text).wait_for_sent_state(60)
            return self.ImageContainer(self.driver, self.locator)
        except NoSuchElementException:
            self.driver.fail("No image container is found in message!")

    @property
    def pinned_by_label(self):
        class PinnedByLabelText(Text):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator,
                                 xpath="/../..//android.view.ViewGroup[@content-desc='pinned-by']")

        return PinnedByLabelText(self.driver, self.locator)

    @property
    def message_text_content(self):
        return Text(
            self.driver,
            xpath="//%s//*[@content-desc='message-text-content']/android.widget.TextView" % self.chat_item_locator
        )


class UsernameOptions(Button):
    def __init__(self, driver, username):
        super().__init__(driver, xpath="//*[@text='%s']/..//*[@content-desc='menu-option']" % username)

    def navigate(self):
        return ChatView(self.driver)

    def click(self):
        self.scroll_to_element()
        self.wait_for_element().click()
        return self.navigate()


class UsernameCheckbox(Button):
    def __init__(self, driver, username, state_on):
        self.username = username
        super().__init__(driver, xpath="//*[@text='%s']/..//*[@content-desc='checkbox-%s']" % (
            username, 'on' if state_on else 'off'))

    def click(self):
        try:
            self.scroll_to_element(20).click()
        except NoSuchElementException:
            self.scroll_to_element(direction='up', depth=20).click()


class CommunityView(HomeView):
    def __init__(self, driver):
        super().__init__(driver)

        # Community info page
        self.community_options_button = Button(self.driver, accessibility_id="community-options-for-community")
        self.community_membership_request_value = Text(
            self.driver, translation_id="members-label",
            suffix='/following-sibling::android.view.ViewGroup/android.widget.TextView')
        self.members_button = Button(self.driver, translation_id="members-label")
        self.leave_community_button = Button(self.driver, translation_id="leave-community")
        self.share_community_button = Button(self.driver, accessibility_id="share-community")
        self.invite_people_from_contacts_button = Button(self.driver, accessibility_id="invite-people-from-contacts")

        # Members
        self.membership_requests_button = Button(self.driver, translation_id="membership-requests")

        # Requesting access to community / joining community
        self.request_access_button = Button(self.driver, translation_id="request-access")
        self.membership_request_pending_text = Text(self.driver, translation_id="membership-request-pending")
        self.join_button = Button(self.driver, accessibility_id="show-request-to-join-screen-button")
        self.slide_to_request_to_join_button = Button(
            self.driver, xpath="(//*[@resource-id='slide-button-track']//*[@content-desc='icon'])[1]")

        # Communities initial page
        self.close_community_view_button = Button(self.driver, accessibility_id="back-button")
        self.community_title = Text(self.driver, accessibility_id="community-title")
        self.community_logo = BaseElement(
            self.driver, xpath="//*[@content-desc='community-title']/preceding-sibling::*/android.widget.ImageView")
        self.community_description_text = Text(self.driver, accessibility_id="community-description-text")
        self.community_status_joined = Text(self.driver, accessibility_id="status-tag-positive")
        self.community_status_pending = Text(self.driver, accessibility_id="status-tag-pending")

    def join_community(self, password=common_password, open_community=True):
        self.driver.info("Joining community")
        ChatView(self.driver).chat_element_by_text("https://status.app/c/").click_on_link_inside_message_body()
        self.join_button.wait_and_click(120)
        self.slide_to_request_to_join_button.swipe_right_on_element(width_percentage=16)
        self.password_input.send_keys(password)
        self.login_button.click()
        if open_community:
            self.community_status_joined.wait_for_visibility_of_element(60)

    def get_channel(self, channel_name: str):
        self.driver.info("Getting  %s channel element in community" % channel_name)
        chat_element = self.get_chat(username=channel_name, community_channel=True, wait_time=30)
        return chat_element

    def leave_community(self, community_name: str):
        self.driver.info("Leaving %s" % community_name)
        home = self.get_home_view()
        home.communities_tab.click()
        community_element = home.get_chat(community_name, community=True)
        community_element.long_press_until_element_is_shown(self.leave_community_button)
        self.leave_community_button.click()
        self.leave_community_button.click()

    def get_channel_avatar(self, channel_name='general'):
        return Button(self.driver, xpath='//*[@text="# %s"]/../*[@content-desc="channel-avatar"]' % channel_name)

    def copy_community_link(self):
        self.driver.info("Copy community link")
        self.community_options_button.click()
        self.share_community_button.click()
        text = self.sharing_text_native.text
        self.click_system_back_button(times=2)
        return text

    def handle_membership_request(self, username: str, approve=True):
        self.driver.info("Handling membership request of user '%s', approve='%s'" % (username, str(approve)))
        self.members_button.click()
        self.membership_requests_button.click()
        approve_suffix, decline_suffix = '/following-sibling::android.view.ViewGroup[1]', '/following-sibling::android.view.ViewGroup[2]'
        if approve:
            Button(self.driver, xpath="//*[starts-with(@text,'%s')]%s" % (username, approve_suffix)).click()
        else:
            Button(self.driver, xpath="//*[starts-with(@text,'%s')]%s" % (username, decline_suffix)).click()
        self.close_button.click()

    def invite_to_community(self, community_name, user_names_to_invite):
        if isinstance(user_names_to_invite, str):
            user_names_to_invite = [user_names_to_invite]
        self.driver.info("Share to  %s community" % ', '.join(map(str, user_names_to_invite)))
        self.navigate_back_to_home_view()
        home = self.get_home_view()
        home.communities_tab.click()
        community_element = home.get_chat(community_name, community=True)
        community_element.long_press_until_element_is_shown(self.share_community_button)
        self.invite_people_from_contacts_button.click()
        for user_name in user_names_to_invite:
            xpath = "//*[@content-desc='user-avatar']/following-sibling::android.widget.TextView[@text='%s']" % user_name
            Button(self.driver, xpath=xpath).click()
        self.next_button.click()


class PreviewMessage(ChatElementByText):
    def __init__(self, driver, text: str):
        super().__init__(driver, text=text)

    @staticmethod
    def return_element_or_empty(obj):
        try:
            return obj.scroll_to_element()
        except NoSuchElementException:
            return ''

    @property
    def preview_image(self):
        class PreviewImage(SilentButton):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator, xpath="//*[@content-desc='thumbnail']")

        return PreviewMessage.return_element_or_empty(PreviewImage(self.driver, self.locator))

    @property
    def preview_title(self):
        class PreviewTitle(SilentButton):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator, xpath="//*[@content-desc='title']")

        return PreviewMessage.return_element_or_empty(PreviewTitle(self.driver, self.locator))

    @property
    def preview_subtitle(self):
        class PreviewSubTitle(SilentButton):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator, xpath="//*[@content-desc='description']")

        return PreviewMessage.return_element_or_empty(PreviewSubTitle(self.driver, self.locator))

    @property
    def preview_link(self):
        class PreviewLink(SilentButton):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator, xpath="//*[@content-desc='link']")

        return PreviewMessage.return_element_or_empty(PreviewLink(self.driver, self.locator))


class CommunityLinkPreviewMessage(ChatElementByText):
    def __init__(self, driver, text: str):
        super().__init__(driver, text=text)
        self.locator += "//*[@text='%s']" % self.get_translation_by_key('community')

    @property
    def community_name(self) -> str:
        class CommunityName(SilentButton):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator, xpath="/following-sibling::android.widget.TextView[1]")

        return CommunityName(self.driver, self.locator).text

    @property
    def community_description(self) -> str:
        class CommunityDescription(SilentButton):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator, xpath="/following-sibling::android.widget.TextView[2]")

        return CommunityDescription(self.driver, self.locator).text

    @property
    def community_members_amount(self) -> int:
        class CommunityMembers(SilentButton):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator, xpath="/following-sibling::android.widget.TextView[3]")

        members_string = CommunityMembers(self.driver, self.locator).text

        return int(re.search(r'\d+', members_string).group())

    def view(self) -> object:
        class CommunityViewButton(SilentButton):
            def __init__(self, driver, parent_locator: str):
                super().__init__(driver, prefix=parent_locator,
                                 xpath="/..//*[@text='%s']" % self.get_translation_by_key("view"))

        CommunityViewButton(self.driver, self.locator).click()
        CommunityView(self.driver).request_access_button.wait_for_element(20)
        return CommunityView(self.driver)


class PinnedMessagesList(BaseElement):
    def __init__(self, driver):
        super().__init__(driver, xpath="//*[@content-desc='pinned-messages-menu']")

    def get_pinned_messages_number(self):
        self.driver.info("Getting number of pinned messages inside pinned messages list element")
        element = BaseElement(self.driver, prefix=self.locator, xpath="//*[@content-desc='message-sent']")
        return len(element.find_elements())

    def message_element_by_text(self, text):
        message_element = Button(self.driver, prefix=self.locator, xpath="//*[starts-with(@text,'%s')]" % text)
        self.driver.info("Looking for a pinned message by text: %s" % message_element.exclude_emoji(text))
        return message_element

    def get_message_pinned_by_text(self, text):
        xpath = "//*[starts-with(@text,'%s')]/../../*[@content-desc='pinned-by']/android.widget.TextView" % text
        pinned_by_element = Text(self.driver, prefix=self.locator, xpath=xpath)
        self.driver.info("Looking for a pinned by message with text: %s" % text)
        return pinned_by_element


class ChatMessageInput(EditBox):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="chat-message-input")

    def paste_text_from_clipboard(self):
        action = ActionChains(self.driver)
        element = self.find_element()
        location = element.location
        x, y = location['x'], location['y']
        action.move_by_offset(xoffset=x + 250, yoffset=y).click_and_hold().perform()  # long press
        action.release(element).perform()
        action.move_by_offset(xoffset=x + 50, yoffset=y - 50).click().perform()  # tap Paste
        action.release(element).perform()

    def click_inside(self):
        action = ActionChains(self.driver)
        element = self.find_element()
        location = element.location
        x, y = location['x'] + 250, location['y']
        action.move_to_element_with_offset(to_element=element, xoffset=x, yoffset=y).click().perform()
        action.release(element).perform()


class ChatView(BaseView):
    def __init__(self, driver):
        super().__init__(driver)

        # Start new chat
        self.public_key_edit_box = EditBox(self.driver, accessibility_id="enter-contact-code-input")
        self.view_profile_new_contact_button = Button(self.driver, accessibility_id="new-contact-button")

        ## Options
        self.chat_options = ChatOptionsButton(self.driver)
        self.delete_chat_button = Button(self.driver, translation_id="close-chat")
        self.clear_history_button = Button(self.driver, translation_id="clear-history")
        self.reply_message_button = Button(self.driver, translation_id="message-reply")
        self.share_chat_button = Button(self.driver, accessibility_id="share-chat-button")
        self.clear_button = Button(self.driver, translation_id="clear-history")
        self.view_profile_button = ViewProfileButton(self.driver)
        self.view_profile_by_avatar_button = Button(self.driver, accessibility_id="member-photo")
        self.user_options = Button(self.driver, accessibility_id="options")
        self.open_in_status_button = OpenInStatusButton(self.driver)

        # Chat input
        self.chat_message_input = ChatMessageInput(self.driver)
        self.cancel_reply_button = Button(self.driver, accessibility_id="reply-cancel-button")
        self.url_preview_composer = Button(self.driver, accessibility_id="url-preview")
        self.url_preview_composer_text = Text(self.driver,
                                              xpath='//*[@content-desc="url-preview"]//*[@content-desc="title"]')
        self.quote_username_in_message_input = EditBox(
            self.driver,
            xpath="//*[@content-desc='reply-cancel-button']/preceding::android.widget.TextView[3]")

        # General chat view
        self.history_start_icon = Button(self.driver, accessibility_id="history-chat")
        self.contact_request_button = Button(self.driver, accessibility_id="contact-request--button")

        # Communities
        self.mentions_list = BaseElement(self.driver, accessibility_id="mentions-list")

        # New UI
        self.pinned_messages_count = Button(self.driver,
                                            xpath="//*[@content-desc='pins-count']//android.widget.TextView")
        self.pinned_messages_list = PinnedMessagesList(self.driver)
        self.pin_limit_popover = BaseElement(self.driver, translation_id="pin-limit-reached")
        self.view_pinned_messages_button = Button(self.driver, accessibility_id="pinned-banner")

        # Images
        self.show_images_button = Button(self.driver, accessibility_id="open-images-button")
        self.take_photo_button = Button(self.driver, accessibility_id="camera-button")
        self.snap_button = Button(self.driver, accessibility_id="snap")
        self.images_confirm_selection_button = Button(self.driver, accessibility_id="confirm-selection")
        self.share_image_icon_button = Button(self.driver, accessibility_id="share-image")
        self.view_image_options_button = Button(self.driver, accessibility_id="image-options")
        self.save_image_icon_button = Button(self.driver, accessibility_id="save-image")

        # Group chats
        self.leave_chat_button = Button(self.driver, accessibility_id="leave-chat-button")
        self.leave_button = Button(self.driver, translation_id="leave", uppercase=True)
        self.remove_user_button = Button(self.driver, accessibility_id="remove-from-chat")
        self.make_admin_button = Button(self.driver, accessibility_id="make-admin")
        self.edit_group_chat_name_button = Button(self.driver, accessibility_id="edit-button")
        self.edit_group_chat_name_edit_box = EditBox(self.driver, accessibility_id="new-chat-name")
        self.done_button = Button(self.driver, accessibility_id="done")
        self.create_group_chat_button = Button(self.driver, accessibility_id="Create group chat")

        # Contact's profile
        self.profile_send_message_button = ProfileSendMessageButton(self.driver)
        self.profile_options_button = Button(self.driver, accessibility_id="contact-actions")
        self.profile_block_contact_button = Button(self.driver, accessibility_id="block-user")
        self.confirm_block_contact_button = Button(self.driver, accessibility_id="block-contact")
        self.profile_send_contact_request_button = Button(self.driver, accessibility_id="icon, Send contact request")
        self.contact_request_message_input = EditBox(self.driver, accessibility_id="contact-request-message")
        self.confirm_send_contact_request_button = EditBox(self.driver, accessibility_id="send-contact-request")
        self.profile_add_to_contacts_button = Button(self.driver, accessibility_id="Add to contacts-item-button")
        self.profile_remove_from_contacts = Button(self.driver, accessibility_id="Remove from contacts-item-button")
        self.profile_nickname_button = Button(self.driver, accessibility_id="profile-nickname-item")
        self.nickname_input_field = EditBox(self.driver, accessibility_id="nickname-input")
        self.remove_from_contacts = Button(self.driver, accessibility_id="Remove from contacts-item-button")

    def get_preview_message_by_text(self, text=None) -> object:
        self.driver.info('Getting preview message for link: %s' % text)
        return PreviewMessage(self.driver, text)

    def get_username_checkbox(self, username: str, state_on=False):
        self.driver.info("Getting %s checkbox" % username)
        return UsernameCheckbox(self.driver, username, state_on)

    def chat_element_by_text(self, text):
        chat_element = ChatElementByText(self.driver, text)
        self.driver.info("Looking for a message by text: %s" % chat_element.exclude_emoji(text))
        return chat_element

    def verify_message_is_under_today_text(self, text, errors, timeout=10):
        self.driver.info("Verifying that '%s' is under today" % text)
        message_element = self.chat_element_by_text(text)
        message_element.wait_for_visibility_of_element(timeout)
        message_location = message_element.find_element().location['y']
        today_text_element = self.element_by_text('Today').find_element()
        today_location = today_text_element.location['y']
        today_height = today_text_element.size['height']
        if message_location < today_location + today_height:
            errors.append("Message '%s' is not under 'Today' text" % text)

    def send_message(self, message: str = 'test message', wait_chat_input_sec=5):
        self.driver.info("Sending message '%s'" % BaseElement(self.driver).exclude_emoji(message))
        self.chat_message_input.wait_for_element(wait_chat_input_sec)
        for _ in range(3):
            try:
                self.chat_message_input.send_keys(message)
                break
            except (StaleElementReferenceException, InvalidElementStateException):
                time.sleep(1)
            except Exception as e:
                raise e
        else:
            raise StaleElementReferenceException(msg="Can't send keys to chat message input, loading")
        try:
            self.send_message_button.click()
        except NoSuchElementException:
            self.chat_message_input.clear()
            self.chat_message_input.send_keys(message)
            self.send_message_button.click()

    def pin_message(self, message, action="pin"):
        self.driver.info("Looking for message '%s' pin" % message)
        element = self.element_by_translation_id(action)
        self.chat_element_by_text(message).long_press_without_release()
        element.click_until_absense_of_element(element)

    def edit_message_in_chat(self, message_to_edit, message_to_update):
        self.driver.info("Looking for message '%s' to edit it" % message_to_edit)
        self.chat_element_by_text(message_to_edit).message_body.long_press_without_release()
        self.element_by_translation_id("edit-message").double_click()
        self.chat_message_input.clear()
        self.chat_message_input.send_keys(message_to_update)
        self.send_message_button.click()

    def delete_message_in_chat(self, message, everyone=True):
        self.driver.info("Looking for message '%s' to delete it" % message)
        if everyone:
            delete_button = self.element_by_translation_id("delete-for-everyone")
        else:
            delete_button = self.element_by_translation_id("delete-for-me")
        self.chat_element_by_text(message).message_body.long_press_without_release()
        delete_button.double_click()

    def copy_message_text(self, message_text):
        self.driver.info("Copying '%s' message via long press" % message_text)
        self.chat_element_by_text(message_text).wait_for_visibility_of_element()
        self.chat_element_by_text(message_text).long_press_without_release()
        self.element_by_translation_id("copy-text").double_click()

    def quote_message(self, message: str):
        self.driver.info("Quoting '%s' message" % message)
        element = self.chat_element_by_text(message)
        element.wait_for_sent_state()
        element.long_press_without_release()
        self.reply_message_button.double_click()

    def set_reaction(self, message: str, emoji: str = 'thumbs-up', emoji_message: bool = False,
                     times_to_long_press: int = 1):
        self.driver.info("Setting '%s' reaction" % emoji)
        element = Button(self.driver, accessibility_id='reaction-%s' % emoji)
        if emoji_message:
            self.element_by_text_part(message).long_press_without_release()
        else:
            for _ in range(times_to_long_press):
                self.chat_element_by_text(message).long_press_without_release()
        element.wait_for_element()
        element.double_click()
        element.wait_for_invisibility_of_element()

    def add_remove_same_reaction(self, emoji: str = 'thumbs-up'):
        self.driver.info("Adding one more '%s' reaction or removing an added one" % emoji)
        key = emojis[emoji]
        element = Button(self.driver, accessibility_id='emoji-reaction-%s' % key)
        element.wait_and_click()

    def block_contact(self):
        self.driver.info("Block contact from other user profile")
        self.profile_options_button.click()
        self.profile_block_contact_button.click()
        self.confirm_block_contact_button.click()

    def set_nickname(self, nickname, close_profile=True):
        self.driver.info("Setting nickname:%s" % nickname)
        self.profile_nickname_button.click()
        self.nickname_input_field.send_keys(nickname)
        self.element_by_text('Done').click()
        if close_profile:
            self.close_button.click()

    def convert_device_time_to_chat_timestamp(self) -> list:
        sent_time_object = dateutil.parser.parse(self.driver.device_time)
        timestamp = datetime.strptime("%s:%s" % (sent_time_object.hour, sent_time_object.minute), '%H:%M').strftime(
            "%I:%M %p")
        timestamp_obj = datetime.strptime(timestamp, '%I:%M %p')
        possible_timestamps_obj = [timestamp_obj + timedelta(0, 0, 0, 0, 1), timestamp_obj,
                                   timestamp_obj - timedelta(0, 0, 0, 0, 1), timestamp_obj - timedelta(0, 0, 0, 0, 2)]
        timestamps = list(map(lambda x: x.strftime("%I:%M %p"), possible_timestamps_obj))
        final_timestamps = [t[1:] if t[0] == '0' else t for t in timestamps]
        return final_timestamps

    def user_list_element_by_name(self, user_name: str):
        return BaseElement(self.driver, xpath="//*[@content-desc='user-list']//*[@text='%s']" % user_name)

    def mention_user(self, user_name: str):
        self.driver.info("Mention user %s in the chat" % user_name)
        self.chat_message_input.send_keys("@")
        try:
            self.mentions_list.wait_for_element()
            self.user_list_element_by_name(user_name).wait_for_rendering_ended_and_click()
        except TimeoutException:
            self.driver.fail("Mentions list is not shown")

    def get_image_by_index(self, index=0):
        return Button(self.driver, accessibility_id="image-%s" % index)

    def send_images_with_description(self, description, indexes=None):
        if indexes is None:
            indexes = [0]
        self.show_images_button.click()
        self.allow_button.click_if_shown()
        self.allow_all_button.click_if_shown()
        try:
            [self.get_image_by_index(i).click() for i in indexes]
        except NoSuchElementException:
            self.click_system_back_button()
            pytest.fail("Can't send image(s) with index(es) %s" % indexes)
        self.images_confirm_selection_button.click()
        self.chat_message_input.send_keys(description)
        self.send_message_button.click()

    def send_image_with_camera(self, description=None):
        self.take_photo_button.click()
        self.allow_button.click_if_shown()
        self.snap_button.click()
        self.element_by_translation_id("use-photo").click()
        if description:
            self.chat_message_input.send_keys(description)
        self.send_message_button.click()

    def authors_for_reaction(self, emoji: str):
        return Button(self.driver, accessibility_id='authors-for-reaction-%s' % emojis[emoji])
