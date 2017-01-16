(ns status-im.utils.sms-listener
  (:require [status-im.utils.platform :refer [android?]]))

(def sms-listener (.-default (js/require "react-native-android-sms-listener")))

;; Only android is supported!

(defn add-sms-listener
  "Message format: {:originatingAddress string, :body string}. Returns
  cancelable subscription."
  [listen-fn]
  (when android?
    (.addListener sms-listener
                  (fn [message]
                    (listen-fn (js->clj message :keywordize-keys true))))))

(defn remove-sms-listener [subscription]
  (when android?
    (.remove subscription)))
