(ns status-im.contexts.chat.messenger.messages.view
  (:require
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.chat.messenger.composer.view :as composer.view]
    [status-im.contexts.chat.messenger.messages.list.style :as style]
    [status-im.contexts.chat.messenger.messages.list.view :as list.view]
    [status-im.contexts.chat.messenger.messages.navigation.view :as messages.navigation]
    [status-im.contexts.chat.messenger.placeholder.view :as placeholder.view]
    [utils.re-frame :as rf]))

;; NOTE(parvesh) - I am working on refactoring/optimization of the chat screen for performance
;; improvement. Please avoid refactoring these files. Also if you are not already working on bug
;; fixes related to the composer, please skip them. And ping me, so I can address them while refactoring
(defn- chat-screen
  [chat-screen-layout-calculations-complete?]
  (let [insets                 (safe-area/get-insets)
        content-height         (atom 0)
        layout-height          (atom 0)
        distance-atom          (atom 0)
        distance-from-list-top (reanimated/use-shared-value 0)
        chat-list-scroll-y     (reanimated/use-shared-value 0)]
    [rn/keyboard-avoiding-view
     {:style                    style/keyboard-avoiding-container
      :keyboard-vertical-offset (:bottom insets)}
     [list.view/messages-list-content
      {:insets                                    insets
       :layout-height                             layout-height
       :content-height                            content-height
       :distance-atom                             distance-atom
       :chat-screen-layout-calculations-complete? chat-screen-layout-calculations-complete?
       :distance-from-list-top                    distance-from-list-top
       :chat-list-scroll-y                        chat-list-scroll-y}]
     [messages.navigation/view
      {:distance-from-list-top                    distance-from-list-top
       :chat-screen-layout-calculations-complete? chat-screen-layout-calculations-complete?}]
     [composer.view/composer
      {:insets                                    insets
       :chat-screen-layout-calculations-complete? chat-screen-layout-calculations-complete?
       :chat-list-scroll-y                        chat-list-scroll-y}]]))

(defn lazy-chat-screen
  [chat-screen-layout-calculations-complete?]
  (let [screen-loaded? (rf/sub [:shell/chat-screen-loaded?])]
    (when-not screen-loaded?
      (reanimated/set-shared-value chat-screen-layout-calculations-complete? false))
    (when screen-loaded?
      [chat-screen chat-screen-layout-calculations-complete?])))

(defn chat
  []
  (let [chat-screen-layout-calculations-complete? (reanimated/use-shared-value false)]
    [:<>
     [lazy-chat-screen chat-screen-layout-calculations-complete?]
     [placeholder.view/view chat-screen-layout-calculations-complete?]]))
