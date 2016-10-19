(ns status-im.components.sync-state.offline
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                text
                                                animated-view
                                                linear-gradient
                                                get-dimensions]]
            [status-im.components.sync-state.styles :as st]
            [status-im.components.animation :as anim]
            [status-im.i18n :refer [label]]))

(def window-width (:width (get-dimensions "window")))

(defn start-offline-animation [offline-opacity]
  (anim/start
    (anim/timing offline-opacity {:toValue  1.0
                                  :duration 250})))

(defn offline-view [_]
  (let [sync-state      (subscribe [:get :sync-state])
        network-status  (subscribe [:get :network-status])
        offline-opacity (anim/create-value 0.0)
        on-update       (fn [_ _]
                          (anim/set-value offline-opacity 0)
                          (when (or (= @network-status :offline) (= @sync-state :offline))
                            (start-offline-animation offline-opacity)))]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [{:keys [top]}]
         (when (or (= @network-status :offline) (= @sync-state :offline))
           [animated-view {:style (st/offline-wrapper top offline-opacity window-width)}
            [view
             [text {:style st/offline-text}
              (label :t/offline)]]]))})))
