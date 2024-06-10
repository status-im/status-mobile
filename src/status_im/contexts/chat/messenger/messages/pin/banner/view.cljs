(ns status-im.contexts.chat.messenger.messages.pin.banner.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.messages.pin.banner.style :as style]
    [utils.re-frame :as rf]))

(defn f-banner
  [{:keys [chat-id banner-opacity top-offset]} latest-pin-text pins-count]
  (let [theme (quo.theme/use-theme)]
    [reanimated/view {:style (style/container-animated-style top-offset banner-opacity)}
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
                          (rf/dispatch [:pin-message/show-pins-bottom-sheet chat-id]))}]]))

(defn banner
  [{:keys [chat-id] :as props}]
  (let [latest-pin-text (rf/sub [:chats/last-pinned-message-text chat-id])
        pins-count      (rf/sub [:chats/pin-messages-count chat-id])]
    (when (> pins-count 0)
      [:f> f-banner props latest-pin-text pins-count])))
