(ns status-im.contexts.shell.home-stack.view
  (:require
    [legacy.status-im.ui.screens.browser.stack :as browser.stack]
    [quo.context]
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
    :screen/communities-stack @state/load-communities-stack?
    :screen/chats-stack       @state/load-chats-stack?
    :screen/browser-stack     @state/load-browser-stack?
    :screen/wallet-stack      @state/load-wallet-stack?))

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
     :screen/communities-stack [communities/view]
     :screen/chats-stack       [chat/view]
     :screen/wallet-stack      [wallet/view]
     :screen/browser-stack     [browser.stack/browser-stack]
     [:<>])])

(defn lazy-screen
  [stack-id shared-values theme]
  (when (load-stack? stack-id)
    [quo.context/provider {:theme theme :screen-id stack-id}
     [f-stack-view stack-id shared-values]]))

(defn view
  [shared-values]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style (style/home-stack theme)}
     [lazy-screen :screen/communities-stack shared-values theme]
     [lazy-screen :screen/chats-stack shared-values theme]
     [lazy-screen :screen/browser-stack shared-values theme]
     [lazy-screen :screen/wallet-stack shared-values theme]]))
