(ns status-im.ui.components.sync-state.offline
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.sync-state.styles :as styles]
            [status-im.ui.components.animation :as animation]
            [status-im.i18n :as i18n]))

(def window-width (:width (react/get-dimensions "window")))

(defn start-offline-animation [offline-opacity]
  (animation/start
    (animation/timing offline-opacity {:toValue 1.0
                                  :duration     250})))

(defn offline-view [_]
  (let [sync-state       (re-frame/subscribe [:sync-state])
        network-status   (re-frame/subscribe [:get :network-status])
        offline-opacity  (animation/create-value 0.0)
        on-update        (fn [_ _]
                           (animation/set-value offline-opacity 0)
                           (when (or (= @network-status :offline) (= @sync-state :offline))
                             (start-offline-animation offline-opacity)))
        pending-contact? (re-frame/subscribe [:current-contact :pending?])
        view-id          (re-frame/subscribe [:get :view-id])]
    (reagent/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :display-name "offline-view"
       :reagent-render
       (fn [{:keys [top]}]
         (when (or (= @network-status :offline) (= @sync-state :offline))
           (let [pending? (and @pending-contact? (= :chat @view-id))]
             [react/animated-view {:style (styles/offline-wrapper top offline-opacity window-width pending?)}
              [react/view
               [react/text {:style styles/offline-text}
                (i18n/label :t/offline)]]])))})))
