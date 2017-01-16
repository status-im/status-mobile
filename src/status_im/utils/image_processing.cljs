(ns status-im.utils.image-processing
  (:require [status-im.utils.fs :refer [read-file]]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(def resizer-class (js/require "react-native-image-resizer"))

(defn- resize [path max-width max-height on-resize on-error]
  (let [resize-fn (aget resizer-class "default" "createResizedImage")]
    (-> (resize-fn path max-width max-height "JPEG" 75 0 nil)
        (.then on-resize)
        (.catch on-error))))

(defn- image-base64-encode [path on-success on-error]
  (let [on-error   (fn [error]
                     (on-error :base64 error))]
    (read-file path "base64" on-success on-error)))

(defn img->base64 [path on-success on-error]
  (let [on-resized (fn [path]
                     (let [path (str/replace path "file:" "")]
                      (log/debug "Resized: " path)
                      (image-base64-encode path on-success on-error)))
        on-error   (fn [error]
                     (log/debug "Resized error: " error)
                     (on-error :resize error))]
    (resize path 150 150 on-resized on-error)))
