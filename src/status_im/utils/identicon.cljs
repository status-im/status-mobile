(ns status-im.utils.identicon
  (:require [status-im.js-dependencies :as dependencies]))

(def default-size 40)

(defn identicon
  ([hash] (identicon hash (clj->js {:background [255 255 255]
                                    :saturation 0.5
                                    :brightness 0.7
                                    :margin     0.24
                                    :size       default-size})))
  ([hash options]
   (when hash
     (try
       (str "data:image/png;base64,"
            (let [identicon-js (dependencies/identicon-js)]
              (str (new identicon-js hash options))))
       ;; the identicon-js lib requires a hash with a length of at least 14
       ;; since we only run this when there is a hash, it only happens with
       ;; dummy values during testing
       (catch :default e)))))
