(ns status-im.common.parallax.blacklist
  (:require
    [native-module.core :as native-module]))

(def ^:private device-id (:device-id (native-module/get-device-model-info)))

(defn- get-model-code
  [model]
  (if-let [[_ code] (and model (re-find #"iPhone(\d+)" model))]
    (js/parseInt code 10)
    0))

(def ^:private minimum-device-code 11)

(def blacklisted?
  (-> device-id
      get-model-code
      (< minimum-device-code)))
