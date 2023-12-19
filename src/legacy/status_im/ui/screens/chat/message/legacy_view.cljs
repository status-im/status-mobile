(ns legacy.status-im.ui.screens.chat.message.legacy-view
  (:require
    [legacy.status-im.react-native.resources :as resources]
    [legacy.status-im.ui.components.colors :as quo.colors]
    [legacy.status-im.ui.components.fast-image :as fast-image]
    [legacy.status-im.ui.screens.chat.message.legacy-style :as style]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.foundations.typography :as typography]
    [react-native.core :as rn]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.messages.delete-message-for-me.events]
    [status-im2.contexts.chat.messages.delete-message.events]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf])
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]]))

(defn system-text?
  [content-type]
  (or
   (= content-type constants/content-type-system-text)
   (= content-type constants/content-type-system-pinned-message)))

(defn mention-element
  [from]
  (rf/sub [:messages/resolve-mention from]))

(defn render-inline
  [_message-text content-type acc {:keys [type literal destination]}
   community-id]
  (case type
    ""
    (conj acc literal)

    "code"
    (conj acc [rn/text literal])

    "emph"
    (conj acc [rn/text (style/emph-style) literal])

    "strong"
    (conj acc [rn/text (style/strong-style) literal])

    "strong-emph"
    (conj acc [quo/text (style/strong-emph-style) literal])

    "del"
    (conj acc [rn/text (style/strikethrough-style) literal])

    "link"
    (conj acc
          [rn/text
           {:style    {:color                (colors/theme-colors colors/primary-50 colors/primary-60)
                       :text-decoration-line :underline}
            :on-press #(rf/dispatch [:browser.ui/message-link-pressed destination])}
           destination])

    "mention"
    (conj
     acc
     [rn/view
      {:style {:background-color colors/primary-50-opa-10 :border-radius 6 :padding-horizontal 3}}
      [rn/text
       {:style    (merge {:color (if (system-text? content-type) quo.colors/black colors/primary-50)}
                         (if (system-text? content-type) typography/font-regular typography/font-medium))
        :on-press (when-not (system-text? content-type)
                    #(rf/dispatch [:chat.ui/show-profile literal]))}
       [mention-element literal]]])
    "status-tag"
    (conj acc
          [rn/text
           (when community-id
             {:style    {:color                (colors/theme-colors colors/primary-50 colors/primary-60)
                         :text-decoration-line :underline}
              :on-press #(rf/dispatch [:communities/status-tag-pressed community-id literal])})
           "#"
           literal])

    "edited"
    (conj acc [rn/text (style/edited-style) (str " (" (i18n/label :t/edited) ")")])

    (conj acc literal)))

;; TEXT
(defn render-block
  [{:keys [content content-type edited-at in-popover?]} acc
   {:keys [type ^js literal children]}
   community-id]

  (case type

    "paragraph"
    (conj acc
          (reduce
           (fn [acc e]
             (render-inline (:text content)
                            content-type
                            acc
                            e
                            community-id))
           [rn/text (style/text-style content-type in-popover?)]
           (conj
            children
            (when edited-at
              {:type "edited"}))))

    "blockquote"
    (conj acc
          [rn/view (style/blockquote-style)
           [rn/text (style/blockquote-text-style)
            (.substring literal 0 (.-length literal))]])

    "codeblock"
    (conj acc
          [rn/view {:style style/codeblock-style}
           [rn/text (.substring literal 0 (dec (.-length literal)))]])

    acc))

(defn render-parsed-text
  [{:keys [content chat-id]
    :as   message-data}]
  (let [community-id (rf/sub [:community-id-by-chat-id chat-id])]
    (reduce (fn [acc e]
              (render-block message-data
                            acc
                            e
                            community-id))
            [:<>]
            (:parsed-text content))))

(defmulti ->message :content-type)

(defmethod ->message constants/content-type-gap
  [_]
  [rn/view])

;; STATUS ? whats that ?
(defmethod ->message constants/content-type-status
  [{:keys [content content-type]}]
  [rn/view style/status-container
   [rn/text {:style (style/status-text)}
    (reduce
     (fn [acc e] (render-inline (:text content) content-type acc e nil))
     [rn/text {:style (style/status-text)}]
     (-> content :parsed-text peek :children))]])

;; EMOJI
(defn emoji
  []
  (fn [{:keys [content] :as message}]
    [rn/view (style/message-view message)
     [rn/view {:style (style/message-view-content)}
      [rn/view {:style (style/style-message-text)}
       [rn/text {:style (style/emoji-message message)}
        (:text content)]]]]))

;; STICKER
(defn sticker
  [{:keys [content]}]
  [fast-image/fast-image
   {:style  {:margin 10 :width 140 :height 140}
    :source {:uri (str (-> content :sticker :url) "&download=true")}}])

(defn contact-request-status-pending
  []
  [rn/view {:style {:flex-direction :row}}
   [quo/text
    {:style  {:margin-right 5.27}
     :weight :medium
     :color  :secondary}
    (i18n/label :t/contact-request-pending)]
   [rn/activity-indicator
    {:animating true
     :size      :small
     :color     quo.colors/gray}]])

(defn contact-request-status-accepted
  []
  [quo/text
   {:style  {:color quo.colors/green}
    :weight :medium}
   (i18n/label :t/contact-request-accepted)])

(defn contact-request-status-declined
  []
  [quo/text
   {:style  {:color quo.colors/red}
    :weight :medium}
   (i18n/label :t/contact-request-declined)])

(defn contact-request-status-label
  [state]
  [rn/view {:style (style/contact-request-status-label state)}
   (condp = state
     constants/contact-request-message-state-pending  [contact-request-status-pending]
     constants/contact-request-message-state-accepted [contact-request-status-accepted]
     constants/contact-request-message-state-declined [contact-request-status-declined]
     nil)])

;;;; SYSTEM

;; CONTACT REQUEST (like system message ? ) no wrapper
(defn system-contact-request
  [message _]
  [rn/view {:style (style/content-type-contact-request)}
   [rn/image
    {:source (resources/get-image :hand-wave)
     :style  {:width  112
              :height 97}}]
   [quo/text
    {:style  {:margin-top 6}
     :weight :bold
     :size   :heading-2}
    (i18n/label :t/contact-request)]
   [rn/view {:style {:padding-horizontal 16}}
    [quo/text
     {:style {:margin-top    2
              :margin-bottom 14}}
     (get-in message [:content :text])]]
   [contact-request-status-label (:contact-request-state message)]])

(defview community-content
  [{:keys [community-id] :as message}]
  (letsubs [{:keys [name description verified] :as community} [:communities/community community-id]]
    (when community
      [rn/view
       {:style (assoc (style/message-wrapper message)
                      :margin-vertical 10
                      :margin-left     8
                      :width           271)}
       (when verified
         [rn/view (style/community-verified)
          [rn/text
           {:style {:font-size 13
                    :color     quo.colors/blue}} (i18n/label :t/communities-verified)]])
       [rn/view (style/community-message verified)
        [rn/view
         {:width        62
          :padding-left 14}]
        [rn/view {:padding-right 14 :flex 1}
         [rn/text {:style {:font-weight "700" :font-size 17 :color quo.colors/black}}
          name]
         [rn/text {:style {:color quo.colors/black}} description]]]
       [rn/view (style/community-view-button)
        [rn/touchable-opacity
         {:on-press #(do
                       (rf/dispatch [:pop-to-root :shell-stack])
                       (rf/dispatch [:navigate-to :community-overview (:id community)])
                       (rf/dispatch [:chat/close]))}
         [rn/text
          {:style {:text-align :center
                   :color      quo.colors/blue}} (i18n/label :t/view)]]]])))

;; COMMUNITY (like system ? ) no wrapper
(defn community
  [message]
  [community-content message])
