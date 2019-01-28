(ns status-im.ui.screens.browser.default-dapps
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]))

(def all
  [{:title (i18n/label :t/default-dapps-exchanges)
    :data  [{:name        "Airswap"
             :dapp-url    "https://instant.airswap.io/"
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
             :description "The first decentralized instant exchange"}
            {:name        "DAI by MakerDao"
             :dapp-url    "https://dai.makerdao.com"
             :photo-path  "contacts://dai"
             :description "Stability for the blockchain"}
            {:name        "LocalEthereum"
             :dapp-url    "https://localethereum.com/"
             :photo-path  "contacts://local-ethereum"
             :description "The smartest way to buy and sell Ether"}
            {:name        "Eth2phone"
             :dapp-url    "https://eth2.io"
             :photo-path  "contacts://eth2phone"
             :description "Send Ether by phone number"}
            {:name        "DDEX"
             :dapp-url    "https://ddex.io/"
             :photo-path  "contacts://ddex"
             :description "Instant, real-time order matching with secure on-chain settlement"}
            {:name        "EasyTrade"
             :dapp-url    "https://easytrade.io"
             :photo-path  "contacts://easytrade"
             :description "One exchange for every token"}
            {:name        "slow.trade"
             :dapp-url    "https://slow.trade/"
             :photo-path  "contacts://slowtrade"
             :description "Trade fairly priced crypto assets on the first platform built with the DutchX protocol."}]}
   {:title (i18n/label :t/default-dapps-marketplaces)
    :data  [{:name        "blockimmo"
             :dapp-url    "https://blockimmo.ch"
             :photo-path  "contacts://blockimmo"
             :description "blockimmo is a blockchain powered, regulated platform enabling shared property investments and ownership."}
            {:name        "CryptoCribs"
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
             :description "ENS name marketplace"}
            {:name        "The Bounties Network"
             :dapp-url    "https://bounties.network/"
             :photo-path  "contacts://bounties-network"
             :description "Bounties on any task, paid in any token"}
            {:name        "Emoon"
             :dapp-url    "https://www.emoon.io/"
             :photo-path  "contacts://emoon"
             :description "A decentralized marketplace for buying & selling crypto assets"}
            {:name        "SuperRare"
             :dapp-url    "https://superrare.co/market"
             :photo-path  "contacts://superrare"
             :description "Buy, sell and collect unique digital creations by artists around the world"}]}
   {:title (i18n/label :t/default-dapps-fun-games)
    :data  [{:name        "CryptoKitties"
             :dapp-url    "https://www.cryptokitties.co"
             :photo-path  "contacts://cryptokitties"
             :description "Collect and breed adorable digital cats."}
            {:name        "CryptoFighters"
             :dapp-url    "https://cryptofighters.io"
             :photo-path  "contacts://cryptofighters"
             :description "Collect train and fight digital fighters."}
            {:name        "Cryptographics"
             :dapp-url    "https://cryptographics.app/"
             :photo-path  "contacts://cryptographics"
             :description "Cryptographics is a digital art hub where artists, creators and collectors can submit asset packs, create unique cryptographics and trade them."}
            {:name        "CryptoPunks"
             :dapp-url    "https://www.larvalabs.com/cryptopunks"
             :photo-path  "contacts://cryptopunks"
             :description "10,000 unique collectible punks"}
            {:name        "Crypto Takeovers"
             :dapp-url    "https://cryptotakeovers.com/"
             :photo-path  "contacts://cryptotakeovers"
             :description "Predict and conquer the world. Make a crypto fortune."}
            {:name        "Decentraland"
             :dapp-url    "https://market.decentraland.org/"
             :photo-path  "contacts://decentraland"
             :description "Decentraland is a virtual reality platform powered by the Ethereum blockchain."}
            {:name        "Dragonereum"
             :dapp-url    "https://dapp.dragonereum.io"
             :photo-path  "contacts://dragonereum"
             :description "Own and trade dragons, fight with other players."}
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
   {:title (i18n/label :t/default-dapps-social-networks)
    :data  [{:name        "Cent"
             :dapp-url    "https://beta.cent.co/"
             :photo-path  "contacts://cent"
             :description "Get wisdom, get money"}
            {:name        "Kickback"
             :dapp-url    "https://kickback.events/"
             :photo-path  "contacts://kickback"
             :description "Event no shows? No problem. Kickback asks event attendees to put skin in the game with Ethereum."}
            {:name        "Peepeth"
             :dapp-url    "https://peepeth.com/"
             :photo-path  "contacts://peepeth"
             :description "Blockchain-powered microblogging"}
            {:name        "Purrbook"
             :dapp-url    "https://cryptopurr.co/"
             :photo-path  "contacts://cryptopurr"
             :description "A social network for CryptoKitties"}]}
   {:title (i18n/label :t/default-dapps-media)
    :data  [{:name        "livepeer.tv"
             :dapp-url    "http://livepeer.tv/"
             :photo-path  "contacts://livepeer"
             :description "Decentralized video broadcasting"}]}
   {:title (i18n/label :t/default-dapps-social-utilities)
    :data  [{:name        "3Box"
             :dapp-url    "https://3box.io/"
             :photo-path  "contacts://3box"
             :description "Create and manage your Ethereum Profile."}
            {:name        "Aragon"
             :dapp-url    "https://mainnet.aragon.org/"
             :photo-path  "contacts://aragon"
             :description "Build unstoppable organizations on Ethereum."}
            {:name        "Civitas"
             :dapp-url    "https://communities.colu.com/"
             :photo-path  "contacts://civitas"
             :description "Blockchain-powered local communities"}
            {:name        "ETHLend"
             :dapp-url    "https://app.ethlend.io"
             :photo-path  "contacts://ethlend"
             :description "Decentralized lending on Ethereum"}
            {:name        "Hexel"
             :dapp-url    "https://www.onhexel.com/"
             :photo-path  "contacts://hexel"
             :description "Create your own cryptocurrency"}
            {:name        "Livepeer"
             :dapp-url    "https://explorer.livepeer.org/"
             :photo-path  "contacts://livepeer"
             :description "Decentralized video broadcasting"}
            {:name        "Smartz"
             :dapp-url    "https://smartz.io"
             :photo-path  "contacts://smartz"
             :description "Easy smart contract management"}
            {:name        "SNT Voting DApp"
             :dapp-url    "https://vote.status.im"
             :photo-path  "contacts://snt-voting"
             :description "Let your SNT be heard! Vote on decisions exclusive to SNT holders, or create a poll of your own."}
            {:name        "Status Test DApp"
             :dapp-url    "simpledapp.eth"
             :description "Request test assets and test basic web3 functionality."
             :developer?  true}]}])

(re-frame/reg-cofx
 :get-default-dapps
 (fn [coeffects _]
   (assoc coeffects :default-dapps all)))
