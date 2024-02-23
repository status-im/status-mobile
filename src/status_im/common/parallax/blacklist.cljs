(ns status-im.common.parallax.blacklist
  (:require
    [native-module.core :as native-module]))

(def ^:const device-id (:device-id (native-module/get-device-model-info)))

(defn get-model-code
  [model]
  (let [match (re-find #"iPhone(\d+)" model)]
    (if match
      (js/parseInt (second match))
      0)))

(def ^:const minimum-device-code 11)

(def blacklisted?
  (-> device-id
      get-model-code
      (< minimum-device-code)))
