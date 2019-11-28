(ns status-im.hardwallet.nfc)

(def is-nfc-supported? (atom nil))

(defn set-nfc-supported? [supported?]
  (reset! is-nfc-supported? supported?))

(defn nfc-supported? []
  @is-nfc-supported?)
