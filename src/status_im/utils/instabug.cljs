(ns status-im.utils.instabug
  (:require [taoensso.timbre :as log]))

(def instabug-rn (js/require "instabug-reactnative"))

(defn log [str]
  (if js/goog.DEBUG
    (log/debug str)
    (let [a nil]
      (.IBGLog instabug-rn str))))

(defn instabug-appender []
  {:enabled?   true
   :async?     false
   :min-level  nil
   :rate-limit nil
   :output-fn  :inherit

   :fn
               (fn [data]
                 (let [{:keys [level ?ns-str ?err output_]} data]
                   (log (force output_))))})

(log/merge-config! {:appenders {:instabug (instabug-appender)
                                ;; useful for local debug
                                ;; :print    (log/println-appender)
                                }})
