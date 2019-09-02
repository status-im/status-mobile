(ns prepare
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

;;TODO copy dirs to have images
;(io/copy (io/file "../resources/images") (io/file "./resources/public/images"))

(let [fls (file-seq (java.io.File. "../android/app/src/main/res/drawable-mdpi"))]
  (spit "./resources/public/icons.edn"
    (pr-str
     (remove nil?
      (map
       (fn [fl]
         (let [n (first (str/split (.getName fl) #".png"))]
          (when (.isFile fl)
            (io/copy (io/file (.getPath fl)) (io/file (str "./resources/public/" (str/replace n #"_" "-"))))
            (str/replace n #"_" "-"))))
       fls)))))
