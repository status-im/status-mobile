(ns status-im2.contexts.chat.home.chat-list-item.view
  (:require [clojure.string :as string]
            [quo2.components.icon :as icons]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.common.home.actions.view :as actions]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.home.chat-list-item.style :as style]
            [utils.datetime :as datetime]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def max-subheader-length 50)

(defn open-chat
  [chat-id]
  (fn []
    (rf/dispatch [:dismiss-keyboard])
    (debounce/dispatch-and-chill [:chat/navigate-to-chat chat-id] 500)))

(defn parsed-text-to-one-line
  [parsed-text]
  (reduce
   (fn [acc {:keys [type literal children destination]}]
     (case type
       "paragraph"
       (str acc (parsed-text-to-one-line children) " ")

       "mention"
       (str acc "@" (rf/sub [:messages/resolve-mention literal]))

       "status-tag"
       (str acc literal)

       "link"
       (str acc destination)

       (str acc (string/replace literal #"\n" " "))))
   ""
   parsed-text))

(defn extract-text-from-message
  [{:keys [content]}]
  (let [{:keys [parsed-text text]} content]
    (if parsed-text
      (parsed-text-to-one-line parsed-text)
      (if text
        (string/replace text #"\n" " ")
        text))))

(defn preview-text-from-content
  [group-chat primary-name {:keys [content-type album-images-count content outgoing] :as message}]
  (let [content-text (extract-text-from-message message)
        reply? (not (string/blank? (:response-to content)))
        author (if outgoing
                 :you
                 (if group-chat
                   :other-person
                   :dont-show))
        preview-text
        (case content-type
          constants/content-type-text
          (if reply?
            (case author
              :you          (str (i18n/label :t/you-replied) ": " content-text)
              :other-person (str (i18n/label :t/user-replied {:user primary-name}) ": " content-text)
              :dont-show    (str (i18n/label :t/replied) ": " content-text)
              (str (i18n/label :t/replied) ": " content-text))
            (case author
              :you          (str (i18n/label :t/You) ": " content-text)
              :other-person (str primary-name ": " content-text)
              :dont-show    content-text
              content-text))

          constants/content-type-emoji
          (case author
            :you          (str (i18n/label :t/You) ": " content-text)
            :other-person (str primary-name ": " content-text)
            :dont-show    content-text
            content-text)

          constants/content-type-system-text
          (case author
            :you          (i18n/label :t/you-pinned-a-message)
            :other-person (i18n/label :t/user-pinned-a-message {:user primary-name})
            :dont-show    (i18n/label :t/Pinned-a-message)
            (i18n/label :t/Pinned-a-message))

          (constants/content-type-contact-request
           constants/content-type-system-message-mutual-event-removed
           constants/content-type-system-message-mutual-event-accepted)
          (i18n/label :t/contact-request)

          constants/content-type-sticker
          (case author
            :you          (i18n/label :t/you-sent-a-sticker)
            :other-person (i18n/label :t/user-sent-a-sticker {:user primary-name})
            :dont-show    (i18n/label :t/sent-a-sticker)
            (i18n/label :t/sent-a-sticker))

          constants/content-type-image
          (let [sent-photos (if album-images-count
                              (case author
                                :you          (i18n/label :t/you-sent-n-photos
                                                          {:number album-images-count})
                                :other-person (i18n/label :t/user-sent-n-photos
                                                          {:number album-images-count
                                                           :user   primary-name})
                                :dont-show    (i18n/label :t/sent-n-photos {:number album-images-count})
                                (i18n/label :t/sent-n-photos {:number album-images-count}))
                              (case author
                                :you          (i18n/label :t/you-sent-a-photo)
                                :other-person (i18n/label :t/user-sent-a-photo {:user primary-name})
                                :dont-show    (i18n/label :t/sent-a-photo)
                                (i18n/label :t/sent-a-photo)))]
            (if (not (string/blank? content-text))
              (str sent-photos ": " content-text)
              sent-photos))

          constants/content-type-audio
          (case author
            :you          (i18n/label :t/you-sent-audio-message)
            :other-person (i18n/label :t/user-sent-audio-message {:user primary-name})
            :dont-show    (i18n/label :t/sent-audio-message)
            (i18n/label :t/sent-audio-message))

          constants/content-type-gif
          (case author
            :you          (i18n/label :t/you-sent-a-gif)
            :other-person (i18n/label :t/user-sent-audio-message {:user primary-name})
            :dont-show    (i18n/label :t/sent-a-gif)
            (i18n/label :t/sent-a-gif))

          constants/content-type-community
          (case author
            :you          (i18n/label :t/you-shared-a-community)
            :other-person (i18n/label :t/user-shared-a-community {:user primary-name})
            :dont-show    (i18n/label :t/shared-a-community)
            (i18n/label :t/shared-a-community))

          "")]
    (subs preview-text 0 (min (count preview-text) max-subheader-length))))


(defn last-message-preview
  "Render the preview of a last message to a maximum of max-subheader-length characters"
  [group-chat {:keys [deleted? outgoing from deleted-for-me?] :as message}]
  (let [[primary-name _] (rf/sub [:contacts/contact-two-names-by-identity from])
        preview-text     (if deleted-for-me?
                           (i18n/label :t/you-deleted-a-message)
                           (if deleted?
                             (if outgoing
                               (i18n/label :t/you-deleted-a-message)
                               (if group-chat
                                 (i18n/label :t/user-deleted-a-message {:user primary-name})
                                 (i18n/label :t/this-message-was-deleted)))
                             (preview-text-from-content group-chat primary-name message)))]
    [quo/text
     {:size                :paragraph-2
      :style               {:color (colors/theme-colors colors/neutral-50
                                                        colors/neutral-40)
                            :flex  1}
      :number-of-lines     1
      :ellipsize-mode      :tail
      :accessibility-label :chat-message-text}
     preview-text]))

(defn avatar-view
  [{:keys [contact chat-id full-name color muted?]}]
  (if contact ; `contact` is passed when it's not a group chat
    (let [online?    (rf/sub [:visibility-status-updates/online? chat-id])
          photo-path (rf/sub [:chats/photo-path chat-id])]
      [quo/user-avatar
       (cond-> {:full-name       full-name
                :size            :small
                :online?         online?
                :profile-picture photo-path}
         muted?
         (assoc :ring? false))])
    [quo/group-avatar
     {:customization-color color
      :size                :size-32}]))

(defn notification
  [{:keys [muted group-chat unviewed-messages-count unviewed-mentions-count]}]
  (let [customization-color (rf/sub [:profile/customization-color])
        unread-messages?    (pos? unviewed-messages-count)
        unread-mentions?    (pos? unviewed-mentions-count)]
    [rn/view {:style style/notification-container}
     (cond
       muted
       [icons/icon :i/muted {:color colors/neutral-40}]

       (and group-chat unread-mentions?)
       [quo/counter
        {:container-style     {:position :relative :right 0}
         :customization-color customization-color
         :accessibility-label :new-message-counter}
        unviewed-mentions-count]

       ;; TODO: use the grey-dot component when chat-list-item is moved to quo2.components
       (and group-chat unread-messages?)
       [rn/view
        {:style               (style/grey-dot)
         :accessibility-label :unviewed-messages-public}]

       unread-messages?
       [quo/counter
        {:container-style     {:position :relative :right 0}
         :customization-color customization-color
         :accessibility-label :new-message-counter}
        unviewed-messages-count])]))

(defn chat-list-item
  [{:keys [chat-id group-chat color name last-message timestamp muted]
    :as   item}]
  (let [[primary-name secondary-name]
        (if group-chat
          [name ""]
          (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
        {:keys [ens-verified added?] :as contact} (when-not group-chat
                                                    (rf/sub [:contacts/contact-by-address chat-id]))]
    [rn/touchable-opacity
     {:style         (style/container)
      :on-press      (open-chat chat-id)
      :on-long-press #(rf/dispatch [:show-bottom-sheet
                                    {:content (fn [] [actions/chat-actions item false])}])}
     [avatar-view
      {:contact   contact
       :chat-id   chat-id
       :full-name primary-name
       :color     color
       :muted?    muted}]
     [rn/view {:style style/chat-data-container}
      [quo/author
       {:primary-name   primary-name
        :secondary-name secondary-name
        :size           15
        :verified?      ens-verified
        :contact?       added?
        :muted?         muted
        :time-str       (datetime/to-short-str timestamp)}]
      [last-message-preview group-chat last-message muted]]
     [notification item]]))
