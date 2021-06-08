from eth_utils import to_checksum_address, is_address
from web3.auto.infura.ropsten import w3


token_data = {"STT": [{
                          "abi": '[{"constant":true,"inputs":[],"name":"name","outputs":[{"name":"","type":"string"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_spender","type":"address"},{"name":"_amount","type":"uint256"}],"name":"approve","outputs":[{"name":"success","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"creationBlock","outputs":[{"name":"","type":"uint256"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"totalSupply","outputs":[{"name":"","type":"uint256"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_from","type":"address"},{"name":"_to","type":"address"},{"name":"_amount","type":"uint256"}],"name":"transferFrom","outputs":[{"name":"success","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"decimals","outputs":[{"name":"","type":"uint8"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_newController","type":"address"}],"name":"changeController","outputs":[],"payable":false,"type":"function"},{"constant":true,"inputs":[{"name":"_owner","type":"address"},{"name":"_blockNumber","type":"uint256"}],"name":"balanceOfAt","outputs":[{"name":"","type":"uint256"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"version","outputs":[{"name":"","type":"string"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_cloneTokenName","type":"string"},{"name":"_cloneDecimalUnits","type":"uint8"},{"name":"_cloneTokenSymbol","type":"string"},{"name":"_snapshotBlock","type":"uint256"},{"name":"_transfersEnabled","type":"bool"}],"name":"createCloneToken","outputs":[{"name":"","type":"address"}],"payable":false,"type":"function"},{"constant":true,"inputs":[{"name":"_owner","type":"address"}],"name":"balanceOf","outputs":[{"name":"balance","type":"uint256"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"parentToken","outputs":[{"name":"","type":"address"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_owner","type":"address"},{"name":"_amount","type":"uint256"}],"name":"generateTokens","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"symbol","outputs":[{"name":"","type":"string"}],"payable":false,"type":"function"},{"constant":true,"inputs":[{"name":"_blockNumber","type":"uint256"}],"name":"totalSupplyAt","outputs":[{"name":"","type":"uint256"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_to","type":"address"},{"name":"_amount","type":"uint256"}],"name":"transfer","outputs":[{"name":"success","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"transfersEnabled","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"parentSnapShotBlock","outputs":[{"name":"","type":"uint256"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_spender","type":"address"},{"name":"_amount","type":"uint256"},{"name":"_extraData","type":"bytes"}],"name":"approveAndCall","outputs":[{"name":"success","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_owner","type":"address"},{"name":"_amount","type":"uint256"}],"name":"destroyTokens","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[{"name":"_owner","type":"address"},{"name":"_spender","type":"address"}],"name":"allowance","outputs":[{"name":"remaining","type":"uint256"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_token","type":"address"}],"name":"claimTokens","outputs":[],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"tokenFactory","outputs":[{"name":"","type":"address"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_transfersEnabled","type":"bool"}],"name":"enableTransfers","outputs":[],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"controller","outputs":[{"name":"","type":"address"}],"payable":false,"type":"function"},{"inputs":[{"name":"_tokenFactory","type":"address"}],"payable":false,"type":"constructor"},{"payable":true,"type":"fallback"},{"anonymous":false,"inputs":[{"indexed":true,"name":"_token","type":"address"},{"indexed":true,"name":"_controller","type":"address"},{"indexed":false,"name":"_amount","type":"uint256"}],"name":"ClaimedTokens","type":"event"},{"anonymous":false,"inputs":[{"indexed":true,"name":"_from","type":"address"},{"indexed":true,"name":"_to","type":"address"},{"indexed":false,"name":"_amount","type":"uint256"}],"name":"Transfer","type":"event"},{"anonymous":false,"inputs":[{"indexed":true,"name":"_cloneToken","type":"address"},{"indexed":false,"name":"_snapshotBlock","type":"uint256"}],"name":"NewCloneToken","type":"event"},{"anonymous":false,"inputs":[{"indexed":true,"name":"_owner","type":"address"},{"indexed":true,"name":"_spender","type":"address"},{"indexed":false,"name":"_amount","type":"uint256"}],"name":"Approval","type":"event"}]',
                          "address": "0xc55cF4B03948D7EBc8b9E8BAD92643703811d162"}]}

