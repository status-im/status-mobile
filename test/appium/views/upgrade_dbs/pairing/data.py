from tests.users import basic_user, ens_user_ropsten

contacts ={
    'blocked': 'Athletic Lean Noctilio',
    'removed': 'Utilized',
    'synced': {
        'nickname': 'My buddy',
        'username_nickname': 'Worthy Shady Harrier',
        'ens': '@%s'% ens_user_ropsten['ens'],
        'username_ens': ens_user_ropsten['username'],
    },
    'added': {
        'name': 'my_second_buddy',
        'username': basic_user['username'],
        'public_key': basic_user['public_key']

    }
}
chats = {
    'deleted': '#status',
    'synced_public':{'#pairing', '#crypto'},
    'added_public': '#after-upgrade',
    'group': 'Group chat',
}