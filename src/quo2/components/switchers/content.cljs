(ns quo2.components.switchers.content
  (:require [quo.react-native :as rn]
            [quo2.components.markdown.text :as text]
            [quo2.components.list-items.preview-list :as preview-list]
            [quo2.components.tags.context-tag.view :as tag]
            [quo2.components.notifications.notification-dot :as notification-dot]
            [quo2.components.counter.counter :as counter]
            [status-im2.constants :as constants]
            [react-native.fast-image :as fast-image]
            [status-im2.contexts.chat.messages.resolver.message-resolver :as resolver]
            [utils.i18n :as i18n]
            [quo2.components.switchers.style :as style]
            [quo2.foundations.colors :as colors]))

(defn notification-container
  [{:keys [notification-indicator counter-label]}]
  [rn/view {:style style/notification-container}
   (if (= notification-indicator :counter)
     [counter/counter
      {:outline             false
       :override-text-color colors/white}
      counter-label]
     [notification-dot/notification-dot])])

(defn content-container
  [{:keys                             [content-type data]
    {:keys [text parsed-text source]} :data}]
  [rn/view {:style {:flex 1}}
   (case content-type
     constants/content-type-text
     [text/text
      {:size            :paragraph-2
       :weight          :regular
       :number-of-lines 1
       :ellipsize-mode  :tail
       :style           style/last-message-text}
      (if parsed-text
        (resolver/resolve-message parsed-text)
        text)]

     constants/content-type-image
     [preview-list/preview-list
      {:type               :photo
       :more-than-99-label (i18n/label :counter-99-plus)
       :size               24
       :override-theme     :dark} data]

     constants/content-type-sticker
     [fast-image/fast-image
      {:source source
       :style  style/sticker}]

     constants/content-type-gif
     [fast-image/fast-image
      {:source source
       :style  style/gif}]

     constants/content-type-audio
     [tag/audio-tag data {:override-theme :dark}]

     constants/content-type-community
     [tag/community-tag
      (:avatar data)
      (:community-name data)
      {:override-theme :dark}]

     (constants/content-type-link) ;; Components not available
     ;; Code snippet content type is not supported yet
     [:<>]

     nil)])

(defn view
  [content]
  (let [{:keys [new-notifications?]} content]
    [rn/view {:style style/content-container}
     [content-container content]
     (when new-notifications? [notification-container content])]))
