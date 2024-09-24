(ns status-im.contexts.chat.messenger.messages.view
  (:require
    [clojure.string :as string]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.contexts.chat.messenger.composer.view :as composer]
    [status-im.contexts.chat.messenger.messages.contact-requests.bottom-drawer.view :as
     contact-requests.bottom-drawer]
    [status-im.contexts.chat.messenger.messages.list.style :as style]
    [status-im.contexts.chat.messenger.messages.list.view :as list.view]
    [status-im.contexts.chat.messenger.messages.navigation.view :as messages.navigation]
    [status-im.contexts.chat.messenger.messages.scroll-to-bottom.view :as scroll-to-bottom]
    [status-im.contexts.chat.messenger.placeholder.view :as placeholder.view]
    [status-im.feature-flags :as ff]
    [utils.re-frame :as rf]))

(defn- footer
  [layout-height]
  (let [current-chat-id       (rf/sub [:chats/current-chat-id])
        able-to-send-message? (rf/sub [:chats/able-to-send-message?])]
    (when-not (string/blank? current-chat-id)
      (if able-to-send-message?
        [composer/view layout-height]
        [contact-requests.bottom-drawer/view {:contact-id current-chat-id}]))))

(defn- chat-screen
  [on-layout-done?]
  (let [theme                    (quo.theme/use-theme)
        layout-height            (rn/use-ref-atom 0)
        distance-from-list-top   (reanimated/use-shared-value 0)
        chat-list-scroll-y       (reanimated/use-shared-value 0)
        alert-banners-top-margin (rf/sub [:alert-banners/top-margin])
        insets                   (safe-area/get-insets)]
    [rn/keyboard-avoiding-view
     {:style                    (style/keyboard-avoiding-container theme)
      :keyboard-vertical-offset (- (if platform/ios? alert-banners-top-margin 0)
                                   (:bottom insets))}
     [:<>
      [list.view/messages-list-content
       {:insets                 insets
        :distance-from-list-top distance-from-list-top
        :chat-list-scroll-y     chat-list-scroll-y
        :layout-height          layout-height
        :on-layout-done?        on-layout-done?}]
      [scroll-to-bottom/button chat-list-scroll-y]]
     [messages.navigation/view]
     [footer layout-height]]))

(defn chat
  []
  (let [on-layout-done?    (reagent/atom false)
        first-render-done? (reagent/atom false)]
    (fn []
      (let [chat-exists?               (rf/sub [:chats/current-chat-exist?])
            jump-to-enabled?           (ff/enabled? ::ff/shell.jump-to)
            screen-loaded-for-jump-to? (rf/sub [:shell/chat-screen-loaded?])
            screen-loaded?             (if jump-to-enabled?
                                         screen-loaded-for-jump-to?
                                         @first-render-done?)]
        (rn/use-mount #(reset! first-render-done? true))
        [:<>
         (when (and chat-exists? screen-loaded?)
           [chat-screen on-layout-done?])
         [placeholder.view/view on-layout-done?]]))))
