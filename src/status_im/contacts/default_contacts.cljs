(ns status-im.contacts.default-contacts
  (:require [status-im.constants :refer [wallet-chat-id]]
            [clojure.string :as s]))

(def contacts
  [{:whisper-identity wallet-chat-id
    :name             (s/capitalize wallet-chat-id)
    :photo-path       :icon_wallet_avatar
    :dapp?            true
    :add-chat?        true}
   
   {:whisper-identity "dapp-auction"
    :name             "Auction House"
    :photo-path       "http://auctionhouse.dappbench.com/images/auctionhouse.png"
    :dapp?            true
    :dapp-url         "http://auctionhouse.dappbench.com"}
   ])
