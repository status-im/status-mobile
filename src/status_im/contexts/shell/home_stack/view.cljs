(ns status-im.contexts.shell.home-stack.view
  (:require
    [legacy.status-im.ui.screens.browser.stack :as browser.stack]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.home.view :as chat]
    [status-im.contexts.communities.home.view :as communities]
    [status-im.contexts.shell.constants :as shell.constants]
    [status-im.contexts.shell.home-stack.style :as style]
    [status-im.contexts.shell.state :as state]
    [status-im.contexts.wallet.home.view :as wallet]))

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
     :communities-stack [:f> communities/view]
     :chats-stack       [:f> chat/view]
     :wallet-stack      [wallet/view]
     :browser-stack     [browser.stack/browser-stack]
     [:<>])])

(defn lazy-screen
  [stack-id shared-values]
  (when (load-stack? stack-id)
    [:f> f-stack-view stack-id shared-values]))

(defn view
  [shared-values]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style (style/home-stack theme)}
     [lazy-screen :communities-stack shared-values]
     [lazy-screen :chats-stack shared-values]
     [lazy-screen :browser-stack shared-values]
     [lazy-screen :wallet-stack shared-values]]))
