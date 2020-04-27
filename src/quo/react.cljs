(ns quo.react
  (:require [oops.core :refer [oget]]
            ["react" :as react]))

(def create-ref (oget react "createRef"))

(defn current-ref [ref]
  (oget ref "current"))
