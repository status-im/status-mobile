(ns quo.react
  (:require [oops.core :refer [oget]]
            [reagent.core :as reagent]
            ["react" :as react]))

;; NOTE(Ferossgp): Available in new versions of reagent as `:<>`
(def fragment (reagent/adapt-react-class (oget react "Fragment")))
