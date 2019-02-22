(ns status-im.ui.components.bottom-bar.core
  (:require
   [status-im.ui.components.animation :as animation]
   [status-im.ui.components.bottom-bar.styles :as tabs.styles]
   [reagent.core :as reagent]
   [status-im.ui.components.react :as react]
   [status-im.utils.platform :as platform]
   [status-im.ui.components.icons.vector-icons :as vector-icons]
   [status-im.ui.components.common.common :as components.common]
   [status-im.i18n :as i18n]
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
  [{:nav-stack           :chat-stack
    :content             {:title (i18n/label :t/chats)
                          :icon  :main-icons/message}
    :count-subscription  :chats/unread-messages-number
    :accessibility-label :home-tab-button}
   #_{:nav-stack           :dapp-stack
      :content             {:title (i18n/label :t/dapp)
                            :icon  :main-icons/dapp}
      ;;:count-subscription  :chats/unread-messages-number
      :accessibility-label :dapp-tab-button}
   {:nav-stack           :wallet-stack
    :content             {:title (i18n/label :t/wallet)
                          :icon  :main-icons/wallet}
    :count-subscription  :get-wallet-unread-messages-number
    :accessibility-label :wallet-tab-button}
   {:nav-stack           :profile-stack
    :content             {:title (i18n/label :t/profile)
                          :icon  :main-icons/user-profile}
    :count-subscription  :get-profile-unread-messages-number
    :accessibility-label :profile-tab-button}])

(defn new-tab
  [{:keys [icon label active? nav-stack
           accessibility-label count-subscription]}]
  (let [count (when count-subscription
                (re-frame/subscribe [count-subscription]))]
    [react/touchable-highlight
     {:style               tabs.styles/touchable-container
      :disabled            active?
      :on-press            #(re-frame/dispatch [:navigate-to nav-stack])
      :accessibility-label accessibility-label}
     [react/view
      {:style tabs.styles/new-tab-container}
      [react/view
       {:style tabs.styles/icon-container}
       [vector-icons/icon icon (tabs.styles/icon active?)]
       (when (pos? (if count @count 0))
         [react/view tabs.styles/counter
          [components.common/counter @count]])]
      [react/view {:style tabs.styles/tab-title-container}
       [react/text {:style (tabs.styles/new-tab-title active?)}
        label]]]]))

(defn tabs [current-view-id]
  [react/view
   {:style tabs.styles/new-tabs-container}
   [react/view {:style tabs.styles/tabs}
    (for [{:keys                [nav-stack accessibility-label count-subscription]
           {:keys [icon title]} :content} tabs-list-data]
      ^{:key nav-stack}
      [new-tab
       {:icon                icon
        :label               title
        :accessibility-label accessibility-label
        :count-subscription  count-subscription
        :active?             (= current-view-id nav-stack)
        :nav-stack           nav-stack}])]])

(defn tabs-animation-wrapper [visible? keyboard-shown? tab]
  [react/animated-view
   {:style (tabs.styles/animated-container visible? keyboard-shown?)}
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
