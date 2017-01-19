(ns status-im.utils.instabug)

(def instabug-rn (js/require "instabug-reactnative"))

(defn log [str] (.IBGLog instabug-rn str))
