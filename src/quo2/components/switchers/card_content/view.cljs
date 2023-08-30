(ns quo2.components.switchers.card-content.view
  (:require [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [quo2.components.switchers.card-content.style :as style]
            [quo2.components.common.notification-dot.view :as notification-dot]
            [quo2.components.counter.counter.view :as counter]
            [quo2.components.markdown.text :as text]
            [quo2.components.list-items.preview-list.view :as preview-list]
            [quo2.components.tags.context-tag.view :as tag]
            [quo2.components.code.snippet :as snippet]
            [utils.i18n :as i18n]))

(defn content-view
  [{:keys                           [type content]
    {:keys [text duration photos community-avatar
            community-name source]} :content}]
  [rn/view {:style {:flex 1 :max-width 108}}
   (case type
     :message
     [text/text
      {:size            :paragraph-2
       :weight          :regular
       :number-of-lines 1
       :ellipsize-mode  :tail
       :style           style/message-text}
      text]

     :photo
     [preview-list/view
      {:type               :collectibles
       :more-than-99-label (i18n/label :t/counter-99-plus)
       :size               :size/s-24}
      photos]

     :sticker
     [fast-image/fast-image
      {:accessibility-label :sticker
       :source              source
       :style               style/sticker}]

     :gif
     [fast-image/fast-image
      {:accessibility-label :gif
       :source              source
       :style               style/gif}]

     :audio
     [tag/view
      {:type     :audio
       :duration duration}]

     :community
     [tag/view
      {:type           :community
       :community-name community-name
       :community-logo community-avatar
       :size           24}]

     :link
     [tag/view
      {:type    :icon
       :icon    (:icon content)
       :context (:text content)
       :size    24}]

     :code
     [snippet/snippet
      {:language  (:language content)
       :max-lines 0}
      content]

     nil)])

(defn notification-indicator
  [{:keys [status mention-count customization-color]}]
  (when (not= status :read)
    [rn/view {:style style/notification-container}
     (case status
       :unread
       [notification-dot/view
        {:customization-color customization-color}]

       :mention
       [counter/view
        {:outline             false
         :customization-color customization-color}
        mention-count]

       nil)]))

(defn view
  [type status customization-color content]
  [rn/view {:style (style/content-container status)}
   [content-view {:type type :content content}]
   [notification-indicator
    {:status              status
     :customization-color customization-color
     :mention-count       (:mention-count content)}]])
