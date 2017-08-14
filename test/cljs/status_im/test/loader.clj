(ns ^{:doc "todo"
      :author "Goran Jovic"}
  status-im.test.loader
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(defn path->ns [path]
  (-> path
      (string/split #"/test/cljs/")
      second
      (string/replace "_" "-")
      (string/replace "/" ".")
      (string/replace ".cljs" "")))

(defn ns->req [ns-str]
  `(quote ~(symbol ns-str)))

(defmacro require-test-namespaces [root]
  (cons 'require
        (->> root
             io/file
             file-seq
             (filter #(and (.exists %)(.isFile %)))
             (map #(.getPath %))
             (filter #(not (string/includes? % "protocol"))) ; a temporary hack
             (filter #(not (string/includes? % "contacts"))) ; a temporary hack
             (filter #(not (string/includes? % "runner")))
             (filter #(string/ends-with? % "cljs"))
             (map path->ns)
             (map ns->req))))

