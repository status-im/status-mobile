(ns status-im.ui.components.connectivity.view
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.connectivity.styles :as styles]
            [status-im.ui.components.animation :as animation]
            [status-im.i18n :as i18n]))

(def window-width (:width (react/get-dimensions "window")))

(defn start-error-animation [offline-opacity]
  (animation/start
   (animation/timing offline-opacity {:toValue  1.0
                                      :duration 250})))

(defn error-view [_]
  (let [offline?             (re-frame/subscribe [:offline?])
        connection-problem?  (re-frame/subscribe [:connection-problem?])
        offline-opacity      (animation/create-value 0.0)
        on-update            (fn [_ _]
                               (animation/set-value offline-opacity 0)
                               (when (or @offline? @connection-problem?)
                                 (start-error-animation offline-opacity)))
        current-chat-contact (re-frame/subscribe [:get-current-chat-contact])
        view-id              (re-frame/subscribe [:get :view-id])]
    (reagent/create-class
     {:component-did-mount
      on-update
      :component-did-update
      on-update
      :display-name "connectivity-error-view"
      :reagent-render
      (fn [{:keys [top]}]
        (when (or @offline? @connection-problem?)
          (let [pending? (and (:pending @current-chat-contact) (= :chat @view-id))]
            [react/animated-view {:style (styles/offline-wrapper top offline-opacity window-width pending?)}
             [react/view
              [react/text {:style styles/offline-text}
               (i18n/label (if @connection-problem?
                             :t/connection-problem
                             :t/offline))]]])))})))
