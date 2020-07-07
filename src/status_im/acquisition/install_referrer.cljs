(ns status-im.acquisition.install-referrer
  (:require [taoensso.timbre :as log]
            ["react-native-device-info" :refer [getInstallReferrer]]
            [status-im.utils.http :as http]))

(defn parse-referrer
  "Google return query params for referral with all utm tags"
  [referrer]
  (-> referrer http/query->map (get "referrer")))

(defn get-referrer [cb]
  (-> (getInstallReferrer)
      (.then cb)
      (.catch #(log/error "[install-referrer]" %))))
