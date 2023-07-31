from tests.users import ens_user_message_sender
main = {
    'name' : 'main chat',
    'messages': {
        'text': 'before upgrade',
        'reply': 'reply',
        'invite': 'https://status.app/g/args?a=0x04359bb3e73cba0b815d71e562670ad00bb5d2db0d16cd1c4c92c668b61fde2274d6e487fcdffe66f913b3fea2a3058f53ce7946c2b501aa61a9ca8a883df72dc9&a1=empty&a2=a935c4fa-5530-407c-84c2-61512120d504-0x04359bb3e73cba0b815d71e562670ad00bb5d2db0d16cd1c4c92c668b61fde2274d6e487fcdffe66f913b3fea2a3058f53ce7946c2b501aa61a9ca8a883df72dc9'
    }
}
to_join = {
    'name' : 'To Join',
}

to_remove = {
    'name' : 'To remove',
}

to_delete = {
    'name' : 'to delete',
}

empty_invite = {
    'name': 'empty',
    'texts': {
        'Heya, accept me pls',
        'Request pending…'
    }

}
make_admin =  {
    'name' : 'To make admin',
}

timeline = {
    'link': 'https://status.app/u/0x045efbcc044e5ae21ac3cf111ea6df6186e0cc50a2cd747f52a56d19ce516e683c66cb47f4b0a21110859aea9592dfba1e0bf4af11ff3eab995f844b3673643bf1',
    'text': 'Hey there!',
    'resolved_username': ens_user_message_sender['username']

}
profile = {
    'log_level': 'DEBUG',
}