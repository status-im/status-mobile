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
  (if (string? number)
    (let [number-obj (awesome-phonenumber. number country-code "international")]
      (cond
        (not (.isValid number-obj)) [{:parameter "Phone number"
                                      :message   "Invalid phone number"}]
        (not (.isMobile number-obj)) [{:parameter "Phone number"
                                       :message   "Only mobile phone number is allowed"}]))
    [{:parameter "Phone number"
      :message   "Invalid phone number"}]))
