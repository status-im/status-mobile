(ns legacy.status-im.ui.components.icons.icons
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]))

(def icon-path "./resources/images/icons/")

(defn require-icon
  [el]
  (let [s (str "../resources/images/icons/" el ".png")
        k (string/replace el "_" "-")]
    [k `(js/require ~s)]))

(defmacro resolve-icons
  []
  (let [files (->> (io/file icon-path)
                   file-seq
                   (filter #(string/ends-with? % "png"))
                   (map #(first (string/split (.getName %) #"@")))
                   distinct)]
    (into {} (map require-icon files))))
