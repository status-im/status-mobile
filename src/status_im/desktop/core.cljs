(ns status-im.desktop.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            ["react-native" :as rn]
            status-im.utils.db
            status-im.subs
            [status-im.ui.screens.views :as views]
            [status-im.ui.components.react :as react]
            [status-im.utils.snoopy :as snoopy]
            [status-im.utils.error-handler :as error-handler]
            [status-im.utils.logging.core :as utils.logs]
            [status-im.ui.screens.desktop.views :as desktop-views]
            [status-im.desktop.deep-links :as deep-links]
            [status-im.utils.config :as config]))

(def app-registry (.-AppRegistry rn))

(defn app-state-change-handler [state]
  (re-frame/dispatch [:app-state-change state]))

(defn app-root [_]
  (if config/mobile-ui-for-desktop?
    (reagent/create-class
     {:component-did-mount
      (fn [this]
        (.addEventListener ^js react/app-state "change" app-state-change-handler)
        (re-frame/dispatch [:set-initial-props (reagent/props this)]))
      :component-will-unmount
      (fn []
        (.removeEventListener ^js react/app-state "change" app-state-change-handler))
      :display-name "root"
      :reagent-render views/main})
    (reagent/create-class
     {:component-did-mount (fn [this]
                             (re-frame/dispatch [:set-initial-props (reagent/props this)])
                                        ;(shortcuts/register-default-shortcuts)
                             (deep-links/add-event-listener))
      :reagent-render      (fn [_]
                             desktop-views/main)})))

(defn init []
  (utils.logs/init-logs)
  (error-handler/register-exception-handler!)
  (re-frame/dispatch-sync [:init/app-started])
  (.registerComponent ^js app-registry "StatusIm" #(reagent/reactify-component app-root))
  (snoopy/subscribe!))
