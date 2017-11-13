(ns status-im.ui.components.sync-state.gradient
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.ui.components.react :refer [view
                                                text
                                                animated-view
                                                linear-gradient
                                                get-dimensions]]
            [status-im.ui.components.sync-state.styles :as st]
            [status-im.ui.components.animation :as anim]
            [taoensso.timbre :as log]))

(def gradient-animation-duration 700)
(def synced-disappear-delay 2500)
(def gradient-width 250)
(def in-progress-animation-delay 1500)

(def window-width (:width (get-dimensions "window")))

(declare start-gradient-reverse-animation)

(defn- start-gradient-animation [{:keys [gradient-position sync-state] :as context}]
  (when (= @sync-state :in-progress)
    (anim/start
      (anim/timing gradient-position
                   {:toValue  (- window-width (/ gradient-width 3))
                    :duration gradient-animation-duration})
      (fn [_]
        (start-gradient-reverse-animation context)))))

(defn- start-gradient-reverse-animation [{:keys [gradient-position sync-state] :as context}]
  (when (= @sync-state :in-progress)
    (anim/start
      (anim/timing gradient-position
                   {:toValue  (- 0 (* 2 (/ gradient-width 3)))
                    :duration gradient-animation-duration})
      (fn [_]
        (start-gradient-animation context)))))

(defn- start-synced-animation [{:keys [sync-state-opacity in-progress-opacity synced-opacity]}]
  (anim/start
    (anim/timing in-progress-opacity {:toValue  0.0
                                      :duration 250}))
  (anim/start
    (anim/timing synced-opacity {:toValue  1.0
                                 :duration 250})
    (fn [_]
      (anim/start
        (anim/timing sync-state-opacity {:toValue  0.0
                                         :duration 250
                                         :delay    synced-disappear-delay})
        (fn [_]
          (dispatch [:set :sync-state :done]))))))

(defn start-in-progress-animation [component]
  (r/set-state component
               {:pending?  true
                :animation (js/setTimeout
                             (fn []
                               (dispatch [:set :sync-state :in-progress])
                               (r/set-state component {:pending? false}))
                             in-progress-animation-delay)}))

(defn start-offline-animation [{:keys [sync-state-opacity]}]
  (anim/start
    (anim/timing sync-state-opacity {:toValue  0.0
                                     :duration 250})))

(defn clear-pending-animation [component]
  (let [{:keys [pending? animation]} (r/state component)]
    (when pending?
      (r/set-state component {:pending? false})
      (js/clearTimeout animation))))


(defn sync-state-gradient-view []
  (let [sync-state          (subscribe [:sync-state])
        gradient-position   (anim/create-value 0)
        sync-state-opacity  (anim/create-value 0.0)
        in-progress-opacity (anim/create-value 0.0)
        synced-opacity      (anim/create-value 0.0)

        context             {:sync-state          sync-state
                             :gradient-position   gradient-position

                             :sync-state-opacity  sync-state-opacity
                             :in-progress-opacity in-progress-opacity
                             :synced-opacity      synced-opacity}
        on-update           (fn [component _]
                              (case @sync-state
                                :pending (start-in-progress-animation component)
                                :in-progress (do
                                               (anim/set-value gradient-position 0)
                                               (anim/set-value sync-state-opacity 1)
                                               (anim/set-value in-progress-opacity 1)
                                               (anim/set-value synced-opacity 0)
                                               (start-gradient-animation context))
                                :synced (start-synced-animation context)
                                :done (clear-pending-animation component)
                                :offline (do (clear-pending-animation component)
                                             (start-offline-animation context))
                                (log/debug "Sync state:" @sync-state)))]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :display-name "sync-state-gradient-view"
       :reagent-render
       (fn []
         [view st/sync-style-gradient
          [animated-view {:style (st/loading-wrapper sync-state-opacity)}
           [animated-view {:style (st/gradient-wrapper in-progress-opacity gradient-position)}
            [linear-gradient {:colors    ["#89b1fe" "#8b5fe4" "#8b5fe4" "#89b1fe"]
                              :start     {:x 0 :y 1}
                              :end       {:x 1 :y 1}
                              :locations [0 0.3 0.7 1]
                              :style     (st/gradient gradient-width)}]]
           (when (not= @sync-state :in-progress)
             [animated-view {:style (st/synced-wrapper synced-opacity window-width)}])]])})))
