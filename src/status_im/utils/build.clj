(ns status-im.utils.build
  (:require [cljs.analyzer :as analyzer]
            [clojure.string :as string]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io]))

;; Some warnings are unavoidable due to dependencies. For example, reagent 0.6.0
;; has a warning in its util.cljs namespace. Adjust this as is necessary and
;; unavoidable warnings arise.
(def acceptable-warning?
  #{"Protocol IFn implements method -invoke with variadic signature (&)"}) ;; reagent 0.6.0 reagent/impl/util.cljs:61

(defn nil-acceptable-warning [s]
  (when-not (acceptable-warning? s)
    s))

(defn warning-handler [warning-type env extra]
  (when (warning-type analyzer/*cljs-warnings*)
    (when-let [s (nil-acceptable-warning (analyzer/error-message warning-type extra))]
      (binding [*out* *err*]
        (println (analyzer/message env (str "\u001B[31mWARNING\u001B[0m: " s))))
      (System/exit 1))))

(defmacro git-short-version []
  (let [version-file-path ".version"
        version-file (io/file version-file-path)]
    (if (.exists version-file)
      (string/trim (slurp version-file-path))
      (string/replace (:out (shell/sh "bash" "-c" "git describe --always")) "\n" ""))))
