(ns legacy.status-im.utils.build
  (:require
    [clojure.java.io :as io]
    [clojure.java.shell :as shell]
    [clojure.string :as string]))

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
