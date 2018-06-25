import requests


def get_ethereum_price_in_usd() -> float:
    url = 'https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=USD'
    return float(requests.request('GET', url).json()['USD'])


def get_token_info(address: str):
    url = 'http://api.ethplorer.io/getTokenInfo/%s?apiKey=freekey' % address
    return requests.request('GET', url).json()
