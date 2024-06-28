(ns legacy.status-im.utils.image
  (:require
    [clojure.string :as string]))

(defn source
  [photo-path]
  (when-not (and (not (string/blank? photo-path))
                 (string/starts-with? photo-path "contacts://"))
    {:uri photo-path}))
