(ns status-im.ui.components.tabbar.core
  (:require [oops.core :refer [oget]]
            [quo.gesture-handler :as gesture-handler]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [cljs-bean.core :refer [bean]]
            [status-im.i18n :as i18n]
            [quo.components.safe-area :as safe-area]
            [status-im.ui.screens.routing.core :as navigation]
            [quo.animated :as animated]
            [quo.react-native :as rn]
            [status-im.ui.components.badge :as badge]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.tabbar.styles :as tabs.styles]
            [status-im.utils.platform :as platform]))

(defn main-tab? [view-id]
  (contains?
   #{:chat-stack :browser-stack :wallet-stack :profile-stack
     :home :wallet :open-dapp :my-profile :wallet-onboarding-setup}
   view-id))

(def tabs-list-data
  (->>
   [{:nav-stack           :chat-stack
     :content             {:title (i18n/label :t/chat)
                           :icon  :main-icons/message}
     :count-subscription  :chats/unread-messages-number
     :accessibility-label :home-tab-button}
    {:nav-stack           :browser-stack
     :content             {:title (i18n/label :t/browser)
                           :icon  :main-icons/browser}
     :accessibility-label :dapp-tab-button}
    {:nav-stack           :wallet-stack
     :content             {:title (i18n/label :t/wallet)
                           :icon  :main-icons/wallet}
     :accessibility-label :wallet-tab-button}
    {:nav-stack           :profile-stack
     :content             {:title (i18n/label :t/profile)
                           :icon  :main-icons/user-profile}
     :count-subscription  :get-profile-unread-messages-number
     :accessibility-label :profile-tab-button}]
   (remove nil?)
   (map-indexed vector)))

(defn tab []
  (fn [{:keys [icon label active? nav-stack on-press
               accessibility-label count-subscription]}]
    (let [count (when count-subscription @(re-frame/subscribe [count-subscription]))]
      [react/view {:style tabs.styles/touchable-container}
       [gesture-handler/touchable-without-feedback
        {:style               {:height "100%"
                               :width  "100%"}
         :on-press            on-press
         :accessibility-label accessibility-label}
        [react/view {:style tabs.styles/tab-container}
         [react/view {:style tabs.styles/icon-container}
          [vector-icons/icon icon (tabs.styles/icon active?)]
          (when count
            (cond
              (or (pos? count) (pos? (:other count)))
              [react/view {:style (if (= nav-stack :chat-stack)
                                    tabs.styles/message-counter
                                    tabs.styles/counter)}
               [badge/message-counter (or (:other count) count) true]]
              (pos? (:public count))
              [react/view {:style (tabs.styles/counter-public-container)}
               [react/view {:style               tabs.styles/counter-public
                            :accessibility-label :public-unread-badge}]]))]
         [react/view {:style tabs.styles/tab-title-container}
          [react/text {:style (tabs.styles/tab-title active?)}
           label]]]]])))

(def tabs
  (reagent/adapt-react-class
   (fn [props]
     (let [{:keys [navigate index route state popToTop]} (bean props)
           {:keys [keyboard-shown]
            :or   {keyboard-shown false}} (when platform/android? (rn/use-keyboard))
           {:keys [bottom]} (safe-area/use-safe-area)
           animated-visible (animated/use-timing-transition
                             (main-tab? (keyword route))
                             {:duration 150})
           keyboard-visible (animated/use-timing-transition
                             keyboard-shown
                             {:duration 200})]
       (reagent/as-element
        [animated/view {:style          (tabs.styles/tabs-wrapper keyboard-shown keyboard-visible)
                        :pointer-events (if keyboard-shown "none" "auto")}
         [animated/view {:style          (tabs.styles/space-handler bottom)
                         :pointer-events "none"}]
         [animated/view {:style (tabs.styles/animated-container animated-visible bottom)}
          (for [[route-index
                 {:keys [nav-stack accessibility-label count-subscription content]}]
                tabs-list-data
                :let [{:keys [icon title]} content]]
            ^{:key nav-stack}
            [tab
             {:icon                icon
              :label               title
              :on-press            #(if (= (str index) (str route-index))
                                      (popToTop)
                                      (let [view-id (navigation/get-index-route-name route-index (bean state))]
                                        (re-frame/dispatch-sync [:screens/tab-will-change view-id])
                                        (reagent/flush)
                                        (navigate (name nav-stack))))
              :accessibility-label accessibility-label
              :count-subscription  count-subscription
              :active?             (= (str index) (str route-index))
              :nav-stack           nav-stack}])]
         [react/view
          {:style (tabs.styles/ios-titles-cover bottom)}]])))))

(defn tabbar [props]
  (let [navigate (oget props "navigation" "navigate")
        pop-to-top (oget props "navigation" "popToTop")
        state (bean (oget props "state"))
        index (get state :index)]
    (reagent/as-element
     [tabs {:navigate navigate
            :state    (oget props "state")
            :popToTop pop-to-top
            :route    (navigation/get-active-route-name state)
            :index    index}])))
