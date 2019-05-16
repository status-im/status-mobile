(ns status-im.react-native.resources)

(def ui
  {:empty-hashtags      "./resources/images/ui/empty-hashtags.png"
   :empty-recent        "./resources/images/ui/empty-recent.png"
   :analytics-image     "./resources/images/ui/analytics-image.png"
   :welcome-image       "./resources/images/ui/welcome-image.png"
   :lock                "./resources/images/ui/lock.png"
   :tribute-to-talk     "./resources/images/ui/tribute-to-talk.png"
   :wallet-welcome      "./resources/images/ui/wallet-welcome.png"
   :hardwallet-card     "./resources/images/ui/hardwallet-card.png"
   :secret-keys         "./resources/images/ui/secret-keys.png"
   :keycard-lock        "./resources/images/ui/keycard-lock.png"
   :hold-card-animation "./resources/images/ui/hold-card-animation.gif"
   :warning-sign        "./resources/images/ui/warning-sign.png"
   :phone-nfc-on        "./resources/images/ui/phone-nfc-on.png"
   :phone-nfc-off       "./resources/images/ui/phone-nfc-off.png"
   :dapp-store          "./resources/images/ui/dapp-store.png"})

(def loaded-images (atom {}))

(defn get-image [k]
  (if (contains? @loaded-images k)
    (get @loaded-images k)
    (get (swap! loaded-images assoc k
                (js/require (get ui k))) k)))
