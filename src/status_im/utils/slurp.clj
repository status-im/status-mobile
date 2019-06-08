(ns status-im.utils.slurp
  (:refer-clojure :exclude [slurp])
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.io File)))

(def prod? (= "prod" (System/getenv "BUILD_ENV")))

(defn copy-file [source-path dest-path]
  (io/copy (io/file source-path) (io/file dest-path)))

(def resources-dir "status-modules/resources/")

(defn check-resources-dir []
  (let [resources (File. resources-dir)]
    (when-not (.exists resources)
      (.mkdir resources))))

(defmacro slurp [file]
  (if prod?
    (let [name      (str/replace file #"[\/\.]" "_")
          file-name (str resources-dir name)]
      (check-resources-dir)
      (copy-file file (str file-name "-raw.js"))
      (let [res (gensym "res")]
        `(let [~res (atom nil)]
           (fn []
             (or @~res
                 (reset! ~res (js/require ~(str file-name ".js"))))))))
    `(fn []
       ~(clojure.core/slurp file))))
