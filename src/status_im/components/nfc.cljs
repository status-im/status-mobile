(ns status-im.components.nfc
  (:require [cljs.spec.alpha :as s]
            [status-im.utils.platform :as platform]))


(def class
  (when platform/android?
    (js/require "nfc-react-native")))

(def android-only-error "NFC API is available only on Android")

(defn get-card-id [on-success on-error]
  (if platform/android?
    (-> (.getCardId class)
        (.then on-success)
        (.catch on-error))
    (on-error android-only-error)))

(defn read-tag [sectors on-success on-error]
  (if platform/android?
    (-> (.readTag class (clj->js sectors))
        (.then on-success)
        (.catch on-error))
    (on-error android-only-error)))

(defn write-tag [sectors card-id on-success on-error]
  (if platform/android?
    (-> (.writeTag class (clj->js sectors) card-id)
        (.then on-success)
        (.catch on-error))
    (on-error android-only-error)))
