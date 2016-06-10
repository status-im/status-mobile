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
    (let [number-obj (awesome-phonenumber. number country-code "international")]
      (and (.isValid number-obj)
           (.isMobile number-obj)))))
