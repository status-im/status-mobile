(ns utils.promesa
  (:require [promesa.core :as promesa]))

(defmacro all-seq
  "A drop-in replacement for promesa/all which runs the promises in
  sequence, instead of running them in parallel."
  [bindings]
  `(promesa/loop [results#   []
                  remaining# ~bindings]
     (if (empty? remaining#)
       (promesa/resolved results#)
       (promesa/let [result# (first remaining#)
                     value#  (promesa/resolved result#)]
         (promesa/recur (conj results# value#) (rest remaining#))))))

