(ns status-im.utils.phone-number
  (:require [status-im.utils.utils :as u]))

(def i18n (u/require "react-native-i18n"))
(def locale (or (.-locale i18n) "___en"))
(def country-code (subs locale 3 5))
(def awesome-phonenumber (u/require "awesome-phonenumber"))

;; todo check wrong numbers, .getNumber returns empty string
(defn format-phone-number [number]
  (str (.getNumber (awesome-phonenumber. number country-code "international"))))

(defn valid-mobile-number? [number]
  (when (string? number)
    (let [pattern    #"^\s*(?:\+?(\d{1,3}))?[-. (]*(\d{3})[-. )]*(\d{3})[-. ]*(\d{2})[-. ]*(\d{2})\s*$"
          number-obj (awesome-phonenumber. number country-code "international")]
      (and (re-matches pattern number)
           (.isValid number-obj)
           (.isMobile number-obj)))))
