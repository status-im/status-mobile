(ns status-im2.common.parallax.whitelist
  (:require [native-module.core :as native-module]
            [clojure.string :as string]))

(def ^:const device-id (:device-id (native-module/get-device-model-info)))

;; iPhone 14 is 15 for some reason
(def ^:const whitelist #{"iPhone11" "iPhone12" "iPhone13" "iPhone14" "iPhone15"})

(def whitelisted?
  (let [device-type (first (string/split (str device-id) ","))]
    (whitelist device-type)))
