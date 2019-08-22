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

(defonce visible? (animation/create-value 1))
(defonce last-to-value (atom 1))

(defn animate
  ([visible duration to]
   (animate visible duration to nil))
  ([visible duration to callback]
   (when (not= to @last-to-value)
     (reset! last-to-value to)
     (animation/start
      (animation/timing visible
                        {:toValue         to
                         :duration        duration
                         :easing          (animation/cubic)
                         :useNativeDriver true})
      callback))))

(def tabs-list-data
  (->>
   [{:nav-stack           :chat-stack
     :content             {:title (i18n/label :t/chats)
                           :icon  :main-icons/message}
     :count-subscription  :chats/unread-messages-number
     :accessibility-label :home-tab-button}
    (when-not platform/desktop?
      {:nav-stack           :browser-stack
       :content             {:title (i18n/label :t/dapps)
                             :icon  :main-icons/dapp}
       :accessibility-label :dapp-tab-button})
    (when-not platform/desktop?
      {:nav-stack           :wallet-stack
       :content             {:title (i18n/label :t/wallet)
                             :icon  :main-icons/wallet}
       :accessibility-label :wallet-tab-button})
    {:nav-stack           :profile-stack
     :content             {:title (i18n/label :t/profile)
                           :icon  :main-icons/user-profile}
     :count-subscription  :get-profile-unread-messages-number
     :accessibility-label :profile-tab-button}]
   (remove nil?)))

(defn new-tab
  [{:keys [icon label active? nav-stack
           accessibility-label count-subscription]}]
  (let [count (when count-subscription
                (re-frame/subscribe [count-subscription]))]
    [react/touchable-highlight
     {:style               tabs.styles/touchable-container
      :disabled            active?
      :on-press            #(re-frame/dispatch-sync [:navigate-to nav-stack])
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

(defn main-tab? [view-id]
  (contains?
   #{:home :wallet :open-dapp :my-profile :wallet-onboarding-setup}
   view-id))

(defn minimize-bar [view-id]
  (if (main-tab? view-id)
    (animate visible? 150 1)
    (animate visible? 150 tabs.styles/minimized-tab-ratio)))

(defn tabs-animation-wrapper-ios
  [content]
  [react/view
   [react/view
    {:style tabs.styles/title-cover-wrapper}
    content
    (when platform/iphone-x?
      [react/view
       {:style tabs.styles/ios-titles-cover}])]
   [(react/safe-area-view) {:flex 1}]])

(defn tabs-animation-wrapper-android
  [keyboard-shown? view-id content]
  [react/view
   {:style (tabs.styles/animation-wrapper
            keyboard-shown?
            (main-tab? view-id))}
   [react/view
    {:style tabs.styles/title-cover-wrapper}
    content]])

(defn tabs-animation-wrapper [keyboard-shown? view-id tab]
  (reagent.core/create-class
   {:component-will-update
    (fn [this new-params]
      (let [old-view-id (get (.-argv (.-props this)) 2)
            new-view-id (get new-params 2)]
        (when (not= new-view-id old-view-id)
          (minimize-bar new-view-id))))
    :reagent-render
    (fn [keyboard-shown? view-id tab]
      (when-not (contains? #{:enter-pin-login
                             :enter-pin-sign
                             :enter-pin-settings} view-id)
        (if platform/ios?
          [tabs-animation-wrapper-ios
           [react/animated-view
            {:style (tabs.styles/animated-container visible? keyboard-shown?)}
            [tabs tab]]]
          [tabs-animation-wrapper-android
           keyboard-shown?
           view-id
           [react/animated-view
            {:style (tabs.styles/animated-container visible? keyboard-shown?)}
            [tabs tab]]])))}))

(def disappearance-duration 150)
(def appearance-duration 100)

(defn bottom-bar [_ view-id]
  (let [keyboard-shown? (reagent/atom false)
        listeners       (atom [])]
    (reagent/create-class
     {:component-did-mount
      (fn []
        (when platform/android?
          (reset!
           listeners
           [(.addListener (react/keyboard)  "keyboardDidShow"
                          (fn []
                            (reset! keyboard-shown? true)
                            (animate visible?
                                     disappearance-duration 0)))
            (.addListener (react/keyboard)  "keyboardDidHide"
                          (fn []
                            (reset! keyboard-shown? false)
                            (animate visible? appearance-duration
                                     (if (main-tab? @view-id)
                                       1
                                       tabs.styles/minimized-tab-ratio))))])))
      :component-will-unmount
      (fn []
        (when (not-empty @listeners)
          (doseq [listener @listeners]
            (when listener
              (.remove listener)))))
      :reagent-render
      (fn [args view-id]
        (let [idx (.. (:navigation args)
                      -state
                      -index)
              tab (case idx
                    0 :chat-stack
                    1 :browser-stack
                    2 :wallet-stack
                    3 :profile-stack
                    :chat-stack)]
          [tabs-animation-wrapper @keyboard-shown? @view-id tab]))})))
