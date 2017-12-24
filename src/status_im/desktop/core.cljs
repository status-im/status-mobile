(ns status-im.desktop.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            status-im.utils.db
            status-im.ui.screens.db
            status-im.ui.screens.events
            status-im.ui.screens.subs
            status-im.data-store.core
            [status-im.ui.screens.views :as views]
            [status-im.ui.components.react :as react]
            [status-im.core :as core]))

(defn app-root []

  (reagent/create-class
    {
     :component-did-mount (fn [] ())
     :display-name "root"
     :reagent-render views/main}))

(defn init []
  (enable-console-print!)
  (core/init app-root))
