(ns legacy.status-im.ui.components.connectivity.view
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.animation :as animation]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views :refer [defview letsubs]]))

(defn easing
  [direction n]
  {:toValue         n
   :easing          ((if (= :in direction)
                       (animation/easing-in)
                       (animation/easing-out))
                     (.-quad ^js animation/easing))
   :duration        400
   :useNativeDriver true})

(defn animated-bar-style
  [margin-value width color]
  {:position         :absolute
   :width            width
   :transform        [{:translateX
                       (animation/interpolate
                        margin-value
                        {:inputRange  [0 1]
                         :outputRange [0 width]})}]
   :height           3
   :background-color color})

(views/defview loading-indicator-anim
  [parent-width]
  (views/letsubs [blue-bar-left-margin  (animation/create-value 0)
                  white-bar-left-margin (animation/create-value 0)]
    {:component-did-mount
     (fn [_]
       (animation/start
        (animation/anim-loop
         (animation/anim-sequence
          [(animation/parallel
            [(animation/timing blue-bar-left-margin (easing :in 0.19))
             (animation/timing white-bar-left-margin (easing :in 0.65))])
           (animation/parallel
            [(animation/timing blue-bar-left-margin (easing :out 0.85))
             (animation/timing white-bar-left-margin (easing :out 0.85))])
           (animation/parallel
            [(animation/timing blue-bar-left-margin (easing :in 0.19))
             (animation/timing white-bar-left-margin (easing :in 0.65))])
           (animation/parallel
            [(animation/timing blue-bar-left-margin (easing :out 0))
             (animation/timing white-bar-left-margin (easing :out 0))])]))))}
    [react/view
     [react/view
      {:style               {:width            parent-width
                             :position         :absolute
                             :top              -3
                             :z-index          3
                             :height           3
                             :background-color colors/white}
       :accessibility-label :loading-indicator}
      [react/animated-view
       {:style (animated-bar-style blue-bar-left-margin
                                   parent-width
                                   colors/blue)}]
      [react/animated-view
       {:style (assoc (animated-bar-style white-bar-left-margin
                                          parent-width
                                          colors/white)
                      :left
                      (* 0.15 parent-width))}]]]))

(defview loading-indicator
  []
  (letsubs [fetching?    [:mailserver/fetching?]
            window-width [:dimensions/window-width]]
    (when fetching?
      [loading-indicator-anim window-width])))

(defn hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (re-frame/dispatch event))

(defview connectivity-sheet
  []
  (letsubs [{:keys [peers node mobile sync]}     [:connectivity/state]
            current-mailserver-name              [:mailserver/current-name]
            peers-count                          [:peers-count]
            {:keys [syncing-on-mobile-network?]} [:profile/profile]]
    [:<>
     [quo/header {:title (i18n/label :t/connection-status) :border-bottom false}]
     [quo/list-header (i18n/label :t/peer-to-peer)]
     (if (= peers :offline)
       [list.item/list-item
        {:title               (i18n/label :t/not-connected-to-peers)
         :accessibility-label "not-connected-to-peers"
         :subtitle            (i18n/label :t/unable-to-send-messages)
         :subtitle-max-lines  2
         :theme               :negative
         :icon                :main-icons/network}]
       [list.item/list-item
        {:title               (str (i18n/label :t/connected-to)
                                   " " peers-count
                                   " " (string/lower-case (i18n/label :t/peers)))
         :accessibility-label "connected-to-n-peers"
         :subtitle            (i18n/label :t/can-send-messages)
         :subtitle-max-lines  2
         :theme               :positive
         :icon                :main-icons/network}])
     [quo/list-header (i18n/label :t/history-nodes)]
     (cond
       (#{:error :offline} node)
       [list.item/list-item
        {:title               (i18n/label :t/not-connected-nodes)
         :accessibility-label "not-connected-nodes"
         :subtitle            (i18n/label :t/unable-to-fetch)
         :theme               :negative
         :icon                :main-icons/mailserver}]
       (= node :disabled)
       [list.item/list-item
        {:title               (i18n/label :t/nodes-disabled)
         :accessibility-label "nodes-disabled"
         :subtitle            (i18n/label :t/unable-to-fetch)
         :disabled            true
         :icon                :main-icons/mailserver}]
       (and mobile (not sync))
       [list.item/list-item
        {:title               (i18n/label :t/waiting-wi-fi)
         :accessibility-label "waiting-wi-fi"
         :subtitle            (i18n/label :t/unable-to-fetch)
         :disabled            true
         :icon                :main-icons/mailserver}]
       (= node :connecting)
       [list.item/list-item
        {:title               (i18n/label :t/connecting)
         :accessibility-label "connecting"
         :subtitle            (i18n/label :t/unable-to-fetch)
         :icon                :main-icons/mailserver}]
       (= node :online)
       [list.item/list-item
        {:title               (str (i18n/label :t/connected-to) " " current-mailserver-name)
         :accessibility-label "connected-to-mailserver"
         :subtitle            (i18n/label :t/you-can-fetch)
         :theme               :positive
         :icon                :main-icons/mailserver}])
     [list.item/list-item
      {:title               (i18n/label :t/settings)
       :accessibility-label "settings"
       :theme               :accent
       :on-press            #(hide-sheet-and-dispatch [:navigate-to :sync-settings])
       :icon                :main-icons/settings}]
     (when mobile
       [:<>
        [react/view
         {:margin-vertical  8
          :background-color colors/gray-lighter
          :height           36
          :align-items      :center
          :justify-content  :center}
         [react/text {:style {:color colors/gray}} (i18n/label :t/youre-on-mobile-network)]]
        [list.item/list-item
         {:title               (i18n/label :t/mobile-network-use-mobile)
          :accessibility-label "mobile-network-use-mobile"
          :accessory           :switch
          :on-press            #(re-frame/dispatch [:mobile-network/set-syncing
                                                    (not syncing-on-mobile-network?)])
          :active              syncing-on-mobile-network?}]
        [react/text {:style {:margin-horizontal 16 :margin-bottom 12 :color colors/gray}}
         (i18n/label :t/status-mobile-descr)]])]))

(defn get-icon
  [{:keys [peers node mobile sync]}]
  (if (or (= peers :offline)
          (= node :offline)
          (= node :connecting))
    :main-icons/offline
    (if mobile
      (if sync :main-icons/mobile-sync :main-icons/mobile-sync-off)
      (when (#{:error :disabled} node) :main-icons/node-offline))))
