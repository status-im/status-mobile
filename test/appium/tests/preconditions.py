

def set_chat_for_users_from_scratch(*args):
    for view in args:
        view.request_password_icon.click()
        view.type_message_edit_box.send_keys("qwerty1234")
        view.confirm()
        view.type_message_edit_box.send_keys("qwerty1234")
        view.confirm()
        view.find_text("Tap here to enter your phone number & I\'ll find your friends")
