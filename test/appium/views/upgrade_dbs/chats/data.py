from tests import transl
chats = {
    'All Whopping Dassierat': {
        'preview': 'unread 1-1',
        'unread' : '1',
        'messages': {
            'pic with descr',
            'unread 1-1'
        }
    },
    '#before-upgrade':{
        'preview_messages':{
            'status_im':    {'txt':'1) https://our.status.im/tag/news-and-announcements/',
                             'title': 'News & Announcements - Our Status',
                             'subtitle':'Our Status'},
            'youtu.be':     {'txt':'short youtu.be https://youtu.be/LesnixX76YY',
                             'title':'Short URLs or Branded Links?',
                             'subtitle': 'YouTube'},
            'youtube':      {'txt': '2. YOTUBE full https://www.youtube.com/watch?v=5sd7gJTnFRM',
                             'title': 'Animal ДжаZ — Чувства',
                             'subtitle': 'YouTube'},
            'gph.is':       {'txt':'4. short vertical gif https://gph.is/2jG1Xjj'},
            'giphy.com':    {'txt':'5.  long landscape gif https://giphy.com/gifs/thegoodplace-season-1-episode-11-3oxHQoD2rep6XGbDDa'},
            'media.giphy':  {'txt':'6. media gif https://media.giphy.com/media/iFxXouCf76ZencqIRP/giphy.gif'},
            'github':       {'txt': '7. github. com https://github.com/status-im/status-react/ ',
                             'title': 'status-im/status-react',
                             'subtitle': 'GitHub'}
        },
    },
    '#before-upgrade-2':{
        'mention': 'Burlywood Vapid Zethuswasp be my friend',
        'long': 'Папирус (др.-греч. πάπῡρος, лат. papyrus), или би́блиос (др.-греч. βιβλίος), также ха́рта',
        'reply': 'reply',
    },
    '#before-upgrade-3':{
            'tag': '#what-is-going-on',
            'quoted': 'quoted 1',
            'markdown_text_messages': [
                'inline code',
                'bold text in asterics',
                'bold text in underscores',
                'italic text in asteric',
                'italic text in underscore',
                'code blocks\n8\n9']
    },
    'Thoughtful Stupendous Graywolf': {
        'preview':'hey Thoughtful Stupendous Graywolf !',
    },
    'Royal Defensive Solenodon': {
        'preview':'Request address for transaction accepted',
        'messages': {
            'audio': {
                'length':'00:10',
                'timestamp' : '1:02 PM'
            },
        },
        'commands':{
            'incoming_ETH_shared':{
                'value':'0.01 ETH',
                'status': "Shared 'Ethereum account'"
            },
            'incoming_ETH_confirmed': {
                'value': '0.1 ETH',
                'status': transl["status-confirmed"]
            },
            'incoming_ETH_declined': {
                'value': '20 ETH',
                'status': transl["transaction-declined"]
            },
            'incoming_STT_confirmed': {
                'value': '200 STT',
                'status': transl["status-confirmed"]
            },
            'outgoing_STT_sign': {
                'value': '5 STT',
                'status': transl["address-request-accepted"],
            },
        },
    }
}
