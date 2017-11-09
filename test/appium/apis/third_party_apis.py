import requests


def get_ethereum_price_in_usd() -> float:
    url = 'https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=USD'
    return float(requests.request('GET', url).json()['USD'])
