

def set_password_as_new_user(*args):
    for view in args:
        view.request_password_icon.click()
        view.type_message_edit_box.send_keys("qwerty1234")
        view.confirm()
        view.type_message_edit_box.send_keys("qwerty1234")
        view.confirm()
        view.find_full_text("Tap here to enter your phone number & I\'ll find your friends")
