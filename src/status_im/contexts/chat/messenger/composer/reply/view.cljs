(ns status-im.contexts.chat.messenger.composer.reply.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.reanimated :as reanimated]
    [status-im.constants :as constant]
    [status-im.contexts.chat.messenger.composer.constants :as constants]
    [status-im.contexts.chat.messenger.composer.reply.style :as style]
    [status-im.contexts.chat.messenger.composer.utils :as utils]
    [utils.ens.stateofus :as stateofus]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn get-quoted-text-with-mentions
  [parsed-text]
  (string/join
   (mapv (fn [{:keys [type literal children]}]
           (cond
             (= type "paragraph")
             (get-quoted-text-with-mentions children)

             (= type "mention")
             (rf/sub [:messages/resolve-mention literal])

             (= type "status-tag")
             (str "#" literal)

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
  [{:keys [from contact-name current-public-key pin?]}]
  (let [[primary-name _] (rf/sub [:contacts/contact-two-names-by-identity from])
        photo-path       (rf/sub [:chats/photo-path from])]
    [rn/view {:style style/reply-from}
     [quo/user-avatar
      {:full-name         primary-name
       :profile-picture   photo-path
       :status-indicator? false
       :size              :xxxs
       :ring?             false}]
     [quo/text
      {:weight          :semi-bold
       :size            (if pin? :label :paragraph-2)
       :number-of-lines 1
       :style           style/message-author-text}
      (format-reply-author from contact-name current-public-key)]]))

(defn quoted-message
  [{:keys [from content-type contentType parsed-text content deleted? deleted-for-me?
           album-images-count]}
   in-chat-input? pin? recording-audio? input-ref]
  (let [[primary-name _]   (rf/sub [:contacts/contact-two-names-by-identity from])
        current-public-key (rf/sub [:multiaccount/public-key])
        theme              (quo.theme/use-theme)
        content-type       (or content-type contentType)
        text               (get-quoted-text-with-mentions (or parsed-text (:parsed-text content)))]
    [rn/view
     {:style               (style/container pin? in-chat-input?)
      :accessibility-label :reply-message}
     [rn/view {:style (style/reply-content pin?)}
      (when-not pin?
        [quo/icon :i/connector
         {:size            16
          :color           (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)
          :container-style {:position :absolute :left 0 :bottom -4 :width 16 :height 16}}])
      (if (or deleted? deleted-for-me?)
        [rn/view {:style (style/quoted-message pin?)}
         [reply-deleted-message]]
        [rn/view {:style (style/quoted-message pin?)}
         [reply-from
          {:pin?               pin?
           :from               from
           :contact-name       primary-name
           :current-public-key current-public-key}]
         (when (not-empty text)
           [quo/text
            {:number-of-lines     1
             :size                :label
             :weight              :regular
             :accessibility-label :quoted-message
             :ellipsize-mode      :tail
             :style               style/message-text}
            text])
         [quo/text
          {:size   :label
           :weight :regular
           :style  {:color      (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
                    :margin-top 2}}
          (str " "
               (condp = (or content-type contentType)
                 constant/content-type-image   (if (pos? album-images-count)
                                                 (i18n/label :t/album-images-count
                                                             {:album-images-count album-images-count})
                                                 (i18n/label :t/photo))
                 constant/content-type-sticker (i18n/label :t/sticker)
                 constant/content-type-audio   (i18n/label :t/audio)
                 ""))]])]
     (when (and in-chat-input? (not recording-audio?))
       [quo/button
        {:icon-only?          true
         :size                24
         :accessibility-label :reply-cancel-button
         :on-press            #(utils/cancel-reply-message input-ref)
         :type                :outline}
        :i/close])
     (when (and in-chat-input? recording-audio?)
       [linear-gradient/linear-gradient
        {:colors [(colors/theme-colors colors/white-opa-0 colors/neutral-90-opa-0 theme)
                  (colors/theme-colors colors/white colors/neutral-90 theme)]
         :start  {:x 0 :y 0}
         :end    {:x 0.7 :y 0}
         :style  style/gradient}])]))

(defn- f-view
  [recording? input-ref]
  (let [reply  (rf/sub [:chats/reply-message])
        height (reanimated/use-shared-value (if reply constants/reply-container-height 0))]
    (rn/use-effect #(reanimated/animate height (if reply constants/reply-container-height 0)) [reply])
    [reanimated/view {:style (reanimated/apply-animations-to-style {:height height} {})}
     (when reply [quoted-message reply true false recording? input-ref])]))

(defn view
  [{:keys [recording?]} input-ref]
  [:f> f-view @recording? input-ref])
