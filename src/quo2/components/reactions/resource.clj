(ns quo2.components.reactions.resource
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(def ^:private reactions-dir "./resources/images/reactions/")

(defn- resolve-reaction
  [reaction]
  (let [path (str reactions-dir (name reaction) ".png")
        file (io/file path)]
    (when (.exists file)
      `(js/require ~(str "." path)))))

(defn- find-all-image-base-names
  []
  (let [dir (io/file reactions-dir)]
    (->> dir
         file-seq
         (filter #(string/ends-with? % "png"))
         (map #(.getName %))
         (map #(string/replace % #"\.png$" ""))
         (map #(first (string/split % #"@")))
         distinct)))

(defmacro resolve-all-reactions
  []
  (reduce (fn [acc reaction]
            (assoc acc reaction (resolve-reaction reaction)))
          {}
          (find-all-image-base-names)))
