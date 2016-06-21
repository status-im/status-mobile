(ns status-im.utils.identicon
  (:require [clojure.string :as s]
            [status-im.utils.utils :as u]))

(def identicon-js (u/require "identicon.js"))

(defn identicon [hash options]
    (str "data:image/png;base64," (.toString (new identicon-js hash options))))

