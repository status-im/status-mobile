(ns status-im.contexts.chat.messenger.messages.pin.banner.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.contexts.chat.messenger.messages.pin.banner.style :as style]
    [utils.re-frame :as rf]))

(defn banner
  [{:keys [chat-id top-offset]}]
  (let [theme           (quo.theme/use-theme)
        latest-pin-text (rf/sub [:chats/last-pinned-message-text chat-id])
        pins-count      (rf/sub [:chats/pin-messages-count chat-id])]
    (when (> pins-count 0)
      [rn/view {:style (style/container-with-top-offset top-offset)}
       [quo/blur
        {:style       style/container
         :blur-radius (if platform/ios? 20 10)
         :blur-type   theme
         :blur-amount 20}]
       [quo/banner
        {:latest-pin-text latest-pin-text
         :pins-count      pins-count
         :on-press        (fn []
                            (rf/dispatch [:dismiss-keyboard])
                            (rf/dispatch [:pin-message/show-pins-bottom-sheet chat-id]))}]])))