ACCOUNT_PRIVATE_KEY = '0x5507f8c5c12707770c12fd0fae5d012b947d61f43b9203ae67916e703fd12ad7'


class Account(object):

    def __init__(self, account_private_key):
        self.pk = account_private_key

    @property
    def account_address(self):
        return w3.eth.account.from_key(self.pk).address

    @property
    def nonce(self):
        return w3.eth.getTransactionCount(self.account_address)

    @property
    def balance(self):
        return w3.eth.getBalance(self.account_address)

    def send_eth(self, to_address, eth_value, gas_price_increment=0):
        signed_txn = w3.eth.account.sign_transaction(dict(
            nonce=self.nonce,
            gasPrice=w3.eth.gasPrice + gas_price_increment * 1000000000,
            gas=21000,
            to=to_address,
            value=int(eth_value * 10 ** 18),
            data=b'',
        ),
            self.pk,
        )
        w3.eth.sendRawTransaction(signed_txn.rawTransaction)
        return w3.toHex(w3.sha3(signed_txn.rawTransaction))


class ContractInteractions(object):

    def __init__(self, contract_address, abi):
        self.contract = w3.eth.contract(address=contract_address, abi=abi)

    @property
    def decimals(self):
        return self.contract.functions.decimals().call()

    def balance_of(self, account_address):
        return self.contract.functions.balanceOf(account_address).call()

    def nonce(self, account_address):
        return w3.eth.getTransactionCount(account_address)

    def transfer_token_to(self, from_address, to_address, number_of_tokens, nonce, gas_price_increment=0):
        gas_price = w3.eth.gasPrice + gas_price_increment * 1000000000
        token_value = int(number_of_tokens * 10 ** self.decimals)
        return self.contract.functions.transfer(to_address, token_value, ).buildTransaction({
            'from': from_address,
            'gasPrice': gas_price,
            'gas': 600000,
            'nonce': nonce}
        )


def balance_of_address(address):
    if not is_address(address):
        return ("Invalid address provided")
    else:
        return w3.eth.getBalance(to_checksum_address(address))

def transaction_status(hash):
    return w3.eth.getTransaction(hash)

def to_checksumed_address(address):
    return to_checksum_address(address)


def current_gas_price():
    return str(w3.eth.gasPrice / 1000000000)


def sign_transaction(tx_data, pk):
    return w3.eth.account.signTransaction(tx_data, pk)


def broadcast_signed_tx(signed_txn):
    w3.eth.sendRawTransaction(signed_txn.rawTransaction)
    return w3.toHex(w3.sha3(signed_txn.rawTransaction))


account = Account(ACCOUNT_PRIVATE_KEY)
account_address = account.account_address


def donate_testnet_eth(address=str(), amount=float(), inscrease_default_gas_price=int()):
    """
    address: address where to send ETH to
    amount: amount in Ether form
    inscrease_default_gas_price: specify GWEI value (int) if you want to speed up transaction pick up
    """
    return account.send_eth(address, amount, inscrease_default_gas_price)


def donate_testnet_token(token_name=str(), address=str(), amount=float(), inscrease_default_gas_price=int()):
    """
    token_name: token 'name' value you want to send taken from token_data
    address: address where to send ETH to
    amount: amount in Ether form
    inscrease_default_gas_price: specify GWEI value (int) if you want to speed up transaction pick up
    """
    token_contract = ContractInteractions(token_data[token_name][0]['address'], token_data[token_name][0]['abi'])
    to_address_data = token_contract.transfer_token_to(
        from_address=account_address,
        to_address=address,
        number_of_tokens=amount,
        nonce=token_contract.nonce(account_address),
        gas_price_increment=inscrease_default_gas_price)
    signed_tx = sign_transaction(tx_data=to_address_data, pk=account.pk)
    return broadcast_signed_tx(signed_tx)
