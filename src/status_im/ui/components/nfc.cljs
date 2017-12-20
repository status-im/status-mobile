(ns status-im.ui.components.nfc
  (:require [cljs.spec.alpha :as s]
            [status-im.utils.platform :as platform]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(def android-only-error "NFC API is available only on Android")

(defn get-card-id [on-success on-error]
  (if platform/android?
    (-> (.getCardId rn-dependencies/nfc)
        (.then on-success)
        (.catch on-error))
    (on-error android-only-error)))

(defn read-tag [sectors on-success on-error]
  (if platform/android?
    (-> (.readTag rn-dependencies/nfc (clj->js sectors))
        (.then on-success)
        (.catch on-error))
    (on-error android-only-error)))

(defn write-tag [sectors card-id on-success on-error]
  (if platform/android?
    (-> (.writeTag rn-dependencies/nfc (clj->js sectors) card-id)
        (.then on-success)
        (.catch on-error))
    (on-error android-only-error)))
