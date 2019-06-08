(ns status-im.utils.slurp
  (:refer-clojure :exclude [slurp])
  (:require [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]
            [clojure.string :as str])
  (:import (java.io PushbackReader)
           (java.io File)))

(def params
  (with-open [r (io/reader "params.edn")]
    (edn/read (PushbackReader. r))))

(defn copy-file [source-path dest-path]
  (io/copy (io/file source-path) (io/file dest-path)))

(def resources-dir "status-modules/resources/")

(defn check-resources-dir []
  (let [resources (File. resources-dir)]
    (when-not (.exists resources)
      (.mkdir resources))))

(defmacro slurp [file]
  (if (= (:env params) :dev)
    `(fn []
       ~(clojure.core/slurp file))
    (let [name      (str/replace file #"[\/\.]" "_")
          file-name (str resources-dir name)]
      (check-resources-dir)
      (copy-file file (str file-name "-raw.js"))
      (let [res (gensym "res")]
        `(let [~res (atom nil)]
           (fn []
             (or @~res
                 (reset! ~res (js/require ~(str file-name ".js"))))))))))
