(ns status-im.utils.image-processing
  (:require [status-im.utils.fs :refer [read-file]]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn- resize [path max-width max-height on-resize on-error]
  (let [resize-fn (aget rn-dependencies/image-resizer "default" "createResizedImage")]
    (-> (resize-fn path max-width max-height "JPEG" 75 0 nil)
        (.then on-resize)
        (.catch on-error))))

(defn- image-base64-encode [path on-success on-error]
  (let [on-error   (fn [error]
                     (on-error :base64 error))]
    (read-file path "base64" on-success on-error)))

(defn img->base64 [path on-success on-error]
  (let [on-resized (fn [image]
                     (let [path (aget image "path")]
                       (log/debug "Resized: " path)
                       (image-base64-encode path on-success on-error)))
        on-error   (fn [error]
                     (log/debug "Resized error: " error)
                     (on-error :resize error))]
    (resize path 150 150 on-resized on-error)))
