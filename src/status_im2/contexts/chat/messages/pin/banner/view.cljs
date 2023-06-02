(ns status-im2.contexts.chat.messages.pin.banner.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.messages.resolver.message-resolver :as resolver]
            [status-im2.contexts.chat.messages.pin.banner.style :as style]
            [status-im2.constants :as constants]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn message-text
  [{:keys [content-type] :as message}]
  (cond (= content-type constants/content-type-audio)
        (i18n/label :audio-message)
        :else
        (get-in message [:content :parsed-text])))

(defn blur-container-style
  [layout-height top-offset opacity-animation]
  (reanimated/apply-animations-to-style
    {:opacity opacity-animation}
    {:position :absolute
     :top      top-offset
     :left     0
     :right    0
     :bottom   0
     :height   layout-height
     :overflow :hidden}))

(defn blur-view-style
  []
  {:style       {:flex 1}
   :blur-radius (if platform/ios? 20 10)
   :blur-type   (colors/theme-colors :light :dark)
   :blur-amount 20})

(defn f-blur-view
  [layout-height top-offset opacity-animation]
  [reanimated/view {:style (blur-container-style layout-height top-offset opacity-animation)}
   [blur/view (blur-view-style)]])

(defn blur-view
  [layout-height top-offset opacity-animation]
  [:f> f-blur-view layout-height top-offset opacity-animation])

(defn f-banner
  [{:keys [chat-id opacity-animation all-loaded? top-offset]}]
  (let [pinned-message (rf/sub [:chats/last-pinned-message chat-id])
        latest-pin-text (message-text pinned-message)
        {:keys [deleted? deleted-for-me?]} pinned-message
        pins-count (rf/sub [:chats/pin-messages-count chat-id])
        latest-pin-text
        (cond deleted? (i18n/label :t/message-deleted-for-everyone)
              deleted-for-me? (i18n/label :t/message-deleted-for-you)
              (#{constants/content-type-text
                 constants/content-type-image
                 constants/content-type-sticker
                 constants/content-type-emoji}
               (:content-type pinned-message))
              (resolver/resolve-message latest-pin-text)
              :else latest-pin-text)]
    [rn/view
     [blur-view 32 top-offset opacity-animation]
     [reanimated/view {:style (style/animated-pinned-banner top-offset all-loaded? opacity-animation)}
      [quo/banner
       {:latest-pin-text latest-pin-text
        :pins-count      pins-count
        :on-press        (fn []
                           (rf/dispatch [:dismiss-keyboard])
                           (rf/dispatch [:pin-message/show-pins-bottom-sheet chat-id]))}]]]))

(defn banner
  [props]
  [:f> f-banner props])

