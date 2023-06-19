(ns status-im2.common.parallax.whitelist
  (:require [native-module.core :as native-module]
            [clojure.string :as string]))

(def ^:const device-id (:device-id (native-module/get-device-model-info)))

(def ^:const whitelist #{"iPhone11" "iPhone12" "iPhone13" "iPhone14"})

(def whitelisted?
  (let [device-type (first (string/split (str device-id) ","))]
    (whitelist device-type)))
