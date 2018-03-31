def create_chart_one_to_one_chat(one_to_one_chat_data: dict):
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt

    user_a = one_to_one_chat_data['user_a']
    user_b = one_to_one_chat_data['user_b']

    fig, ax = plt.subplots(nrows=1, ncols=1, figsize=(15, 7))
    time_1 = sorted(user_a['message_time'])
    ax.plot([i / 60 for i in time_1], [user_a['message_time'][i] for i in time_1],
            'o-', color='#0c0fea', label='user_a')
    time_2 = sorted(user_b['message_time'])
    ax.plot([i / 60 for i in time_2], [user_b['message_time'][i] for i in time_2],
            'o-', color='#f61e06', label='user_b')
    sent_messages = user_a['sent_messages'] + user_b['sent_messages']
    title = "User A: sent messages: {}, received messages: {}" \
            "\nUser B: sent messages: {}, received messages: {}".format(user_a['sent_messages'],
                                                                        len(user_a['message_time']),
                                                                        user_b['sent_messages'],
                                                                        len(user_b['message_time']))
    if sent_messages:
        title += "\nReceived messages: {}%".format(
            round((len(user_a['message_time']) + len(user_b['message_time'])) / sent_messages * 100, ndigits=2))
    plt.title(title)
    plt.xlabel('chat session duration, minutes')
    plt.ylabel('time to receive a message, seconds')
    plt.legend()
    fig.savefig('chart.png')


def create_chart_public_chat(public_chat_data: dict):
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt

    sent_messages = public_chat_data['sent_messages']
    message_time = public_chat_data['message_time']

    fig, ax = plt.subplots(nrows=1, ncols=1, figsize=(15, 7))
    sorted_time = sorted(message_time)
    ax.plot([i / 60 for i in sorted_time], [message_time[i] for i in sorted_time], 'o-', color='#0c0fea')
    title = "Sent messages: {}\nReceived messages: {}".format(sent_messages, len(message_time))
    plt.title(title)
    plt.xlabel('chat session duration, minutes')
    plt.ylabel('time to receive a message, seconds')
    plt.legend()
    fig.savefig('chart.png')
