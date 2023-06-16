(ns status-im2.common.parallax.whitelist
  (:require [native-module.core :as native-module]
            [clojure.string :as string]))

(def device-id (:device-id (native-module/get-device-model-info)))

(def whitelist ["iPhone11" "iPhone12" "iPhone13" "iPhone14"])

(defn whitelisted?
  []
  (let [device-type (first (string/split (str device-id) ","))]
    (some (fn [a] (= a device-type)) whitelist)))
