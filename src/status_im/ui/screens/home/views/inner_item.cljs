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
            [status-im.ui.screens.chat.sheets :as sheets]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.utils.contenthash :as contenthash]
            [status-im.utils.core :as utils]
            [status-im.utils.datetime :as time]))

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
                  :length 4} ;; we can't predict name length so take the smallest possible

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

(defn message-content-text [{:keys [content content-type]}]
  [:<>
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

     (string/blank? (:text content))
     [react/text {:style styles/last-message-text}
      ""]

     (:text content)
     (render-subheader (:parsed-text content)))])

(defn message-timestamp [timestamp]
  [react/view
   (when timestamp
     [react/text {:style               styles/datetime-text
                  :number-of-lines     1
                  :accessibility-label :last-message-time-text}
      ;;TODO (perf) move to event
      (string/upper-case (time/to-short-str timestamp))])])

(defn unviewed-indicator [{:keys [unviewed-messages-count public?]}]
  (when (pos? unviewed-messages-count)
    [react/view {:padding-left    16
                 :justify-content :flex-end
                 :align-items     :flex-end}
     (if public?
       [react/view {:style               styles/public-unread
                    :accessibility-label :unviewed-messages-public}]
       [badge/message-counter unviewed-messages-count])]))

(defn icon-style []
  {:color           colors/black
   :width           15
   :height          15
   :container-style {:width           15
                     :height          15
                     :margin-right   2}})

(defn home-list-item [home-item]
  (let [{:keys [chat-id chat-name color online group-chat
                public? timestamp last-message muted]}
        home-item
        private-group? (and group-chat (not public?))
        public-group?  (and group-chat public?)]
    [quo/list-item
     {:icon                      [chat-icon.screen/chat-icon-view-chat-list
                                  chat-id group-chat chat-name color online false]
      :title                     [react/view {:flex-direction :row
                                              :flex           1}
                                  [react/view {:flex-direction :row
                                               :flex           1
                                               :padding-right  16
                                               :align-items    :center}
                                   (cond
                                     muted
                                     [icons/icon :main-icons/tiny-muted (assoc (icon-style) :color colors/gray)]
                                     private-group?
                                     [icons/icon :main-icons/tiny-group (icon-style)]
                                     public-group?
                                     [icons/icon :main-icons/tiny-public (icon-style)]
                                     :else
                                     [icons/icon :main-icons/tiny-new-contact (icon-style)])
                                   [quo/text {:weight              :medium
                                              :color               (when muted :secondary)
                                              :accessibility-label :chat-name-text
                                              :ellipsize-mode      :tail
                                              :number-of-lines     1}
                                    (if group-chat
                                      (utils/truncate-str chat-name 30)
                                      ;; This looks a bit odd, but I would like only to subscribe
                                      ;; if it's a one-to-one. If wrapped in a component styling
                                      ;; won't be applied correctly.
                                      (first @(re-frame/subscribe [:contacts/contact-two-names-by-identity chat-id])))]]
                                  [message-timestamp (if (pos? (:whisper-timestamp last-message))
                                                       (:whisper-timestamp last-message)
                                                       timestamp)]]
      :title-accessibility-label :chat-name-text
      :subtitle                  [react/view {:flex-direction :row}
                                  [react/view {:flex 1}
                                   [message-content-text {:content      (:content last-message)
                                                          :content-type (:content-type last-message)}]]
                                  [unviewed-indicator home-item]]
      :on-press                  #(do
                                    (re-frame/dispatch [:dismiss-keyboard])
                                    (re-frame/dispatch [:chat.ui/navigate-to-chat chat-id])
                                    (re-frame/dispatch [:search/home-filter-changed nil])
                                    (if public?
                                      (re-frame/dispatch [:chat.ui/mark-public-all-read chat-id])
                                      (re-frame/dispatch [:chat.ui/mark-messages-seen :chat])))
      :on-long-press             #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                      {:content (fn []
                                                                  [sheets/actions home-item])}])}]))
