import itertools


def passpharse_with_spaces(passphrase):
    phrase_list = passphrase.split()
    return ''.join(list(itertools.chain.from_iterable(zip(phrase_list, [' ' * i for i in range(1, 13)]))))
