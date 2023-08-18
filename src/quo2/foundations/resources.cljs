(ns quo2.foundations.resources)

(def ui
  {:keycard-logo              (js/require "../resources/images/ui2/keycard-logo.png")
   :keycard-chip-light        (js/require "../resources/images/ui2/keycard-chip-light.png")
   :keycard-chip-dark         (js/require "../resources/images/ui2/keycard-chip-dark.png")
   :keycard-watermark         (js/require "../resources/images/ui2/keycard-watermark.png")
   :network-link-1x-dark      (js/require "../resources/images/ui2/network-link-1x-dark.png")
   :network-link-1x-light     (js/require "../resources/images/ui2/network-link-1x-light.png")
   :network-link-2x-dark      (js/require "../resources/images/ui2/network-link-2x-dark.png")
   :network-link-2x-light     (js/require "../resources/images/ui2/network-link-2x-light.png")
   :network-link-linear-dark  (js/require "../resources/images/ui2/network-link-linear-dark.png")
   :network-link-linear-light (js/require "../resources/images/ui2/network-link-linear-light.png")})

(defn get-image
  [k]
  (get ui k))

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
  {:ethereum (js/require "../resources/images/tokens/mainnet/ETH-network.png")
   :optimism (js/require "../resources/images/tokens/mainnet/OP.png")
   :arbitrum (js/require "../resources/images/tokens/mainnet/ARB.png")})
