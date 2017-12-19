(ns status-im.ios.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.ui.screens.views :as views]
            [status-im.ui.components.react :as react]
            [status-im.core :as core]))

(defn orientation->keyword [o]
  (keyword (.toLowerCase o)))

(defn app-root []

  (reagent/create-class
    {
     :component-did-mount (fn [] ())
     :display-name "root"
     :reagent-render views/main}))

(defn init []
  (core/init app-root))
