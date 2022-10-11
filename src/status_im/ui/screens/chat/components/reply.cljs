(ns status-im.ui.screens.chat.components.reply
  (:require [quo.core :as quo]
            [quo.react :as quo.react]
            [quo.react-native :as rn]
            [quo.design-system.colors :as quo.colors]
            [status-im.i18n.i18n :as i18n]
            [quo.components.animated.pressable :as pressable]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.ui.screens.chat.components.style :as styles]
            [re-frame.core :as re-frame]
            [clojure.string :as string]
            [quo2.foundations.colors :as quo2.colors :refer [theme-colors]]
            [quo2.components.buttons.button :as quo2.button]
            [quo2.components.markdown.text :as quo2.text]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.constants :as constants]))

(def ^:private reply-symbol "↪ ")

(defn input-focus [text-input-ref]
  (some-> ^js (quo.react/current-ref text-input-ref) .focus))

(defn format-author-old [contact-name]
  (let [author (if (or (= (aget contact-name 0) "@")
                       ;; in case of replies
                       (= (aget contact-name 1) "@"))
                 (or (stateofus/username contact-name)
                     (subs contact-name 0 81))
                 contact-name)]
    (i18n/label :replying-to {:author author})))

(defn format-author [contact-name]
  (let [author (if (or (= (aget contact-name 0) "@")
                       ;; in case of replies
                       (= (aget contact-name 1) "@"))
                 (or (stateofus/username contact-name)
                     (subs contact-name 0 81))
                 contact-name)]
    author))

(defn format-reply-author-old [from username current-public-key]
  (or (and (= from current-public-key)
           (str reply-symbol (i18n/label :t/You)))
      (str reply-symbol (format-author-old username))))

(defn format-reply-author [from username current-public-key]
  (or (and (= from current-public-key)
           (i18n/label :t/You))
      (format-author username)))

(defn get-quoted-text-with-mentions [parsed-text]
  (string/join
   (mapv (fn [{:keys [type literal children]}]
           (cond
             (= type "paragraph")
             (get-quoted-text-with-mentions children)

             (= type "mention")
             @(re-frame/subscribe [:contacts/contact-name-by-identity literal])

             (seq children)
             (get-quoted-text-with-mentions children)

             :else
             literal))
         parsed-text)))

(defn reply-message-old [{:keys [from]}]
  (let [contact-name       @(re-frame/subscribe [:contacts/contact-name-by-identity from])
        current-public-key @(re-frame/subscribe [:multiaccount/public-key])]
    [rn/view {:style {:flex-direction :row}}
     [rn/view {:style (styles/reply-content-old)}
      [quo/text {:weight          :medium
                 :number-of-lines 1
                 :style           {:line-height 18}}
       (format-reply-author-old from contact-name current-public-key)]]
     [rn/view
      [pressable/pressable {:on-press            #(re-frame/dispatch [:chat.ui/cancel-message-reply])
                            :accessibility-label :cancel-message-reply}
       [icons/icon :main-icons/close-circle {:container-style (styles/close-button)
                                             :color (:icon-02 @quo.colors/theme)}]]]]))

(defn reply-message [{:keys [from identicon content-type contentType parsed-text content]} in-chat-input?]
  (let [contact-name       @(re-frame/subscribe [:contacts/contact-name-by-identity from])
        current-public-key @(re-frame/subscribe [:multiaccount/public-key])
        content-type       (or content-type contentType)]
    [rn/view {:style {:flex-direction :row :height 24}}
     [rn/view {:style (styles/reply-content)}
      [icons/icon :main-icons/connector {:color (theme-colors quo2.colors/neutral-40 quo2.colors/neutral-60)
                                         :container-style {:position :absolute :left 10 :bottom -4 :width 16 :height 16}}]
      [rn/view {:style {:position :absolute :left 34 :top 3 :flex-direction :row :align-items :center :width "45%"}}
       [photos/member-photo from identicon 16]
       [quo2.text/text {:weight          :semi-bold
                        :size            :paragraph-2
                        :number-of-lines 1
                        :style           {:margin-left 4}}
        (format-reply-author from contact-name current-public-key)]
       [quo2.text/text {:number-of-lines 1
                        :size            :label
                        :weight          :regular
                        :style           (merge {:ellipsize-mode :tail
                                                 :text-transform :none
                                                 :margin-left 4
                                                 :margin-top 2}
                                                (when (or (= constants/content-type-image content-type)
                                                          (= constants/content-type-sticker content-type)
                                                          (= constants/content-type-audio content-type))
                                                  {:color (theme-colors quo2.colors/neutral-50 quo2.colors/neutral-40)}))}
        (case (or content-type contentType)
          constants/content-type-image "Image"
          constants/content-type-sticker "Sticker"
          constants/content-type-audio "Audio"
          (get-quoted-text-with-mentions (or parsed-text (:parsed-text content))))]]]
     (when in-chat-input?
       [quo2.button/button {:width               24
                            :size 24
                            :type                :outline
                            :accessibility-label :reply-cancel-button
                            :on-press            #(re-frame/dispatch [:chat.ui/cancel-message-reply])}
        [icons/icon :main-icons/close {:width 16
                                       :height 16
                                       :color (theme-colors quo2.colors/neutral-100 quo2.colors/neutral-40)}]])]))

(defn send-image [images]
  [rn/view {:style (styles/reply-container-image)}
   [rn/scroll-view {:horizontal true
                    :style      (styles/reply-content)}
    (for [{:keys [uri]} (vals images)]
      ^{:key uri}
      [rn/image {:source {:uri uri}
                 :style  {:width         56
                          :height        56
                          :border-radius 4
                          :margin-right  4}}])]
   [rn/view
    [pressable/pressable {:on-press            #(re-frame/dispatch [:chat.ui/cancel-sending-image])
                          :accessibility-label :cancel-send-image}
     [icons/icon :main-icons/close-circle {:container-style (styles/close-button)
                                           :color           quo.colors/white}]]]])

(defn focus-input-on-reply [reply had-reply text-input-ref]
  ;;when we show reply we focus input
  (when-not (= reply @had-reply)
    (reset! had-reply reply)
    (when reply
      ;; A setTimeout of 0 is necessary to ensure the statement is enqueued and will get executed ASAP.
      (js/setTimeout #(input-focus text-input-ref) 0))))

(defn reply-message-wrapper-old [reply]
  [rn/view {:style {:padding-horizontal 15
                    :border-top-width 1
                    :border-top-color (:ui-01 @quo.colors/theme)
                    :padding-vertical 8}}
   [reply-message-old reply]])

(defn reply-message-wrapper [reply]
  [rn/view {:style {:padding-horizontal 15
                    :padding-vertical 8}}
   [reply-message reply true]])

(defn reply-message-auto-focus-wrapper-old [text-input-ref]
  (let [had-reply (atom nil)]
    (fn []
      (let [reply @(re-frame/subscribe [:chats/reply-message])]
        (focus-input-on-reply reply had-reply text-input-ref)
        (when reply
          [reply-message-wrapper-old reply])))))

(defn reply-message-auto-focus-wrapper [text-input-ref _]
  (let [had-reply (atom nil)]
    (fn [_ reply]
      (focus-input-on-reply reply had-reply text-input-ref)
      (when reply
        [reply-message-wrapper reply]))))
