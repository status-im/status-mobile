(ns status-im2.contexts.shell.jump-to.components.home-stack.view
  (:require [react-native.reanimated :as reanimated]
            [status-im.ui.screens.wallet.accounts.views :as wallet.accounts]
            [status-im2.contexts.wallet.home.view :as wallet-new]
            [status-im2.contexts.chat.home.view :as chat]
            [status-im2.contexts.shell.jump-to.state :as state]
            [status-im2.contexts.shell.jump-to.utils :as utils]
            [status-im2.contexts.communities.home.view :as communities]
            [status-im2.contexts.shell.jump-to.constants :as shell.constants]
            [status-im2.contexts.shell.jump-to.components.home-stack.style :as style]
            [status-im.ui.screens.browser.stack :as browser.stack]))

(defn load-stack?
  [stack-id]
  (case stack-id
    :communities-stack @state/load-communities-stack?
    :chats-stack       @state/load-chats-stack?
    :browser-stack     @state/load-browser-stack?
    :wallet-stack      @state/load-wallet-stack?))

(defn- f-stack-view
  [stack-id shared-values]
  [reanimated/view
   {:style (style/stack-view
            stack-id
            {:opacity (get shared-values
                           (get shell.constants/stacks-opacity-keywords stack-id))
             :z-index (get shared-values
                           (get shell.constants/stacks-z-index-keywords stack-id))})}
   (case stack-id
     :communities-stack [:f> communities/home]
     :chats-stack       [:f> chat/home]
     ;NOTE temporary while we support old wallet
     :wallet-stack      (if @state/load-new-wallet?
                          [wallet-new/view]
                          [wallet.accounts/accounts-overview-old])
     :browser-stack     [browser.stack/browser-stack]
     [:<>])])

(defn lazy-screen
  [stack-id shared-values]
  (when (load-stack? stack-id)
    [:f> f-stack-view stack-id shared-values]))

(defn f-home-stack
  []
  (let [shared-values @state/shared-values-atom]
    [reanimated/view {:style (style/home-stack shared-values (utils/dimensions))}
     [lazy-screen :communities-stack shared-values]
     [lazy-screen :chats-stack shared-values]
     [lazy-screen :browser-stack shared-values]
     [lazy-screen :wallet-stack shared-values]]))
