(ns status-im2.contexts.shell.home-stack
  (:require [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [status-im.ui.screens.wallet.accounts.views :as wallet.accounts]
            [status-im2.contexts.chat.home.view :as chat] ;; TODO move to status-im2
            [status-im2.contexts.communities.home.view :as communities]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.constants :as shell.constants]
            [status-im2.contexts.shell.style :as styles]
            [status-im.ui.screens.browser.stack :as browser.stack]))

(defn load-stack?
  [stack-id]
  (case stack-id
    :communities-stack @animation/load-communities-stack?
    :chats-stack       @animation/load-chats-stack?
    :browser-stack     @animation/load-browser-stack?
    :wallet-stack      @animation/load-wallet-stack?))

(defn stack-view
  [stack-id shared-values]
  (when (load-stack? stack-id)
    [:f>
     (fn []
       [reanimated/view
        {:style (reanimated/apply-animations-to-style
                 {:opacity        (get shared-values
                                       (get shell.constants/stacks-opacity-keywords stack-id))
                  :pointer-events (get shared-values
                                       (get shell.constants/stacks-pointer-keywords stack-id))}
                 {:position            :absolute
                  :top                 0
                  :bottom              0
                  :left                0
                  :right               0
                  :accessibility-label stack-id})}
        (case stack-id
          :communities-stack [communities/home]
          :chats-stack       [chat/home]
          :wallet-stack      [wallet.accounts/accounts-overview]
          :browser-stack     [browser.stack/browser-stack])])]))

(defn home-stack
  []
  [safe-area/consumer
   (fn [insets]
     [:f>
      (fn []
        (let [shared-values             @animation/shared-values-atom
              home-stack-original-style (styles/home-stack @animation/screen-height)
              home-stack-animated-style (reanimated/apply-animations-to-style
                                         {:top            (:home-stack-top shared-values)
                                          :left           (:home-stack-left shared-values)
                                          :opacity        (:home-stack-opacity shared-values)
                                          :pointer-events (:home-stack-pointer shared-values)
                                          :transform      [{:scale (:home-stack-scale shared-values)}]}
                                         home-stack-original-style)]
          [reanimated/view {:style home-stack-animated-style}
           [rn/view {:margin-top (:top insets) :flex 1}
            [stack-view :communities-stack shared-values]
            [stack-view :chats-stack shared-values]
            [stack-view :browser-stack shared-values]
            [stack-view :wallet-stack shared-values]]]))])])
