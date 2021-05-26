(ns status-im.ui.screens.home.views.inner-item
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.badge :as badge]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [quo.core :as quo]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.utils.contenthash :as contenthash]
            [status-im.utils.core :as utils]
            [status-im.utils.datetime :as time]
            [status-im.ui.components.chat-icon.styles :as chat-icon.styles]))

(defn mention-element [from]
  @(re-frame/subscribe [:contacts/contact-name-by-identity from]))

;; if truncated subheader text is too short we won't get ellipsize at the end of text
(def max-subheader-length 100)

(defn truncate-literal [literal]
  (let [size (min max-subheader-length (.-length literal))]
    {:components (.substring literal 0 size)
     :length     size}))

(defn add-parsed-to-subheader [acc {:keys [type destination literal children]}]
  (let [result (case type
                 "paragraph"
                 (reduce
                  (fn [{:keys [_ length] :as acc-paragraph} parsed-child]
                    (if (>= length max-subheader-length)
                      (reduced acc-paragraph)
                      (add-parsed-to-subheader acc-paragraph parsed-child)))
                  {:components [react/text-class]
                   :length     0}
                  children)

                 "mention"
                 {:components [react/text-class [mention-element literal]]
                  :length     4}                            ;; we can't predict name length so take the smallest possible

                 "status-tag"
                 (truncate-literal (str "#" literal))

                 "link"
                 (truncate-literal destination)

                 (truncate-literal literal))]
    {:components (conj (:components acc) (:components result))
     :length     (+ (:length acc) (:length result))}))

(def subheader-wrapper
  [react/text-class {:style               styles/last-message-text
                     :number-of-lines     1
                     :ellipsize-mode      :tail
                     :accessibility-label :chat-message-text}])

(defn render-subheader
  "Render the preview of a last message to a maximum of max-subheader-length characters"
  [parsed-text]
  (let [result
        (reduce
         (fn [{:keys [_ length] :as acc-text} new-text-chunk]
           (if (>= length max-subheader-length)
             (reduced acc-text)
             (add-parsed-to-subheader acc-text new-text-chunk)))
         {:components subheader-wrapper
          :length     0}
         parsed-text)]
    (:components result)))

(defn message-content-text [{:keys [content content-type community-id]}]
  [react/view {:position :absolute :left 72 :top 32 :right 80}
   (cond

     (not (and content content-type))
     [react/text {:style               styles/last-message-text
                  :accessibility-label :no-messages-text}
      (i18n/label :t/no-messages)]

     (= constants/content-type-sticker content-type)
     [react/image {:style  {:margin 1 :width 20 :height 20}
                   ;;TODO (perf) move to event
                   :source {:uri (contenthash/url (-> content :sticker :hash))}}]

     (= constants/content-type-image content-type)
     [react/text {:style               styles/last-message-text
                  :accessibility-label :no-messages-text}
      (i18n/label :t/image)]

     (= constants/content-type-audio content-type)
     [react/text {:style               styles/last-message-text
                  :accessibility-label :no-messages-text}
      (i18n/label :t/audio)]

     (= constants/content-type-community content-type)
     (let [{:keys [name]}
           @(re-frame/subscribe [:communities/community community-id])]
       [react/text {:style               styles/last-message-text
                    :accessibility-label :no-messages-text}
        (i18n/label :t/community-message-preview {:community-name name})])

     (string/blank? (:text content))
     [react/text {:style styles/last-message-text}
      ""]

     (:text content)
     (render-subheader (:parsed-text content)))])

(def memo-timestamp
  (memoize
   (fn [timestamp]
     (string/upper-case (time/to-short-str timestamp)))))

(defn unviewed-indicator [{:keys [unviewed-mentions-count
                                  unviewed-messages-count
                                  public?]}]
  (when (pos? unviewed-messages-count)
    [react/view {:position :absolute :right 16 :bottom 12}
     (cond
       (and public? (not (pos? unviewed-mentions-count)))
       [react/view {:style               styles/public-unread
                    :accessibility-label :unviewed-messages-public}]

       (and public? (pos? unviewed-mentions-count))
       [badge/message-counter unviewed-mentions-count]

       :else
       [badge/message-counter unviewed-messages-count])]))

(defn icon-style []
  {:color           colors/black
   :width           15
   :height          15
   :container-style {:top          13 :left 72
                     :position     :absolute
                     :width        15
                     :height       15
                     :margin-right 2}})

(defn chat-item-icon [muted private-group? public-group?]
  (cond
    muted
    [icons/icon :main-icons/tiny-muted (assoc (icon-style) :color colors/gray)]
    private-group?
    [icons/icon :main-icons/tiny-group (icon-style)]
    public-group?
    [icons/icon :main-icons/tiny-public (icon-style)]
    :else
    [icons/icon :main-icons/tiny-new-contact (icon-style)]))

(defn chat-item-title [chat-id muted group-chat chat-name]
  [quo/text {:weight              :medium
             :color               (when muted :secondary)
             :accessibility-label :chat-name-text
             :ellipsize-mode      :tail
             :number-of-lines     1
             :style               {:position :absolute :left 92 :top 10 :right 90}}
   (if group-chat
     (utils/truncate-str chat-name 30)
     ;; This looks a bit odd, but I would like only to subscribe
     ;; if it's a one-to-one. If wrapped in a component styling
     ;; won't be applied correctly.
     (first @(re-frame/subscribe [:contacts/contact-two-names-by-identity chat-id])))])

(defn home-list-item [home-item opts]
  (let [{:keys [chat-id chat-name color group-chat public? timestamp last-message muted]} home-item]
    [react/touchable-opacity (merge {:style {:height 64}} opts)
     [:<>
      [chat-item-icon muted (and group-chat (not public?)) (and group-chat public?)]
      [chat-icon.screen/chat-icon-view chat-id group-chat chat-name
       {:container              (assoc chat-icon.styles/container-chat-list
                                       :top 12 :left 16 :position :absolute)
        :size                   40
        :chat-icon              chat-icon.styles/chat-icon-chat-list
        :default-chat-icon      (chat-icon.styles/default-chat-icon-chat-list color)
        :default-chat-icon-text (chat-icon.styles/default-chat-icon-text 40)}]
      [chat-item-title chat-id muted group-chat chat-name]
      [react/text {:style               styles/datetime-text
                   :number-of-lines     1
                   :accessibility-label :last-message-time-text}
       ;;TODO (perf) move to event
       (memo-timestamp (if (pos? (:whisper-timestamp last-message))
                         (:whisper-timestamp last-message)
                         timestamp))]
      [message-content-text (select-keys last-message [:content :content-type :community-id])]
      [unviewed-indicator home-item]]]))
