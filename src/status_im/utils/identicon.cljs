(ns status-im.utils.identicon
  (:require [status-im.js-dependencies :as dependencies]))

(def default-size 40)

(defn identicon
  ([hash] (identicon hash (clj->js {:background [255 255 255 255]
                                    :margin     0.24
                                    :size       default-size})))
  ([hash options]
   (str "data:image/png;base64,"
        (str (new (dependencies/identicon-js) hash options)))))
