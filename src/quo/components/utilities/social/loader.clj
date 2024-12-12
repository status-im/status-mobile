(ns quo.components.utilities.social.loader
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(def ^:private icon-path "./resources/images/socials/")

(defn- require-icon
  [size path social-type]
  (fn [el]
    (let [s (str "." path el ".png")
          k (-> el
                (string/replace "_" "-")
                (string/replace " " "-")
                (string/lower-case)
                (str size)
                (str social-type))]
      [k `(js/require ~s)])))

(defn- get-files
  [path]
  (->> (io/file path)
       file-seq
       (filter #(string/ends-with? % "png"))
       (map #(first (string/split (.getName %) #"@")))
       distinct))

(defn- get-socials
  [size social-type]
  (let [path (str icon-path size "/" social-type "/")]
    (into {} (map (require-icon size path social-type) (get-files path)))))


(defmacro resolve-socials
  []
  (merge
   (get-socials "default" "default")
   (get-socials "bigger" "default")
   (get-socials "default" "solid")
   (get-socials "bigger" "solid")))
