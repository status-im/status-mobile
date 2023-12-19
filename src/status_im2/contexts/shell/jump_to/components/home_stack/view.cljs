(ns status-im2.contexts.shell.jump-to.components.home-stack.view
  (:require
    [legacy.status-im.ui.screens.browser.stack :as browser.stack]
    [quo.theme :as quo.theme]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.home.view :as chat]
    [status-im2.contexts.communities.home.view :as communities]
    [status-im2.contexts.shell.jump-to.components.home-stack.style :as style]
    [status-im2.contexts.shell.jump-to.constants :as shell.constants]
    [status-im2.contexts.shell.jump-to.state :as state]
    [status-im2.contexts.shell.jump-to.utils :as utils]
    [status-im2.contexts.wallet.home.view :as wallet-new]))

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
     :wallet-stack      [wallet-new/view]
     :browser-stack     [browser.stack/browser-stack]
     [:<>])])

(defn lazy-screen
  [stack-id shared-values]
  (when (load-stack? stack-id)
    [:f> f-stack-view stack-id shared-values]))

(defn f-home-stack
  []
  (let [shared-values @state/shared-values-atom
        theme         (quo.theme/use-theme-value)]
    [reanimated/view {:style (style/home-stack shared-values (assoc (utils/dimensions) :theme theme))}
     [lazy-screen :communities-stack shared-values]
     [lazy-screen :chats-stack shared-values]
     [lazy-screen :browser-stack shared-values]
     [lazy-screen :wallet-stack shared-values]]))
