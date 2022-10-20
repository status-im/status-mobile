(ns quo2.components.icons.icons
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]))

(def icon-path "./resources/images/icons2/")

(defn require-icon [size path]
  (fn [el]
    (let [s (str "." path el ".png")
          k (-> el
                (cstr/replace "_" "-")
                (cstr/replace " " "-")
                (cstr/lower-case)
                (str size))]
      [k `(js/require ~s)])))

(defn get-files [path]
  (->> (io/file path)
       file-seq
       (filter #(cstr/ends-with? % "png"))
       (map #(first (cstr/split (.getName %) #"@")))
       distinct))

(defn get-icons [size]
  (let [path (str icon-path size "x" size "/")]
    (into {} (map (require-icon size path) (get-files path)))))

(defmacro resolve-icons []
  (merge
   (get-icons 12)
   (get-icons 16)
   (get-icons 20)
   (get-icons 24)))
