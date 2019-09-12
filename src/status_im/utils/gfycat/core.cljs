(ns status-im.utils.gfycat.core
  (:require [status-im.native-module.core :as native-module]
            [status-im.utils.datetime :as datetime]))

(def unknown-gfy "Unknown")

(defn- build-gfy
  [public-key]
  (case public-key
    nil unknown-gfy
    "0" unknown-gfy
    (native-module/generate-gfycat public-key)))

(def generate-gfy (memoize build-gfy))
