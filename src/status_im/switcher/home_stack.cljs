(ns status-im.switcher.home-stack
  (:require [quo2.reanimated :as reanimated]
            [quo2.screens.main :as quo2.preview]
            [status-im.utils.platform :as platform]
            [status-im.switcher.switcher :as switcher]
            [status-im.ui.screens.home.views :as home]
            [status-im.switcher.constants :as constants]
            [status-im.switcher.bottom-tabs :as bottom-tabs]
            [status-im.ui.screens.profile.user.views :as profile.user]
            [status-im.ui.screens.wallet.accounts.views :as wallet.accounts]))

(defn load-stack? [stack-id]
  (case stack-id
    :communities-stack @bottom-tabs/load-communities-tab?
    :chats-stack @bottom-tabs/load-chats-tab?
    :browser-stack @bottom-tabs/load-browser-tab?
    :wallet-stack @bottom-tabs/load-wallet-tab?))

(defn stack-view [stack-id shared-values top-margin]
  (when (load-stack? stack-id)
    [:f>
     (fn []
       [reanimated/view {:style (reanimated/apply-animations-to-style
                                 {:opacity        (get shared-values (get constants/stacks-opacity-keywords stack-id))
                                  :pointer-events (get shared-values (get constants/stacks-pointer-keywords stack-id))}
                                 {:top              top-margin
                                  :bottom           (if platform/ios? 79 55)
                                  :left             0
                                  :right            0
                                  :position         :absolute})}
        (case stack-id
          :communities-stack [quo2.preview/main-screen]
          :chats-stack       [home/home]
          :wallet-stack      [wallet.accounts/accounts-overview]
          :browser-stack     [profile.user/my-profile])])]))

(defn home-stack [shared-values]
  [:<>
   [stack-view :communities-stack shared-values (if platform/ios? 47 0)]
   [stack-view :chats-stack shared-values (if platform/ios? 47 0)]
   [stack-view :browser-stack shared-values 0]
   [stack-view :wallet-stack shared-values 0]])

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
