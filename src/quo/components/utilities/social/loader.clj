(ns quo.components.utilities.social.loader
  (:require [clojure.core]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn- social-path
  [size type]
  (println (str "./resources/images/socials/" size "/" type))
  (str "./resources/images/socials/" size "/" type))

(defn- clean-filename
  "Return a (string) filename without the .png extension and @<number>x suffix."
  [file]
  (first (string/split (str file) #"@|\.png")))

(defn- get-js-require
  [filename]
  (let [require-path (str "." filename ".png")]
    `(js/require ~require-path)))

(defn- get-file-key
  [filename]
  (-> filename
      (string/split #"\/")
      (peek)
      (string/lower-case)))

(defn get-socials
  [size type]
  (println "here" size type)
  (let [files         (file-seq (io/file (social-path size type)))
        png-filenames (keep (fn [file]
                              (when (string/ends-with? file "png")
                                (clean-filename file)))
                            files)]
    (zipmap (map get-file-key png-filenames)
            (map get-js-require png-filenames))))

(defmacro resolve-socials 
  [size type] 
  (println "rere" size type)
  (get-socials size type))
