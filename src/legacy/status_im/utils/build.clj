(ns legacy.status-im.utils.build
  (:require
    [cljs.analyzer :as analyzer]
    [clojure.java.io :as io]
    [clojure.java.shell :as shell]
    [clojure.string :as string]))

;; Some warnings are unavoidable due to dependencies. For example, reagent 0.6.0
;; has a warning in its util.cljs namespace. Adjust this as is necessary and
;; unavoidable warnings arise.
(def acceptable-warning?
  #{"Protocol IFn implements method -invoke with variadic signature (&)"}) ;; reagent 0.6.0 reagent/impl/util.cljs:61

(defn nil-acceptable-warning
  [s]
  (when-not (acceptable-warning? s)
    s))

(defn warning-handler
  [warning-type env extra]
  (when (warning-type analyzer/*cljs-warnings*)
    (when-let [s (nil-acceptable-warning (analyzer/error-message warning-type extra))]
      (binding [*out* *err*]
        (println (analyzer/message env (str "\u001B[31mWARNING\u001B[0m: " s))))
      (System/exit 1))))

(defmacro get-build-no
  []
  (-> (shell/sh "bash" "./scripts/version/build_no.sh")
      :out
      (string/replace "\n" "")))

(defmacro get-current-sha
  "fetches the latest commit sha from the current branch"
  []
  (-> (shell/sh "git" "rev-parse" "HEAD")
      :out
      (string/replace "\n" "")))

(defmacro git-short-version
  []
  (let [version-file-path "VERSION"
        version-file      (io/file version-file-path)]
    (if (.exists version-file)
      (string/trim (slurp version-file-path))
      (-> (shell/sh "git" "rev-parse" "--short" "HEAD")
          :out
          (string/replace "\n" "")))))
