(ns status-im.navigation2.home-stack
  (:require [quo.react-native :as rn]
            [status-im.switcher.switcher :as switcher]
            [status-im.ui.screens.home.views :as home]
            [status-im.switcher.constants :as switcher-constants]
            [status-im.ui.screens.profile.user.views :as profile.user]
            ;; [status-im.ui.screens.browser.empty-tab.views :as empty-tab]
            [status-im.ui.screens.communities.communities-list-redesign :as communities]
            [status-im.ui.screens.wallet.accounts.views :as wallet.accounts]
            [status-im.switcher.bottom-tabs :as bottom-tabs]))

;; TODO(parvesh) - improve stack changing performance (load all stacks at once)
(defn stack-view []
  (let [{:keys [width height]} (switcher-constants/dimensions)]
       ;; bottom-tabs-height     (switcher-constants/bottom-tabs-height)]
    [rn/view {:style {:width  width
                      :height (- height 80)}} ;; TODO(parvesh) - add height for ios
     (case @bottom-tabs/selected-tab-id
       :chats-stack       [home/home]
       :communities-stack [communities/views]
       :wallet-stack      [wallet.accounts/accounts-overview]
       ;;:browser-stack     [empty-tab/empty-tab])]))
       :browser-stack     [profile.user/my-profile])]))

(defn home []
  [:<>
   [stack-view]
   [bottom-tabs/bottom-tabs]
   [switcher/switcher :home-stack]])
