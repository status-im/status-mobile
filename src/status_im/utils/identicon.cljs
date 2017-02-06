(ns status-im.utils.identicon)

(def default-size 40)

(def identicon-js (js/require "identicon.js"))

(defn identicon
  ([hash] (identicon hash default-size))
  ([hash options]
    (str "data:image/png;base64," (str (new identicon-js hash options)))))

