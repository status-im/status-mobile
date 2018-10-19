(ns status-im.i18n
  (:require [clojure.string :as string]))

(defn read-file [f]
  (when (.isFile f)
    (let [locale  (-> f
                      .getName
                      (string/split #"\.")
                      first)
          content (slurp f)]
      [(keyword locale) content])))

(defn read-translations []
  (->>
   (java.io.File. "translations")
   file-seq
   (keep read-file)
   vec))

(defmacro translations [languages]
  (select-keys (into {} (read-translations)) languages))
