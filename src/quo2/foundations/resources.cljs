(ns quo2.foundations.resources)

(def ui
  {:keycard-logo       (js/require "../resources/images/ui2/keycard-logo.png")
   :keycard-chip-light (js/require "../resources/images/ui2/keycard-chip-light.png")
   :keycard-chip-dark  (js/require "../resources/images/ui2/keycard-chip-dark.png")
   :keycard-watermark  (js/require "../resources/images/ui2/keycard-watermark.png")})

(defn get-image
  [k]
  (get ui k))

(def dapps
  {:coingecko (js/require "../resources/images/dapps/CoinGecko.png")
   :1inch     (js/require "../resources/images/dapps/1inch.png")
   :aave      (js/require "../resources/images/dapps/Aave.png")
   :uniswap   (js/require "../resources/images/dapps/Uniswap.png")
   :zapper    (js/require "../resources/images/dapps/Zapper.png")
   :zerion    (js/require "../resources/images/dapps/Zerion.png")})

(defn get-dapp
  [k]
  (get dapps k))

(def tokens
  {:eth  (js/require "../resources/images/tokens/mainnet/ETH-token.png")
   :knc  (js/require "../resources/images/tokens/mainnet/KNC.png")
   :mana (js/require "../resources/images/tokens/mainnet/MANA.png")
   :rare (js/require "../resources/images/tokens/mainnet/RARE.png")
   :dai  (js/require "../resources/images/tokens/mainnet/DAI.png")
   :fxc  (js/require "../resources/images/tokens/mainnet/FXC.png")
   :usdt (js/require "../resources/images/tokens/mainnet/USDT.png")
   :snt  (js/require "../resources/images/tokens/mainnet/SNT.png")})

(defn get-token
  [k]
  (get tokens k))

(def networks
  {:arbitrum (js/require "../resources/images/networks/Arbitrum.png")
   :ethereum (js/require "../resources/images/networks/Ethereum.png")
   :gnosis   (js/require "../resources/images/networks/Gnosis.png")
   :hermez   (js/require "../resources/images/networks/Hermez.png")
   :optimism (js/require "../resources/images/networks/Optimism.png")
   :polygon  (js/require "../resources/images/networks/Polygon.png")
   :scroll   (js/require "../resources/images/networks/Scroll.png")
   :taiko    (js/require "../resources/images/networks/Taiko.png")
   :unknown  (js/require "../resources/images/networks/Unknown.png")
   :xdai     (js/require "../resources/images/networks/xDAI.png")
   :zksync   (js/require "../resources/images/networks/zkSync.png")})

(defn get-network
  [k]
  (get networks k))
