(ns status-im.ui2.screens.chat.messages.message
  (:require
    [quo.design-system.colors :as quo.colors]
    [quo.react-native :as rn]
    [quo2.components.icon :as icons]
    [quo2.components.markdown.text :as text]
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [quo2.foundations.typography :as typography]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im.react-native.resources :as resources]
    [status-im.ui.components.fast-image :as fast-image]
    [status-im.ui.screens.chat.image.preview.views :as preview]
    [status-im.ui.screens.chat.message.audio :as message.audio]
    [status-im.ui.screens.chat.message.gap :as message.gap]
    [status-im.ui.screens.chat.sheets :as sheets]
    [status-im.ui.screens.chat.styles.message.message :as style]
    [status-im.ui.screens.chat.utils :as chat.utils]
    [status-im.ui.screens.communities.icon :as communities.icon]
    [status-im.ui2.screens.chat.components.reply.view :as components.reply]
    [status-im.utils.utils :as utils]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.home.chat-list-item.view :as home.chat-list-item]
    [status-im2.contexts.chat.messages.delete-message-for-me.events]
    [status-im2.contexts.chat.messages.delete-message.events]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn system-text?
  [content-type]
  (= content-type constants/content-type-system-text))

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
           {:style    {:color                :blue
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
             {:style    {:color                :blue
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
  (let [community-id                  (rf/sub [:community-id-by-chat-id chat-id])
        _ (prn community-id)]
    (reduce (fn [acc e]
              (render-block message-data
                            acc
                            e
                            community-id))
            [:<>]
            (:parsed-text content))))

(defn quoted-message
  [{:keys [message-id chat-id]} pin?]
  (let [quoted-message (get @(re-frame/subscribe [:chats/chat-messages chat-id])
                            message-id)]
    [rn/view {:style (when-not pin? (style/quoted-message-container))}
     [components.reply/reply-message quoted-message false pin?]]))

(defn message-not-sent-text
  [chat-id message-id]
  [rn/touchable-opacity
   {:on-press
    (fn []
      (re-frame/dispatch
       [:bottom-sheet/show-sheet
        {:content        (sheets/options chat-id message-id)
         :content-height 200}])
      (rn/dismiss-keyboard!))}
   [rn/view style/not-sent-view
    [rn/text {:style style/not-sent-text}
     (i18n/label :t/status-not-sent-tap)]
    [rn/view style/not-sent-icon
     [icons/icon :i/warning {:color quo.colors/red}]]]])

;; TODO (Omar): a reminder to clean these defviews
(defview message-author-name
  [from opts max-length]
  (letsubs [contact-with-names [:contacts/contact-by-identity from]]
    (chat.utils/format-author contact-with-names opts max-length)))

(defn display-name-view
  [display-name contact timestamp show-key?]
  [rn/view {:style {:flex-direction :row}}
   [text/text
    {:weight          :semi-bold
     :size            :paragraph-2
     :number-of-lines 1
     :style           {:width "45%"}}
    display-name]
   [home.chat-list-item/verified-or-contact-icon contact]
   (when show-key?
     (let [props {:size  :label
                  :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}]
       [rn/view
        {:style {:margin-left    8
                 :margin-top     2
                 :flex-direction :row}}
        [text/text
         (assoc props :accessibility-label :message-chat-key)
         (utils/get-shortened-address
          (or (:compressed-key contact)
              (:public-key contact)))]
        [text/text props " • "]
        [text/text
         (assoc props :accessibility-label :message-timestamp)
         (datetime/to-short-str timestamp)]]))])

(def image-max-width 260)
(def image-max-height 192)

(defn image-set-size
  [dimensions]
  (fn [^js evt]
    (let [width  (.-width (.-nativeEvent evt))
          height (.-height (.-nativeEvent evt))]
      (if (< width height)
        ;; if width less than the height we reduce width proportionally to height
        (let [k (/ height image-max-height)]
          (when (not= (/ width k) (first @dimensions))
            (reset! dimensions {:width (/ width k) :height image-max-height :loaded true})))
        (swap! dimensions assoc :loaded true)))))

(defmulti ->message :content-type)

(defmethod ->message constants/content-type-gap
  [message]
  [message.gap/gap message])

(defn pin-message
  [{:keys [chat-id pinned] :as message}]
  (let [pinned-messages @(re-frame/subscribe [:chats/pinned chat-id])]
    (if (and (not pinned) (> (count pinned-messages) 2))
      (do
        (js/setTimeout (fn [] (re-frame/dispatch [:dismiss-keyboard])) 500)
        (re-frame/dispatch [:pin-message/show-pin-limit-modal chat-id]))
      (re-frame/dispatch [:pin-message/send-pin-message (assoc message :pinned (not pinned))]))))

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

;;IMAGE
(defn message-content-image
  [{:keys [content]}]
  (let [dimensions (reagent/atom {:width image-max-width :height image-max-height :loaded false})
        visible    (reagent/atom false)
        uri        (:image content)]
    (fn [message]
      (let [style-opts {:outgoing false
                        :opacity  (if (:loaded @dimensions) 1 0)
                        :width    (:width @dimensions)
                        :height   (:height @dimensions)}]
        [:<>
         [preview/preview-image
          {:message  message
           :visible  @visible
           :on-close #(do (reset! visible false)
                          (reagent/flush))}]
         [rn/view
          {:style               (style/image-message style-opts)
           :accessibility-label :image-message}
          (when (or (:error @dimensions) (not (:loaded @dimensions)))
            [rn/view
             (merge (dissoc style-opts :opacity)
                    {:flex 1 :align-items :center :justify-content :center :position :absolute})
             (if (:error @dimensions)
               [icons/icon :i/cancel]
               [rn/activity-indicator {:animating true}])])
          [fast-image/fast-image
           {:style    (dissoc style-opts :outgoing)
            :on-load  (image-set-size dimensions)
            :on-error #(swap! dimensions assoc :error true)
            :source   {:uri uri}}]
          [rn/view {:style (style/image-message-border style-opts)}]]]))))

;; AUDIO
(defn audio
  [message]
  [rn/view {:style (style/message-view message) :accessibility-label :audio-message}
   [rn/view {:style (style/message-view-content)}
    [message.audio/message-content message]]])

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
   (case state
     constants/contact-request-message-state-pending  [contact-request-status-pending]
     constants/contact-request-message-state-accepted [contact-request-status-accepted]
     constants/contact-request-message-state-declined [contact-request-status-declined])])

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
  (letsubs [{:keys [name description verified] :as community} [:communities/community community-id]
            communities-enabled?                              [:communities/enabled?]]
    (when (and communities-enabled? community)
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
          :padding-left 14}
         (if (= community-id constants/status-community-id)
           [rn/image
            {:source (resources/get-image :status-logo)
             :style  {:width  40
                      :height 40}}]
           [communities.icon/community-icon community])]
        [rn/view {:padding-right 14 :flex 1}
         [rn/text {:style {:font-weight "700" :font-size 17}}
          name]
         [rn/text description]]]
       [rn/view (style/community-view-button)
        [rn/touchable-opacity
         {:on-press #(re-frame/dispatch
                      [:communities/navigate-to-community
                       {:community-id (:id community)}])}
         [rn/text
          {:style {:text-align :center
                   :color      quo.colors/blue}} (i18n/label :t/view)]]]])))

;; COMMUNITY (like system ? ) no wrapper
(defn community
  [message]
  [community-content message])
