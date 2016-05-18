(ns syng-im.utils.sms-listener)

(def sms-listener (js/require "react-native-android-sms-listener"))

;; Only android is supported!

(defn add-sms-listener
   "Message format: {originatingAddress: string, body:
  string}. Returns cancelable subscription."
  [listen-fn]
  (.addListener sms-listener listen-fn))

(defn remove-sms-listener [subscription]
  (.remove subscription))
