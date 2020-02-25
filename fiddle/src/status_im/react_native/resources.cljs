(ns status-im.react-native.resources)

(def ui
  {:lock                "images/ui/lock.png"
   :tribute-to-talk     "images/ui/tribute-to-talk.png"
   :hardwallet-card     "images/ui/hardwallet-card.png"
   :secret-keys         "images/ui/secret-keys.png"
   :keycard-lock        "images/ui/keycard-lock.png"
   :keycard             "images/ui/keycard.png"
   :keycard-logo        "images/ui/keycard-logo.png"
   :keycard-logo-blue   "images/ui/keycard-logo-blue.png"
   :keycard-logo-gray   "images/ui/keycard-logo-gray.png"
   :keycard-key         "images/ui/keycard-key.png"
   :keycard-empty       "images/ui/keycard-empty.png"
   :keycard-phone       "images/ui/keycard-phone.png"
   :keycard-connection  "images/ui/keycard-connection.png"
   :keycard-wrong       "images/ui/keycard-wrong.png"
   :not-keycard         "images/ui/not-keycard.png"
   :status-logo         "images/ui/status-logo.png"
   :hold-card-animation "images/ui/hold-card-animation.gif"
   :warning-sign        "images/ui/warning-sign.png"
   :phone-nfc-on        "images/ui/phone-nfc-on.png"
   :phone-nfc-off       "images/ui/phone-nfc-off.png"
   :dapp-store          "images/ui/dapp-store.png"
   :ens-header          "images/ui/ens-header.png"})

(def loaded-images (atom {}))

(defn get-image [k]
  (if (contains? @loaded-images k)
    (get @loaded-images k)
    (get (swap! loaded-images assoc k
                (get ui k)) k)))
