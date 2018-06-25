(ns status-im.ui.screens.contacts.default-dapps)

(def all
  [{:title "Exchanges"
    :data [{:name        "Airswap"
            :dapp-url    "https://www.airswap.io/trade"
            :photo-path  "contacts://airswap"
            :description "Meet the future of trading."}
           {:name        "Bancor"
            :dapp-url    "https://www.bancor.network/"
            :photo-path  "contacts://bancor"
            :description "Bancor is a decentralized liquidity network"}
           {:name        "ERC dEX"
            :dapp-url    "https://app.ercdex.com/"
            :photo-path  "contacts://erc-dex"
            :description "Trustless trading has arrived on Ethereum"}
           {:name        "Kyber"
            :dapp-url    "https://web3.kyber.network"
            :photo-path  "contacts://kyber"
            :description "On-chain, instant and liquid platform for exchange and payment service"}
           {:name        "Oasis Direct"
            :dapp-url    "https://oasis.direct/"
            :photo-path  "contacts://oasis-direct"
            :description "The first decentralized instant exchange"}]}
   {:title "Marketplaces"
    :data [{:name        "CryptoCribs"
            :dapp-url    "https://cryptocribs.com"
            :photo-path  "contacts://cryptocribs"
            :description "Travel the globe. Pay in crypto."}
           {:name        "Ethlance"
            :dapp-url    "https://ethlance.com"
            :photo-path  "contacts://ethlance"
            :description "The future of work is now. Hire people or work yourself in return for ETH."}
           {:name        "OpenSea"
            :dapp-url    "https://opensea.io"
            :photo-path  "contacts://opensea"
            :description "The largest decentralized marketplace for cryptogoods"}
           {:name        "Name Bazaar"
            :dapp-url    "https://namebazaar.io"
            :photo-path  "contacts://name-bazaar"
            :description "ENS name marketplace"}]}
   {:title "Fun & Games"
    :data [{:name        "CryptoKitties"
            :dapp-url    "https://www.cryptokitties.co"
            :photo-path  "contacts://cryptokitties"
            :description "Collect and breed adorable digital cats."}
           {:name        "CryptoFighters"
            :dapp-url    "https://cryptofighters.io"
            :photo-path  "contacts://cryptofighters"
            :description "Collect train and fight digital fighters."}
           {:name        "CryptoPunks"
            :dapp-url    "https://www.larvalabs.com/cryptopunks"
            :photo-path  "contacts://cryptopunks"
            :description "10,000 unique collectible punks"}
           {:name        "Etherbots"
            :dapp-url    "https://etherbots.io/"
            :photo-path  "contacts://etherbots"
            :description "Robot wars on the Ethereum Platform"}
           {:name        "Etheremon"
            :dapp-url    "https://www.etheremon.com/"
            :photo-path  "contacts://etheremon"
            :description "Decentralized World of Ether Monsters"}
           {:name        "CryptoStrikers"
            :dapp-url    "https://www.cryptostrikers.com/"
            :photo-path  "contacts://cryptostrikers"
            :description "The Beautiful (card) Game"}]}
   {:title "Social Networks"
    :data [{:name        "Cent"
            :dapp-url    "https://beta.cent.co"
            :photo-path  "contacts://cent"
            :description "Get wisdom, get money"}
           {:name        "Peepeth"
            :dapp-url    "http://peepeth.com/"
            :photo-path  "contacts://peepeth"
            :description "Blockchain-powered microblogging"}]}
   {:title "Utilities"
    :data [{:name        "Hexel"
            :dapp-url    "https://www.onhexel.com/"
            :photo-path  "contacts://hexel"
            :description "Create your own cryptocurrency"}
           {:name        "Status Test DApp"
            :dapp-url    "https://status-im.github.io/dapp/"
            :description "Request test assets and test basic web3 functionality."
            :developer?   true}]}])
