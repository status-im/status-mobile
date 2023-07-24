(ns quo2.foundations.resources)

(def ui
  {:keycard-logo       (js/require "../resources/images/ui2/keycard-logo.png")
   :keycard-chip-light (js/require "../resources/images/ui2/keycard-chip-light.png")
   :keycard-chip-dark  (js/require "../resources/images/ui2/keycard-chip-dark.png")
   :keycard-watermark  (js/require "../resources/images/ui2/keycard-watermark.png")})

(def tokens
  {:eth  (js/require "../resources/images/tokens/mainnet/ETH.png")
   :knc  (js/require "../resources/images/tokens/mainnet/KNC.png")
   :mana (js/require "../resources/images/tokens/mainnet/MANA.png")
   :rare (js/require "../resources/images/tokens/mainnet/RARE.png")
   :dai  (js/require "../resources/images/tokens/mainnet/DAI.png")
   :fxc  (js/require "../resources/images/tokens/mainnet/FXC.png")
   :usdt (js/require "../resources/images/tokens/mainnet/USDT.png")
   :snt  (js/require "../resources/images/tokens/mainnet/SNT.png")})

(defn get-image
  [k]
  (get ui k))
