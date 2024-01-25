(ns quo.components.utilities.token.loader
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(def ^:private token-path "./resources/images/tokens/mainnet/")

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

(defn get-tokens
  []
  (let [files         (file-seq (io/file token-path))
        png-filenames (keep (fn [file]
                              (when (string/ends-with? file "png")
                                (clean-filename file)))
                            files)]
    (zipmap (map get-file-key png-filenames)
            (map get-js-require png-filenames))))

(defmacro resolve-tokens [] (get-tokens))
