(ns status-im.utils.identicon
  (:require [status-im.js-dependencies :as dependencies]))

(def default-size 150)

(defn identicon
  ([hash] (identicon hash (clj->js {:background [255 255 255 255]
                                    :margin     0.24
                                    :size       default-size})))
  ([hash options]
   (str "data:image/png;base64,"
        (let [identicon-js dependencies/identicon-js]
          (str (new identicon-js hash options))))))
