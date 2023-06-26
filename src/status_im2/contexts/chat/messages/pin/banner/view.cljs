(ns status-im2.contexts.chat.messages.pin.banner.view
  (:require [quo2.core :as quo]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.messages.pin.banner.style :as style]
            [utils.re-frame :as rf]))

(defn f-blur-view
  [top-offset opacity-animation enabled?]
  [reanimated/view {:style (style/blur-container-style top-offset opacity-animation enabled?)}
   [blur/view (style/blur-view-style)]])

(defn f-banner
  [{:keys [chat-id opacity-animation all-loaded? top-offset]}]
  (let [latest-pin-text (rf/sub [:chats/last-pinned-message-text chat-id])
        pins-count      (rf/sub [:chats/pin-messages-count chat-id])]
    [rn/view
     [:f> f-blur-view top-offset opacity-animation (> pins-count 0)]
     [reanimated/view {:style (style/animated-pinned-banner top-offset all-loaded? opacity-animation)}
      [quo/banner
       {:latest-pin-text latest-pin-text
        :pins-count      pins-count
        :on-press        (fn []
                           (rf/dispatch [:dismiss-keyboard])
                           (rf/dispatch [:pin-message/show-pins-bottom-sheet chat-id]))}]]]))
