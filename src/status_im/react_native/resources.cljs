(ns status-im.react-native.resources
  (:require-macros [status-im.utils.js-require :as js-require]))

(def ui
  {:empty-hashtags      (js-require/js-require "./resources/images/ui/empty-hashtags.png")
   :empty-recent        (js-require/js-require "./resources/images/ui/empty-recent.png")
   :analytics-image     (js-require/js-require "./resources/images/ui/analytics-image.png")
   :welcome-image       (js-require/js-require "./resources/images/ui/welcome-image.png")
   :intro1              (js-require/js-require "./resources/images/ui/intro1.png")
   :intro2              (js-require/js-require "./resources/images/ui/intro2.png")
   :intro3              (js-require/js-require "./resources/images/ui/intro3.png")
   :sample-key          (js-require/js-require "./resources/images/ui/sample-key.png")
   :lock                (js-require/js-require "./resources/images/ui/lock.png")
   :tribute-to-talk     (js-require/js-require "./resources/images/ui/tribute-to-talk.png")
   :wallet-welcome      (js-require/js-require "./resources/images/ui/wallet-welcome.png")
   :hardwallet-card     (js-require/js-require "./resources/images/ui/hardwallet-card.png")
   :secret-keys         (js-require/js-require "./resources/images/ui/secret-keys.png")
   :keycard-lock        (js-require/js-require "./resources/images/ui/keycard-lock.png")
   :keycard             (js-require/js-require "./resources/images/ui/keycard.png")
   :keycard-logo        (js-require/js-require "./resources/images/ui/keycard-logo.png")
   :keycard-phone       (js-require/js-require "./resources/images/ui/keycard-phone.png")
   :keycard-connection  (js-require/js-require "./resources/images/ui/keycard-connection.png")
   :keycard-nfc-on      (js-require/js-require "./resources/images/ui/keycard-nfc-on.png")
   :hold-card-animation (js-require/js-require "./resources/images/ui/hold-card-animation.gif")
   :warning-sign        (js-require/js-require "./resources/images/ui/warning-sign.png")
   :phone-nfc-on        (js-require/js-require "./resources/images/ui/phone-nfc-on.png")
   :phone-nfc-off       (js-require/js-require "./resources/images/ui/phone-nfc-off.png")
   :dapp-store          (js-require/js-require "./resources/images/ui/dapp-store.png")
   :ens-header          (js-require/js-require "./resources/images/ui/ens-header.png")
   :small-badge         (js-require/js-require "./resources/images/ui/small-badge.png")
   :large-badge         (js-require/js-require "./resources/images/ui/large-badge.png")})

(def loaded-images (atom {}))

(defn get-image [k]
  (if (contains? @loaded-images k)
    (get @loaded-images k)
    (get (swap! loaded-images assoc k
                ((get ui k))) k)))
