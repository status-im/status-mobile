(ns status-im.ui2.screens.chat.components.reply.view
  (:require [clojure.string :as string]
            [utils.i18n :as i18n]
            [quo.react-native :as rn]
            [quo2.components.buttons.button :as quo2.button]
            [quo2.components.icon :as quo2.icon]
            [quo2.components.markdown.text :as quo2.text]
            [quo2.foundations.colors :as colors]
            [status-im2.constants :as constants]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.screens.chat.photos :as photos]
            [utils.re-frame :as rf]
            [status-im.ui2.screens.chat.components.reply.style :as style]
            [react-native.linear-gradient :as linear-gradient]))

(defn get-quoted-text-with-mentions
  [parsed-text]
  (string/join
   (mapv (fn [{:keys [type literal children]}]
           (cond
             (= type "paragraph")
             (get-quoted-text-with-mentions children)

             (= type "mention")
             (rf/sub [:messages/resolve-mention literal])

             (seq children)
             (get-quoted-text-with-mentions children)

             :else
             literal))
         parsed-text)))

(defn format-author
  [contact-name]
  (let [author (if (or (= (aget contact-name 0) "@")
                       ;; in case of replies
                       (= (aget contact-name 1) "@"))
                 (or (stateofus/username contact-name)
                     (subs contact-name 0 81))
                 contact-name)]
    author))

(defn format-reply-author
  [from username current-public-key]
  (or (and (= from current-public-key)
           (i18n/label :t/You))
      (when username (format-author username))))

(defn reply-deleted-message
  []
  [rn/view
   {:style {:flex-direction :row
            :align-items    :center}}
   [quo2.icon/icon :sad-face {:size 16}]
   [quo2.text/text
    {:number-of-lines     1
     :size                :label
     :weight              :regular
     :accessibility-label :quoted-message
     :style               {:text-transform :none
                           :margin-left    4
                           :margin-top     2}}
    (i18n/label :t/message-deleted)]])

(defn reply-from
  [{:keys [from identicon contact-name current-public-key]}]
  [rn/view {:style style/reply-from}
   [photos/member-photo from identicon 16]
   [quo2.text/text
    {:weight          :semi-bold
     :size            :paragraph-2
     :number-of-lines 1
     :style           style/message-author-text}
    (format-reply-author from contact-name current-public-key)]])

(defn reply-message
  [{:keys [from identicon content-type contentType parsed-text content deleted? deleted-for-me?
           album-count]}
   in-chat-input? pin? recording-audio?]
  (let [contact-name       (rf/sub [:contacts/contact-name-by-identity from])
        current-public-key (rf/sub [:multiaccount/public-key])
        content-type       (or content-type contentType)]
    [rn/view
     {:style {:flex-direction      :row
              :height              (when-not pin? 24)
              :accessibility-label :reply-message}}
     [rn/view {:style (style/reply-content pin?)}
      (when-not pin?
        ;;TODO quo2 icon should be used
        [icons/icon :main-icons/connector
         {:color           (colors/theme-colors colors/neutral-40 colors/neutral-60)
          :container-style {:position :absolute :left 10 :bottom -4 :width 16 :height 16}}])
      (if (or deleted? deleted-for-me?)
        [rn/view {:style (style/quoted-message pin? in-chat-input?)}
         [reply-deleted-message]]
        [rn/view {:style (style/quoted-message pin? in-chat-input?)}
         [reply-from
          {:from               from
           :identicon          identicon
           :contact-name       contact-name
           :current-public-key current-public-key}]
         [quo2.text/text
          {:number-of-lines     1
           :size                :label
           :weight              :regular
           :accessibility-label :quoted-message
           :ellipsize-mode      :tail
           :style               (merge
                                 style/message-text
                                 (when (or (= constants/content-type-image content-type)
                                           (= constants/content-type-sticker content-type)
                                           (= constants/content-type-audio content-type))
                                   {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}))}
          (case (or content-type contentType)
            constants/content-type-image   (if album-count
                                             (str album-count \space (i18n/label :t/images))
                                             (i18n/label :t/image))
            constants/content-type-sticker (i18n/label :t/sticker)
            constants/content-type-audio   (i18n/label :t/audio)
            (get-quoted-text-with-mentions (or parsed-text (:parsed-text content))))]])]
     (when (and in-chat-input? (not recording-audio?))
       [quo2.button/button
        {:width               24
         :size                24
         :type                :outline
         :accessibility-label :reply-cancel-button
         :on-press            #(rf/dispatch [:chat.ui/cancel-message-reply])}
        ;;TODO quo2 icon should be used
        [icons/icon :main-icons/close
         {:width  16
          :height 16
          :color  (colors/theme-colors colors/neutral-100 colors/neutral-40)}]])
     (when (and in-chat-input? recording-audio?)
       [linear-gradient/linear-gradient
        {:colors [(colors/theme-colors colors/white-opa-0 colors/neutral-90-opa-0)
                  (colors/theme-colors colors/white colors/neutral-90)]
         :start  {:x 0 :y 0}
         :end    {:x 0.7 :y 0}
         :style  style/gradient}])]))
