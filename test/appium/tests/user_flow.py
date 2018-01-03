from tests import get_current_time


def create_user(console):
    console.request_password_icon.click()
    console.chat_request_input.send_keys("qwerty1234")
    console.confirm()
    console.chat_request_input.send_keys("qwerty1234")
    console.confirm()
    console.find_full_text(
        "Here is your signing phrase. You will use it to verify your transactions. Write it down and keep it safe!")


def recover_access(console, passphrase, password, username):
    recover_access_view = console.recover_button.click()
    recover_access_view.passphrase_input.send_keys(passphrase)
    recover_access_view.password_input.send_keys(password)
    recover_access_view.confirm_recover_access.click()
    recovered_user = recover_access_view.element_by_text(username, 'button')
    recover_access_view.confirm()
    recovered_user.click()
    recover_access_view.password_input.send_keys(password)
    recover_access_view.sign_in_button.click()
    recover_access_view.find_full_text('Chats', 60)


def get_public_key(chat):
    profile_drawer = chat.profile_button.click()
    profile_view = profile_drawer.profile_icon.click()
    return profile_view.public_key_text.text


def get_address(chat):
    profile_drawer = chat.profile_button.click()
    profile_view = profile_drawer.profile_icon.click()
    return profile_view.profile_address_text.text


def add_contact(chat, public_key):
    start_new_chat = chat.plus_button.click()
    start_new_chat.add_new_contact.click()
    start_new_chat.public_key_edit_box.send_keys(public_key)
    start_new_chat.confirm()
    start_new_chat.confirm_public_key_button.click()


def create_group_chat(chat, username_to_add, group_chat_name='new_group_chat'):
    start_new_chat = chat.plus_button.click()
    start_new_chat.new_group_chat_button.click()
    user_contact = start_new_chat.element_by_text(username_to_add, 'button')
    user_contact.scroll_to_element()
    user_contact.click()
    start_new_chat.next_button.click()
    start_new_chat.name_edit_box.send_keys(group_chat_name)
    start_new_chat.save_button.click()


def get_new_username_and_status(*args):
    users_details = dict()
    for device, view in enumerate(args):
        current_time = get_current_time()
        new_status = '#newstatus_%s' % current_time
        new_username = 'New_User_Name_%s' % current_time

        view.user_status_box.click()
        view.user_status_input.clear()
        view.user_status_input.send_keys(new_status)
        view.username_input.clear()
        view.username_input.send_keys(new_username)
        view.save_button.click()
        users_details[device] = dict()
        users_details[device]['status'] = new_status
        users_details[device]['name'] = new_username
    return users_details
