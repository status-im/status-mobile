(ns quo.react
  (:require [oops.core :refer [oget]]
            [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :refer [react]]))

;; NOTE(Ferossgp): Available in new versions of reagent as `:<>`
(def fragment (reagent/adapt-react-class (oget react "Fragment")))
