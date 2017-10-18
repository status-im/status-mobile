(ns status-im.utils.sms-listener
  (:require [re-frame.core :as re-frame]
            [status-im.utils.platform :refer [android?]]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

;; Only android is supported!
(defn add-sms-listener
  "Message format: {:originatingAddress string, :body string}. Returns
  cancelable subscription."
  [listen-event-creator]
  (when android?
    (.addListener rn-dependencies/android-sms-listener
                  (fn [message]
                    (re-frame/dispatch (listen-event-creator (js->clj message :keywordize-keys true)))))))

(defn remove-sms-listener [subscription]
  (when android?
    (.remove subscription)))
