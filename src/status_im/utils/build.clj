(ns status-im.utils.build
  (:require [cljs.analyzer :as analyzer]))

(defn warning-handler [warning-type env extra]
  (when (warning-type analyzer/*cljs-warnings*)
    (when-let [s (analyzer/error-message warning-type extra)]
      (binding [*out* *err*]
        (println (analyzer/message env (str "\u001B[31mWARNING\u001B[0m: " s))))
      ;; TODO Do not enable yet as our current reagent version generates warnings
      #_(System/exit 1))))