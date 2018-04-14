(ns status-im.desktop.core
  (:require [reagent.core :as reagent]
            status-im.utils.db
            status-im.ui.screens.db
            status-im.ui.screens.events
            status-im.ui.screens.subs
            status-im.data-store.core
            [status-im.ui.screens.desktop.views :as views]
            [status-im.core :as core]
            [status-im.ui.components.react :as react]))

(defn app-root []
  (reagent/create-class
    {:reagent-render views/main}))

(defn init []
  (core/init app-root))
