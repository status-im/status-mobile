(ns status-im.utils.email
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.react-native.js-dependencies :as dependencies]))

(re-frame/reg-fx
 :email/send
 ;; https://github.com/chirag04/react-native-mail#example
 (fn [[opts callback]]
   (.mail ^js (dependencies/react-native-mail)
          (clj->js opts)
          callback)))

(fx/defn send-email
  [_ opts callback]
  {:email/send [opts callback]})
