(ns status-im.utils.ethereum.tokens
  (:require [status-im.ui.components.styles :as styles])
  (:require-macros [status-im.utils.ethereum.macros :refer [resolve-icons]]))

(defn- asset-border [color]
  {:border-color color :border-width 1 :border-radius 32})

(def ethereum {:name     "Ethereum"
               :symbol   :ETH
               :decimals 18
               :icon     {:source (js/require "./resources/images/assets/ethereum.png")
                          :style  (asset-border styles/color-light-blue-transparent)}})

(def all
  {:mainnet
   (resolve-icons
     [{:name     "Status Network Token"
       :symbol   :SNT
       :decimals 18
       :address  "0x744d70FDBE2Ba4CF95131626614a1763DF805B9E"}])
   :testnet
   (resolve-icons
     [{:name     "Status Test Token"
       :symbol   :STT
       :decimals 18
       :address  "0xc55cf4b03948d7ebc8b9e8bad92643703811d162"}])})

(defn token-for [chain-id symbol]
  (some #(if (= symbol (:symbol %)) %) (get all chain-id)))