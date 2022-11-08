(ns status-im.switcher.home-stack
  (:require [react-native.reanimated :as reanimated]
            [status-im.switcher.styles :as styles]
            [status-im.switcher.animation :as animation]
            [status-im.ui2.screens.chat.home :as chat.home]
            [status-im.switcher.constants :as constants]
            [status-im.switcher.bottom-tabs :as bottom-tabs]
            [status-im.ui.screens.profile.user.views :as profile.user]
            [status-im.ui.screens.wallet.accounts.views :as wallet.accounts]
            [quo2.components.navigation.floating-shell-button :as floating-shell-button]
            [status-im.ui2.screens.communities.communities-home :as communities-home]))

(defn load-stack? [stack-id]
  (case stack-id
    :communities-stack @bottom-tabs/load-communities-tab?
    :chats-stack       @bottom-tabs/load-chats-tab?
    :browser-stack     @bottom-tabs/load-browser-tab?
    :wallet-stack      @bottom-tabs/load-wallet-tab?))

(defn stack-view [stack-id shared-values]
  (when (load-stack? stack-id)
    [:f>
     (fn []
       [reanimated/view
        {:style (reanimated/apply-animations-to-style
                 {:opacity        (get shared-values (get constants/stacks-opacity-keywords stack-id))
                  :pointer-events (get shared-values (get constants/stacks-pointer-keywords stack-id))}
                 {:position :absolute
                  :top                 0
                  :bottom              0
                  :left                0
                  :right               0
                  :accessibility-label stack-id})}
        (case stack-id
          :communities-stack [communities-home/views]
          :chats-stack       [chat.home/home]
          :wallet-stack      [wallet.accounts/accounts-overview]
          :browser-stack     [profile.user/my-profile])])]))

(defn home-stack [shared-values]
  [:f>
   (fn []
     (let [home-stack-original-style (styles/home-stack)
           home-stack-animated-style (reanimated/apply-animations-to-style
                                      {:top            (:home-stack-top shared-values)
                                       :left           (:home-stack-left shared-values)
                                       :opacity        (:home-stack-opacity shared-values)
                                       :pointer-events (:home-stack-pointer shared-values)
                                       :transform      [{:scale (:home-stack-scale shared-values)}]}
                                      home-stack-original-style)]
       [reanimated/view {:style home-stack-animated-style}
        [stack-view :communities-stack shared-values]
        [stack-view :chats-stack shared-values]
        [stack-view :browser-stack shared-values]
        [stack-view :wallet-stack shared-values]
        [floating-shell-button/floating-shell-button
         {:jump-to {:on-press #(animation/close-home-stack shared-values)}}
         {:position :absolute
          :bottom   12}]]))])
