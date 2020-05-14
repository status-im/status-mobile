(ns status-im.ui.screens.home.views.inner-item
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.ui.components.badge :as badge]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.sheets :as sheets]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.utils.contenthash :as contenthash]
            [status-im.utils.core :as utils]
            [status-im.utils.datetime :as time])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview mention-element [from]
  (letsubs [{:keys [ens-name alias]} [:contacts/contact-name-by-identity from]]
    (if ens-name (str "@" ens-name) alias)))

(defn render-subheader-inline [acc {:keys [type destination literal children]}]
  (case type
    "paragraph"
    (conj acc (reduce
               (fn [acc e] (render-subheader-inline acc e))
               [react/text-class " "]
               children))

    "blockquote"
    (conj acc (.substring literal 0 (dec (.-length literal))))

    "codeblock"
    (conj acc (.substring literal 0 (dec (.-length literal))))

    "mention"
    (conj acc [react/text-class
               [mention-element literal]])

    "status-tag"
    (conj acc [react/text-class
               "#"
               literal])

    "link"
    (conj acc destination)

    (conj acc literal)))

(def max-length 40)

(defn render-subheader
  "Render the chat subheader markdown inline, to a maximum of max-length characters"
  [parsed-text]
  (:elements
   (reduce
    (fn [{:keys [elements l] :as acc} {:keys [literal] :as e}]
      (if (> l max-length)
        (reduced acc)
        {:elements (render-subheader-inline elements e)
         :l (+ l (count literal))}))
    {:length 0
     :elements
     [react/text-class {:style               styles/last-message-text
                        :number-of-lines     1
                        :ellipsize-mode      :tail
                        :accessibility-label :chat-message-text}]}
    parsed-text)))

(defn message-content-text [{:keys [content content-type]}]
  [react/view styles/last-message-container
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

     (string/blank? (:text content))
     [react/text {:style styles/last-message-text}
      ""]

     (:text content)
     (render-subheader (:parsed-text content)))])

(defn message-timestamp [timestamp]
  (when timestamp
    [react/text {:style               styles/datetime-text
                 :accessibility-label :last-message-time-text}
     ;;TODO (perf) move to event
     (string/upper-case (time/to-short-str timestamp))]))

(defn unviewed-indicator [{:keys [unviewed-messages-count public?]}]
  (when (pos? unviewed-messages-count)
    (if public?
      [react/view {:style               styles/public-unread
                   :accessibility-label :unviewed-messages-public}]
      [badge/message-counter unviewed-messages-count])))

(defn home-list-item [[_ home-item]]
  (let [{:keys [chat-id chat-name color online group-chat
                public? contact timestamp last-message]}
        home-item
        private-group?      (and group-chat (not public?))
        public-group?       (and group-chat public?)
        ;;TODO (perf) move to event
        truncated-chat-name (utils/truncate-str chat-name 30)]
    [list-item/list-item
     {:icon                      [chat-icon.screen/chat-icon-view-chat-list
                                  contact group-chat truncated-chat-name color online false]
      :title-prefix              (cond
                                   private-group? :main-icons/tiny-group
                                   public-group? :main-icons/tiny-public
                                   :else nil)
      :title                     truncated-chat-name
      :title-accessibility-label :chat-name-text
      :title-row-accessory       [message-timestamp (if (pos? (:whisper-timestamp last-message))
                                                      (:whisper-timestamp last-message)
                                                      timestamp)]
      :subtitle [message-content-text {:content      (:content last-message)
                                       :content-type (:content-type last-message)}]
      :subtitle-row-accessory    [unviewed-indicator home-item]
      :on-press                  #(do
                                    (re-frame/dispatch [:dismiss-keyboard])
                                    (re-frame/dispatch [:chat.ui/navigate-to-chat chat-id])
                                    (if public?
                                      (re-frame/dispatch [:chat.ui/mark-public-all-read chat-id])
                                      (re-frame/dispatch [:chat.ui/mark-messages-seen :chat])))
      :on-long-press             #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                      {:content (fn []
                                                                  [sheets/actions home-item])
                                                       :height  256}])}]))
