(ns status-im.utils.gfycat.core
  (:require [native-module.core :as native-module]))

(def unknown-gfy "Unknown")

(defn- build-gfy
  [public-key]
  (case public-key
    nil unknown-gfy
    "0" unknown-gfy
    (native-module/generate-gfycat public-key)))

(def generate-gfy (memoize build-gfy))
