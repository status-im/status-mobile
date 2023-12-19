(ns status-im2.contexts.chat.messages.view
  (:require
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.chat.composer.view :as composer.view]
    [status-im2.contexts.chat.messages.list.style :as style]
    [status-im2.contexts.chat.messages.list.view :as list.view]
    [status-im2.contexts.chat.messages.navigation.view :as messages.navigation]
    [status-im2.contexts.chat.placeholder.view :as placeholder.view]
    [utils.re-frame :as rf]))

;; NOTE(parvesh) - I am working on refactoring/optimization of the chat screen for performance
;; improvement. Please avoid refactoring these files. Also if you are not already working on bug
;; fixes related to the chat navigation bar, please skip them.
;; And ping me, so I can address them while refactoring
(defn- f-chat-screen
  [calculations-complete?]
  (let [insets                            (safe-area/get-insets)
        keyboard-offset?                  (atom false)
        content-height                    (atom 0)
        show-floating-scroll-down-button? (reagent/atom false)
        messages-list-on-layout-finished? (reagent/atom false)
        distance-from-list-top            (reanimated/use-shared-value 0)]
    [rn/keyboard-avoiding-view
     {:style                    style/keyboard-avoiding-container
      :keyboard-vertical-offset (- (:bottom insets))}
     [:f> messages.navigation/f-view
      {:distance-from-list-top distance-from-list-top
       :calculations-complete? calculations-complete?}]
     [:f> list.view/f-messages-list-content
      {:insets                            insets
       :content-height                    content-height
       :keyboard-offset?                  keyboard-offset?
       :calculations-complete?            calculations-complete?
       :distance-from-list-top            distance-from-list-top
       :messages-list-on-layout-finished? messages-list-on-layout-finished?
       :cover-bg-color                    :turquoise
       :show-floating-scroll-down-button? show-floating-scroll-down-button?}]
     [composer.view/composer
      {:insets                            insets
       :scroll-to-bottom-fn               list.view/scroll-to-bottom
       :messages-list-on-layout-finished? messages-list-on-layout-finished?
       :show-floating-scroll-down-button? show-floating-scroll-down-button?}]]))

(defn lazy-chat-screen
  [calculations-complete?]
  (let [screen-loaded? (rf/sub [:shell/chat-screen-loaded?])]
    (when screen-loaded?
      [:f> f-chat-screen calculations-complete?])))

(defn- f-chat
  []
  (let [calculations-complete? (reanimated/use-shared-value false)]
    [:<>
     [lazy-chat-screen calculations-complete?]
     [:f> placeholder.view/f-view calculations-complete?]]))

(defn chat
  []
  [:f> f-chat])
