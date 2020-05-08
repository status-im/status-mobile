(ns status-im.acquisition.install-referrer
  (:require [taoensso.timbre :as log]
            [clojure.string :as cstr]
            ["react-native-device-info" :refer [getInstallReferrer]]))

(defn- split-param [param]
  (->
   (cstr/split param #"=")
   (concat (repeat ""))
   (->>
    (take 2))))

(defn- url-decode
  [string]
  (some-> string str (cstr/replace #"\+" "%20") (js/decodeURIComponent)))

(defn- query->map
  [qstr]
  (when-not (cstr/blank? qstr)
    (some->> (cstr/split qstr #"&")
             seq
             (mapcat split-param)
             (map url-decode)
             (apply hash-map))))

(defn parse-referrer
  "Google return query params for referral with all utm tags"
  [referrer]
  (-> referrer query->map (get "referrer")))

(defn get-referrer [cb]
  (-> (getInstallReferrer)
      (.then cb)
      (.catch #(log/error "[install-referrer]" %))))
