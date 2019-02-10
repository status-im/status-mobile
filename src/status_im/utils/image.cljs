(ns status-im.utils.image
  (:require [clojure.string :as string]
            [status-im.react-native.resources :as resources]))

(defn source [photo-path]
  (if (and (not (string/blank? photo-path))
           (string/starts-with? photo-path "contacts://"))
    (->> (string/replace photo-path #"contacts://" "")
         (keyword)
         (get resources/contacts))
    {:uri photo-path}))