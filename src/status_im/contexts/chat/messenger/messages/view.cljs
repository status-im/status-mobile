(ns status-im.contexts.chat.messenger.messages.view
  (:require
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.chat.messenger.composer.view :as composer.view]
    [status-im.contexts.chat.messenger.messages.list.style :as style]
    [status-im.contexts.chat.messenger.messages.list.view :as list.view]
    [status-im.contexts.chat.messenger.messages.navigation.view :as messages.navigation]
    [status-im.contexts.chat.messenger.placeholder.view :as placeholder.view]
    [utils.re-frame :as rf]))

(defn- chat-screen
  [{:keys [insets] :as props}]
  (let [alert-banners-top-margin (rf/sub [:alert-banners/top-margin])
        chat-exist?              (rf/sub [:chats/current-chat-exist?])]
    (when chat-exist?
      [rn/keyboard-avoiding-view
       {:style                    style/keyboard-avoiding-container
        :keyboard-vertical-offset (- (if platform/ios? alert-banners-top-margin 0) (:bottom insets))}
       [list.view/messages-list-content props]
       [messages.navigation/view props]
       [composer.view/composer props]])))

(defn lazy-chat-screen
  [chat-screen-layout-calculations-complete?]
  (let [screen-loaded?         (rf/sub [:shell/chat-screen-loaded?])
        distance-from-list-top (reanimated/use-shared-value 0)
        chat-list-scroll-y     (reanimated/use-shared-value 0)
        props                  {:insets (safe-area/get-insets)
                                :content-height (atom 0)
                                :layout-height (atom 0)
                                :distance-atom (atom 0)
                                :distance-from-list-top distance-from-list-top
                                :chat-list-scroll-y chat-list-scroll-y
                                :chat-screen-layout-calculations-complete?
                                chat-screen-layout-calculations-complete?}]
    (when-not screen-loaded?
      (reanimated/set-shared-value chat-screen-layout-calculations-complete? false)
      (reanimated/set-shared-value distance-from-list-top 0)
      (reanimated/set-shared-value chat-list-scroll-y 0))
    (when screen-loaded?
      [chat-screen props])))

(defn chat
  []
  (let [chat-screen-layout-calculations-complete? (reanimated/use-shared-value false)]
    [:<>
     [lazy-chat-screen chat-screen-layout-calculations-complete?]
     [placeholder.view/view chat-screen-layout-calculations-complete?]]))
