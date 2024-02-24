(ns status-im.common.parallax.blacklist
  (:require
    [native-module.core :as native-module]))

(def ^:private device-id (:device-id (native-module/get-device-model-info)))

(defn- get-model-code
  [model]
  (let [[_ code] (re-find #"iPhone(\d+)" model)]
    (if code
      (js/parseInt code)
      0)))

(def ^:private minimum-device-code 11)

(def blacklisted?
  (-> device-id
      get-model-code
      (< minimum-device-code)))
