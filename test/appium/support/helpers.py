from bip_utils import Bip39SeedGenerator, Bip44, Bip44Coins, Bip44Changes


def generate_wallet_address(passphrase: str, number: int = 1):
    # Derive accounts using the standard MetaMask path
    seed_bytes = Bip39SeedGenerator(passphrase).Generate()
    bip44_mst_ctx = Bip44.FromSeed(seed_bytes, Bip44Coins.ETHEREUM)

    # Generate first n addresses
    expected_addresses = list()
    for i in range(number):
        bip44_acc_ctx = bip44_mst_ctx.Purpose().Coin().Account(0).Change(Bip44Changes.CHAIN_EXT).AddressIndex(i)
        expected_addresses.append(bip44_acc_ctx.PublicKey().ToAddress().lower())

    return expected_addresses
