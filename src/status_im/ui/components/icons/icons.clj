(ns status-im.ui.components.icons.icons
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]))

(def icon-path "./resources/images/icons/")

(defn require-icon [el]
  (let [s (str "../resources/images/icons/" el ".png")
        k (cstr/replace el "_" "-")]
    [k `(js/require ~s)]))

(defmacro resolve-icons []
  (let [files (->> (io/file icon-path)
                   file-seq
                   (filter #(cstr/ends-with? % "png"))
                   (map #(first (cstr/split (.getName %) #"@")))
                   distinct)]
    (into {} (map require-icon files))))
