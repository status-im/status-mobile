(ns status-im.ui2.screens.chat.components.reply
  (:require [quo2.foundations.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [quo.react-native :as rn]
            [status-im.constants :as constants]
            [status-im.utils.handlers :refer [<sub >evt]]
            [quo2.components.markdown.text :as quo2.text]
            [status-im.ui.screens.chat.photos :as photos]
            [quo2.components.buttons.button :as quo2.button]
            [clojure.string :as string]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui2.screens.chat.composer.style :as styles]))

(defn get-quoted-text-with-mentions [parsed-text]
  (string/join
   (mapv (fn [{:keys [type literal children]}]
           (cond
             (= type "paragraph")
             (get-quoted-text-with-mentions children)

             (= type "mention")
             (<sub [:contacts/contact-name-by-identity literal])

             (seq children)
             (get-quoted-text-with-mentions children)

             :else
             literal))
         parsed-text)))

(defn format-author [contact-name]
  (let [author (if (or (= (aget contact-name 0) "@")
                       ;; in case of replies
                       (= (aget contact-name 1) "@"))
                 (or (stateofus/username contact-name)
                     (subs contact-name 0 81))
                 contact-name)]
    author))

(defn format-reply-author [from username current-public-key]
  (or (and (= from current-public-key)
           (i18n/label :t/You))
      (format-author username)))

; This component is also used for quoted pinned message as the UI is very similar
(defn reply-message [{:keys [from identicon content-type contentType parsed-text content]} in-chat-input? pin?]
  (let [contact-name       (<sub [:contacts/contact-name-by-identity from])
        current-public-key (<sub [:multiaccount/public-key])
        content-type       (or content-type contentType)]
    [rn/view {:style {:flex-direction      :row
                      :height              (when-not pin? 24)
                      :accessibility-label :reply-message}}
     [rn/view {:style (styles/reply-content pin?)}
      (when-not pin?
        ;;TODO quo2 icon should be used
        [icons/icon :main-icons/connector {:color           (colors/theme-colors colors/neutral-40 colors/neutral-60)
                                           :container-style {:position :absolute :left 10 :bottom -4 :width 16 :height 16}}])
      [rn/view {:style (styles/quoted-message pin?)}
       [photos/member-photo from identicon 16]
       [quo2.text/text {:weight          :semi-bold
                        :size            :paragraph-2
                        :number-of-lines 1
                        :style           {:margin-left 4}}
        (format-reply-author from contact-name current-public-key)]
       [quo2.text/text
        {:number-of-lines     1
         :size                :label
         :weight              :regular
         :accessibility-label :quoted-message
         :style               (merge
                               {:ellipsize-mode :tail
                                :text-transform :none
                                :margin-left 4
                                :margin-top 2}
                               (when (or (= constants/content-type-image content-type)
                                         (= constants/content-type-sticker content-type)
                                         (= constants/content-type-audio content-type))
                                 {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}))}
        (case (or content-type contentType)
          constants/content-type-image "Image"
          constants/content-type-sticker "Sticker"
          constants/content-type-audio "Audio"
          (get-quoted-text-with-mentions (or parsed-text (:parsed-text content))))]]]
     (when in-chat-input?
       [quo2.button/button {:width               24
                            :size                24
                            :type                :outline
                            :accessibility-label :reply-cancel-button
                            :on-press            #(>evt [:chat.ui/cancel-message-reply])}
        ;;TODO quo2 icon should be used
        [icons/icon :main-icons/close {:width  16
                                       :height 16
                                       :color  (colors/theme-colors colors/neutral-100 colors/neutral-40)}]])]))
