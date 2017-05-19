(ns status-im.utils.instabug
  (:require [taoensso.timbre :as log]))

(def instabug-rn (js/require "instabug-reactnative"))

(defn log [str]
  (if js/goog.DEBUG
    (log/debug str)
    (let [a nil]
      (.IBGLog a #_instabug-rn str))))
