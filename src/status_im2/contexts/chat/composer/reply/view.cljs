(ns status-im2.contexts.chat.composer.reply.view
  (:require [clojure.string :as string]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.composer.constants :as constants]
            [utils.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [status-im2.constants :as constant]
            [status-im.ethereum.stateofus :as stateofus]
            [utils.re-frame :as rf]
            [status-im2.contexts.chat.composer.reply.style :as style]
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
  (let [author (if (or (= (first contact-name) "@")
                       ;; in case of replies
                       (= (second contact-name) "@"))
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
   [quo/icon :i/sad-face {:size 16}]
   [quo/text
    {:number-of-lines     1
     :size                :label
     :weight              :regular
     :accessibility-label :quoted-message
     :style               style/reply-deleted-message}
    (i18n/label :t/message-deleted)]])

(defn reply-from
  [{:keys [from contact-name current-public-key]}]
  (let [display-name (first (rf/sub [:contacts/contact-two-names-by-identity from]))
        contact      (rf/sub [:contacts/contact-by-address from])
        photo-path   (when-not (empty? (:images contact)) (rf/sub [:chats/photo-path from]))]
    [rn/view {:style style/reply-from}
     [quo/user-avatar
      {:full-name         display-name
       :profile-picture   photo-path
       :status-indicator? false
       :size              :xxxs}]
     [quo/text
      {:weight          :semi-bold
       :size            :paragraph-2
       :number-of-lines 1
       :style           style/message-author-text}
      (format-reply-author from contact-name current-public-key)]]))

(defn quoted-message
  [{:keys [from content-type contentType parsed-text content deleted? deleted-for-me?
           album-images-count]}
   in-chat-input? pin? recording-audio?]
  (let [contact-name       (rf/sub [:contacts/contact-name-by-identity from])
        current-public-key (rf/sub [:multiaccount/public-key])
        content-type       (or content-type contentType)]
    [rn/view
     {:style               (style/container pin? in-chat-input?)
      :accessibility-label :reply-message}
     [rn/view {:style (style/reply-content pin?)}
      (when-not pin?
        [quo/icon :i/connector
         {:size            16
          :color           (colors/theme-colors colors/neutral-40 colors/neutral-60)
          :container-style {:position :absolute :left 0 :bottom -4 :width 16 :height 16}}])
      (if (or deleted? deleted-for-me?)
        [rn/view {:style (style/quoted-message pin?)}
         [reply-deleted-message]]
        [rn/view {:style (style/quoted-message pin?)}
         [reply-from
          {:from               from
           :contact-name       contact-name
           :current-public-key current-public-key}]
         [quo/text
          {:number-of-lines     1
           :size                :label
           :weight              :regular
           :accessibility-label :quoted-message
           :ellipsize-mode      :tail
           :style               (merge
                                 style/message-text
                                 (when (or (= constant/content-type-image content-type)
                                           (= constant/content-type-sticker content-type)
                                           (= constant/content-type-audio content-type))
                                   {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}))}
          (case (or content-type contentType)
            constant/content-type-image   (if album-images-count
                                            (i18n/label :t/images-albums-count
                                                        {:album-images-count album-images-count})
                                            (i18n/label :t/image))
            constant/content-type-sticker (i18n/label :t/sticker)
            constant/content-type-audio   (i18n/label :t/audio)
            (get-quoted-text-with-mentions (or parsed-text (:parsed-text content))))]])]
     (when (and in-chat-input? (not recording-audio?))
       [quo/button
        {:width               24
         :size                24
         :accessibility-label :reply-cancel-button
         :on-press            #(rf/dispatch [:chat.ui/cancel-message-reply])
         :type                :outline}
        [quo/icon :i/close
         {:size  16
          :color (colors/theme-colors colors/neutral-100 colors/neutral-40)}]])
     (when (and in-chat-input? recording-audio?)
       [linear-gradient/linear-gradient
        {:colors [(colors/theme-colors colors/white-opa-0 colors/neutral-90-opa-0)
                  (colors/theme-colors colors/white colors/neutral-90)]
         :start  {:x 0 :y 0}
         :end    {:x 0.7 :y 0}
         :style  style/gradient}])]))

(defn- f-view
  [recording?]
  (let [reply  (rf/sub [:chats/reply-message])
        height (reanimated/use-shared-value (if reply constants/reply-container-height 0))]
    (rn/use-effect #(reanimated/animate height (if reply constants/reply-container-height 0)) [reply])
    [reanimated/view {:style (reanimated/apply-animations-to-style {:height height} {})}
     (when reply [quoted-message reply true false recording?])]))

(defn view
  [{:keys [recording?]}]
  [:f> f-view @recording?])
