(ns status-im.switcher.home-stack
  (:require [quo2.reanimated :as reanimated]
            [status-im.utils.platform :as platform]
            [status-im.switcher.switcher :as switcher]
            [status-im.ui.screens.home.views :as home]
            [status-im.switcher.constants :as constants]
            [status-im.switcher.bottom-tabs :as bottom-tabs]
            [status-im.ui.screens.profile.user.views :as profile.user]
            [status-im.ui.screens.communities.communities-list-redesign :as communities]
            [status-im.ui.screens.wallet.accounts.views :as wallet.accounts]))

(defn load-stack? [stack-id]
  (case stack-id
    :communities-stack @bottom-tabs/load-communities-tab?
    :chats-stack @bottom-tabs/load-chats-tab?
    :browser-stack @bottom-tabs/load-browser-tab?
    :wallet-stack @bottom-tabs/load-wallet-tab?))

(defn stack-view [stack-id shared-values]
  (when (load-stack? stack-id)
    [:f>
     (fn []
       [reanimated/view {:style (reanimated/apply-animations-to-style
                                 {:opacity        (get shared-values (get constants/stacks-opacity-keywords stack-id))
                                  :pointer-events (get shared-values (get constants/stacks-pointer-keywords stack-id))}
                                 {:top              0
                                  :bottom           (if platform/ios? 79 54)
                                  :left             0
                                  :right            0
                                  :position         :absolute})}
        (case stack-id
          :communities-stack [communities/views]
          :chats-stack       [home/home]
          :wallet-stack      [wallet.accounts/accounts-overview]
          :browser-stack     [profile.user/my-profile])])]))

(defn home-stack [shared-values]
  [:<>
   [stack-view :communities-stack shared-values]
   [stack-view :chats-stack shared-values]
   [stack-view :browser-stack shared-values]
   [stack-view :wallet-stack shared-values]])

(defn home []
  [:f>
   (fn []
     (let [selected-stack-id @bottom-tabs/selected-stack-id
           shared-values     (reduce (fn [acc id]
                                       (let [selected-tab?         (= id selected-stack-id)
                                             tab-opacity-keyword   (get constants/tabs-opacity-keywords id)
                                             stack-opacity-keyword (get constants/stacks-opacity-keywords id)
                                             stack-pointer-keyword (get constants/stacks-pointer-keywords id)]
                                         (assoc
                                          acc
                                          tab-opacity-keyword   (reanimated/use-shared-value (if selected-tab? 1 0))
                                          stack-opacity-keyword (reanimated/use-shared-value (if selected-tab? 1 0))
                                          stack-pointer-keyword (reanimated/use-shared-value (if selected-tab? "auto" "none")))))
                                     {}
                                     constants/stacks-ids)]
       [:<>
        [home-stack shared-values]
        [bottom-tabs/bottom-tabs shared-values]
        [switcher/switcher :home-stack]]))])
