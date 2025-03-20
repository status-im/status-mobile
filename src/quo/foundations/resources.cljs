(ns quo.foundations.resources)

(def ui
  {:keycard-logo       (js/require "../resources/images/ui2/keycard-logo.png")
   :keycard-chip-light (js/require "../resources/images/ui2/keycard-chip-light.png")
   :keycard-chip-dark  (js/require "../resources/images/ui2/keycard-chip-dark.png")
   :keycard-orange     (js/require "../resources/images/ui2/keycard-orange.png")
   :bored-ape          (js/require "../resources/images/mock2/bored-ape.png")})

(def ui-themed
  {:no-funds
   {:light (js/require "../resources/images/ui2/no-funds-light.png")
    :dark  (js/require "../resources/images/ui2/no-funds-dark.png")}
   :no-contacts-to-chat
   {:light (js/require "../resources/images/ui2/no-contacts-to-chat-light.png")
    :dark  (js/require "../resources/images/ui2/no-contacts-to-chat-dark.png")}})

(defn get-themed-image
  [k theme]
  (get-in ui-themed [k theme]))

(defn get-image
  [k]
  (get ui k))

(def dapps
  {:coingecko      (js/require "../resources/images/dapps/CoinGecko.png")
   :1inch          (js/require "../resources/images/dapps/1inch.png")
   :aave           (js/require "../resources/images/dapps/Aave.png")
   :uniswap        (js/require "../resources/images/dapps/Uniswap.png")
   :zapper         (js/require "../resources/images/dapps/Zapper.png")
   :zerion         (js/require "../resources/images/dapps/Zerion.png")
   :wallet-connect (js/require "../resources/images/dapps/WalletConnect.png")})

(defn get-dapp
  [k]
  (get dapps k))

(def ^:private networks
  {:arbitrum (js/require "../resources/images/networks/Arbitrum.png")
   :ethereum (js/require "../resources/images/networks/Ethereum.png")
   :mainnet  (js/require "../resources/images/networks/Ethereum.png")
   :gnosis   (js/require "../resources/images/networks/Gnosis.png")
   :hermez   (js/require "../resources/images/networks/Hermez.png")
   :optimism (js/require "../resources/images/networks/Optimism.png")
   :paraswap (js/require "../resources/images/networks/Paraswap.png")
   :base     (js/require "../resources/images/networks/Base.png")
   :polygon  (js/require "../resources/images/networks/Polygon.png")
   :scroll   (js/require "../resources/images/networks/Scroll.png")
   :taiko    (js/require "../resources/images/networks/Taiko.png")
   :unknown  (js/require "../resources/images/networks/Unknown.png")
   :xdai     (js/require "../resources/images/networks/xDAI.png")
   :zksync   (js/require "../resources/images/networks/zkSync.png")})

(defn get-network
  [k]
  (get networks k))
