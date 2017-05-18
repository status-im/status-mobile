(ns status-im.utils.instabug
  (:require [taoensso.timbre :as log]))

(def instabug-rn (js/require "instabug-reactnative"))

(defn init-instabug []
  (when-not js/goog.DEBUG
    (.startWithToken instabug-rn
                     "b239f82a9cb00464e4c72cc703e6821e"
                     (.. instabug-rn -invocationEvent -shake))))

(defn log [str]
  (if js/goog.DEBUG
    (log/debug str)
    (.IBGLog instabug-rn str)))
