(ns env.utils
  (:require [clojure.string :as string]))

(defn get-host
  "Expects the input url to be in the form protocol://host:port
   Returns host or an empty string upon failure"
  [url]
  (->
   url
   (string/split #"/")
   (get 2 "")
   (string/split #":")
   (get 0 "")))

(defn re-frisk-url
  "Expects the input url to be in the form ws://host:port/figwheel-ws"
  [url]
  (let [host (get-host url)]
    (if (string/blank? host)
      (throw (js/Error. "Failed to parse figwheel url. re-frisk url cannot be blank"))
      (str host ":" 4567))))
