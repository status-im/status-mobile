(ns status-im.react-native.resources)

(def ui
  {:empty-hashtags      (fn [] (js/require "./resources/images/ui/empty-hashtags.png"))
   :empty-recent        (fn [] (js/require "./resources/images/ui/empty-recent.png"))
   :analytics-image     (fn [] (js/require "./resources/images/ui/analytics-image.png"))
   :welcome-image       (fn [] (js/require "./resources/images/ui/welcome-image.png"))
   :lock                (fn [] (js/require "./resources/images/ui/lock.png"))
   :tribute-to-talk     (fn [] (js/require "./resources/images/ui/tribute-to-talk.png"))
   :wallet-welcome      (fn [] (js/require "./resources/images/ui/wallet-welcome.png"))
   :hardwallet-card     (fn [] (js/require "./resources/images/ui/hardwallet-card.png"))
   :secret-keys         (fn [] (js/require "./resources/images/ui/secret-keys.png"))
   :keycard-lock        (fn [] (js/require "./resources/images/ui/keycard-lock.png"))
   :hold-card-animation (fn [] (js/require "./resources/images/ui/hold-card-animation.gif"))
   :warning-sign        (fn [] (js/require "./resources/images/ui/warning-sign.png"))
   :phone-nfc-on        (fn [] (js/require "./resources/images/ui/phone-nfc-on.png"))
   :phone-nfc-off       (fn [] (js/require "./resources/images/ui/phone-nfc-off.png"))
   :dapp-store          (fn [] (js/require "./resources/images/ui/dapp-store.png"))
   :svg                 (fn [] (js/require "./resources/images/svg.png"))})

(def loaded-images (atom {}))

(defn get-image [k]
  (if (contains? @loaded-images k)
    (get @loaded-images k)
    (get (swap! loaded-images assoc k
                ((get ui k))) k)))
