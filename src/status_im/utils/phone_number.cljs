(ns status-im.utils.phone-number
  (:require [status-im.i18n :refer [label]]))

(def i18n (js/require "react-native-i18n"))
(def locale (or (.-locale i18n) "___en"))
(def country-code (subs locale 3 5))
(def awesome-phonenumber (js/require "awesome-phonenumber"))

;; todo check wrong numbers, .getNumber returns empty string
(defn format-phone-number [number]
  (str (.getNumber (awesome-phonenumber. number country-code) "international")))

(defn get-examples []
  (when-let [example (.getExample awesome-phonenumber country-code "mobile")]
    [{:number      (.getNumber example)
      :description (label :t/phone-e164)}
     {:number      (.getNumber example "international")
      :description (label :t/phone-international)}
     {:number      (.getNumber example "national")
      :description (label :t/phone-national)}
     {:number      (.getNumber example "significant")
      :description (label :t/phone-significant)}]))

(defn valid-mobile-number? [number]
  (when (string? number)
    (let [{:keys [valid type]} (-> (awesome-phonenumber. number country-code)
                                   (.toJSON)
                                   (js->clj :keywordize-keys true))]
      (and valid
           (some #{(keyword type)} '(:mobile :fixed-line-or-mobile :pager))))))
