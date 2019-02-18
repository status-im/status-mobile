(ns status-im.ui.components.bottom-bar.core
  (:require-macros [status-im.utils.views :as views])
  (:require
   [status-im.ui.components.animation :as animation]
   [status-im.ui.components.bottom-bar.styles :as tabs.styles]
   [reagent.core :as reagent]
   [status-im.ui.components.react :as react]
   [status-im.utils.platform :as platform]
   [status-im.ui.components.icons.vector-icons :as vector-icons]
   [status-im.ui.components.common.common :as components.common]
   [status-im.i18n :as i18n]
   [status-im.ui.components.styles :as common.styles]
   [re-frame.core :as re-frame]))

(defn animate
  ([visible duration to]
   (animate visible duration to nil))
  ([visible duration to callback]
   (animation/start
    (animation/timing visible
                      {:toValue         to
                       :duration        duration
                       :useNativeDriver true})
    callback)))

(def tabs-list-data
  [{:view-id             :chat-stack
    :content             {:title (i18n/label :t/home)
                          :icon  :main-icons/home}
    :count-subscription  :chats/unread-messages-number
    :accessibility-label :home-tab-button}
   {:view-id             :wallet-stack
    :content             {:title (i18n/label :t/wallet)
                          :icon  :main-icons/wallet}
    :count-subscription  :get-wallet-unread-messages-number
    :accessibility-label :wallet-tab-button}
   {:view-id             :profile-stack
    :content             {:title (i18n/label :t/profile)
                          :icon  :main-icons/profile}
    :count-subscription  :get-profile-unread-messages-number
    :accessibility-label :profile-tab-button}])

(defn- tab-content [{:keys [title icon]}]
  (fn [active? count]
    [react/view {:style tabs.styles/tab-container}
     [react/view
      [vector-icons/icon icon (tabs.styles/tab-icon active?)]]
     [react/view
      [react/text {:style (tabs.styles/tab-title active?)}
       title]]
     (when (pos? count)
       [react/view tabs.styles/counter-container
        [react/view tabs.styles/counter
         [components.common/counter count]]])]))

(def tabs-list (map #(update % :content tab-content) tabs-list-data))

(views/defview tab [view-id content active? accessibility-label count-subscription]
  (views/letsubs [count [count-subscription]]
    [react/touchable-highlight
     (cond-> {:style    common.styles/flex
              :disabled active?
              :on-press #(re-frame/dispatch [:navigate-to-tab view-id])}
       accessibility-label
       (assoc :accessibility-label accessibility-label))
     [react/view
      [content active? count]]]))

(defn tabs [current-view-id]
  [react/view {:style tabs.styles/tabs-container}
   (for [{:keys [content view-id accessibility-label count-subscription]} tabs-list]
     ^{:key view-id} [tab view-id content (= view-id current-view-id) accessibility-label count-subscription])])

(defn tabs-animation-wrapper [visible? keyboard-shown? tab]
  [react/animated-view
   {:style {:height    tabs.styles/tabs-height
            :bottom    0
            :left      0
            :right     0
            :position  (when keyboard-shown? :absolute)
            :transform [{:translateY
                         (animation/interpolate
                          visible?
                          {:inputRange  [0 1]
                           :outputRange [tabs.styles/tabs-height
                                         0]})}]}}
   [react/safe-area-view [tabs tab]]])

(def disappearance-duration 150)
(def appearance-duration 100)

(defn bottom-bar [_]
  (let [keyboard-shown? (reagent/atom false)
        visible?        (animation/create-value 1)
        listeners       (atom [])]
    (reagent/create-class
     {:component-will-mount
      (fn []
        (when platform/android?
          (reset!
           listeners
           [(.addListener react/keyboard "keyboardDidShow"
                          (fn []
                            (reset! keyboard-shown? true)
                            (animate visible?
                                     disappearance-duration 0)))
            (.addListener react/keyboard "keyboardDidHide"
                          (fn []
                            (reset! keyboard-shown? false)
                            (animate visible? appearance-duration 1)))])))
      :component-will-unmount
      (fn []
        (when (not-empty @listeners)
          (doseq [listener @listeners]
            (when listener
              (.remove listener)))))
      :reagent-render
      (fn [args]
        (let [idx (.. (:navigation args)
                      -state
                      -index)
              tab (case idx
                    0 :chat-stack
                    1 :wallet-stack
                    2 :profile-stack
                    :chat-stack)]
          (if platform/ios?
            [react/safe-area-view [tabs tab]]
            [tabs-animation-wrapper visible? @keyboard-shown? tab])))})))
