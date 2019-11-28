(ns status-im.utils.gfycat.core
  (:require [status-im.native-module.core :as native-module]
            [re-frame.core :as re-frame]
            [status-im.utils.datetime :as datetime]))

(def unknown-gfy "Unknown")

(defn- build-gfy
  [public-key]
  (case public-key
    nil unknown-gfy
    "0" unknown-gfy
    (native-module/generate-gfycat public-key)))

(def generate-gfy (memoize build-gfy))

(re-frame/reg-fx
 :insert-gfycats
 (fn [key-path-seq]
   (for [key-path key-path-seq]
     (let [public-key (first key-path)
           path-for-gfycat (second key-path)]
       (native-module/generate-gfycat-async public-key #(re-frame/dispatch [:gfycat-generated path-for-gfycat %]))))))
