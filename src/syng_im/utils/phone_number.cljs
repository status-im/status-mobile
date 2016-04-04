(ns syng-im.utils.phone-number)

(def i18n (js/require "react-native-i18n"))
(def locale (.-locale i18n))
(def country-code (subs locale 3 5))
(set! js/PhoneNumber (js/require "awesome-phonenumber"))

(defn format-phone-number [number]
  (str (.getNumber (js/PhoneNumber. number country-code "international"))))
