(ns status-im.desktop.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            status-im.utils.db
            status-im.ui.screens.db
            status-im.ui.screens.events
            status-im.ui.screens.subs
            status-im.data-store.core
            [reagent.impl.component :as reagent.component]
            [status-im.ui.screens.desktop.views :as views]
            [status-im.core :as core]
            [status-im.desktop.deep-links :as deep-links]))

(defn app-root [props]
  (reagent/create-class
   {:component-did-mount (fn [this]
                           (re-frame/dispatch [:set-initial-props (reagent/props this)])
                           (deep-links/add-event-listener))
    :reagent-render      (fn [props]
                           views/main)}))

(defn init []
  (core/init app-root))
