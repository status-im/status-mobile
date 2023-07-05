(ns quo2.components.icons.icons
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(def ^:private icon-path "./resources/images/icons2/")

(defn- require-icon
  [size path]
  (fn [el]
    (let [s (str "." path el "@2x.png") ; FIX!
          k (-> el
                (string/replace "_" "-")
                (string/replace " " "-")
                (string/lower-case)
                (str size))]
      [k `(js/require ~s)])))

(defn- get-files
  [path]
  (->> (io/file path)
       file-seq
       (filter #(string/ends-with? % "png"))
       (map #(first (string/split (.getName %) #"@")))
       distinct))

(defn- get-icons
  [size]
  (let [path (str icon-path size "x" size "/")]
    (into {} (map (require-icon size path) (get-files path)))))

(defmacro resolve-icons
  []
  (merge
   (get-icons 12)
   (get-icons 16)
   (get-icons 20)
   (get-icons 24)
   (get-icons 32)))
