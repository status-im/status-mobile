(ns status-im.ui2.screens.chat.messages.message
  (:require [quo.core :as quo]
            [quo.design-system.colors :as quo.colors]
            [quo.react-native :as rn]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.messages.system-message :as system-message]
            [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im2.contexts.chat.messages.message.delete-message.events]
            [status-im2.contexts.chat.messages.message.delete-message-for-me.events]
            [status-im.chat.models.images :as images]
            [status-im.chat.models.reactions :as models.reactions]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.fast-image :as fast-image]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.image.preview.views :as preview]
            [status-im.ui.screens.chat.message.audio :as message.audio]
            [status-im.ui.screens.chat.message.command :as message.command]
            [status-im.ui.screens.chat.message.gap :as message.gap]
            [status-im.ui.screens.chat.message.link-preview :as link-preview]
            [status-im.ui.screens.chat.message.reactions :as reactions]
            [status-im.ui.screens.chat.message.reactions-row :as reaction-row]
            [status-im.ui.screens.chat.sheets :as sheets]
            [status-im.ui.screens.chat.styles.message.message :as style]
            [status-im.ui.screens.chat.styles.photos :as photos.style]
            [status-im.ui.screens.chat.utils :as chat.utils]
            [status-im.ui.screens.communities.icon :as communities.icon]
            [status-im.ui2.screens.chat.components.reaction-drawer :as reaction-drawer]
            [status-im.ui2.screens.chat.components.reply :as components.reply]
            [status-im.utils.config :as config]
            [status-im.utils.datetime :as time]
            [utils.security.core :as security]
            [status-im.utils.utils :as utils]
            [status-im2.contexts.chat.home.chat-list-item.view :as home.chat-list-item]
            [utils.re-frame :as rf])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview mention-element [from]
  (letsubs [contact-name [:contacts/contact-name-by-identity from]]
    contact-name))

(def edited-at-text (str " ⌫ " (i18n/label :t/edited)))

(defn message-status [{:keys [outgoing content outgoing-status pinned edited-at in-popover?]}]
  (when-not in-popover? ;; We keep track if showing this message in a list in pin-limit-popover
    [rn/view
     {:align-self                       :flex-end
      :position                         :absolute
      :bottom                           9 ; 6 Bubble bottom, 3 message baseline
      (if (:rtl? content) :left :right) 0
      :flex-direction                   :row
      :align-items                      :flex-end}
     (when outgoing
       [icons/icon (case outgoing-status
                     :sending :tiny-icons/tiny-pending
                     :sent :tiny-icons/tiny-sent
                     :not-sent :tiny-icons/tiny-warning
                     :delivered :tiny-icons/tiny-delivered
                     :tiny-icons/tiny-pending)
        {:width               16
         :height              12
         :color               (if pinned quo.colors/gray quo.colors/white)
         :accessibility-label (name outgoing-status)}])
     (when edited-at [rn/text {:style (style/message-status-text)} edited-at-text])]))

(defn message-timestamp
  [{:keys [timestamp-str in-popover?]}]
  (when-not in-popover? ;; We keep track if showing this message in a list in pin-limit-popover
    (let [anim-opacity (animation/create-value 0)]
      [rn/animated-view {:style (style/message-timestamp-wrapper) :opacity anim-opacity}
       [rn/text
        {:style               (style/message-timestamp-text)
         :accessibility-label :message-timestamp}
        timestamp-str]])))

(defn quoted-message
  [{:keys [message-id chat-id]} reply pin?]
  (let [{:keys [deleted? deleted-for-me?]} (get @(re-frame/subscribe [:chats/chat-messages chat-id]) message-id)
        reply                              (assoc reply :deleted? deleted? :deleted-for-me? deleted-for-me?)]
    [rn/view {:style (when-not pin? (style/quoted-message-container))}
     [components.reply/reply-message reply false pin?]]))

(defn system-text? [content-type]
  (= content-type constants/content-type-system-text))

(defn render-inline [message-text content-type acc {:keys [type literal destination]}]
  (case type
    ""
    (conj acc literal)

    "code"
    (conj acc [quo/text {:max-font-size-multiplier react/max-font-size-multiplier
                         :style                    (style/inline-code-style)
                         :monospace                true}
               literal])

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
           {:style
            {:color                quo.colors/blue
             :text-decoration-line :underline}
            :on-press
            #(when (and (security/safe-link? destination)
                        (security/safe-link-text? message-text))
               (re-frame/dispatch
                [:browser.ui/message-link-pressed destination]))}
           destination])

    "mention"
    (conj acc
          [rn/view {:style {:background-color colors/primary-50-opa-10 :border-radius 6 :padding-horizontal 3}}
           [rn/text
            {:style    (merge {:color (if (system-text? content-type) quo.colors/black colors/primary-50)}
                              (if (system-text? content-type) typography/font-regular typography/font-medium))
             :on-press (when-not (system-text? content-type)
                         #(rf/dispatch [:chat.ui/show-profile literal]))}
            [mention-element literal]]])
    "status-tag"
    (conj acc [rn/text
               {:style {:color                quo.colors/blue
                        :text-decoration-line :underline}
                :on-press
                #(re-frame/dispatch
                  [:chat.ui/start-public-chat literal])}
               "#"
               literal])

    (conj acc literal)))

(defn render-block [{:keys [content content-type in-popover?]} acc
                    {:keys [type ^js literal children]}]
  (case type

    "paragraph"
    (conj acc (reduce
               (fn [acc e] (render-inline (:text content) content-type acc e))
               [rn/text (style/text-style content-type in-popover?)]
               children))

    "blockquote"
    (conj acc [rn/view (style/blockquote-style)
               [rn/text (style/blockquote-text-style)
                (.substring literal 0 (.-length literal))]])

    "codeblock"
    (conj acc [rn/view {:style style/codeblock-style}
               [quo/text {:max-font-size-multiplier react/max-font-size-multiplier
                          :style                    style/codeblock-text-style
                          :monospace                true}
                (.substring literal 0 (dec (.-length literal)))]])

    acc))

(defn render-parsed-text [message tree]
  (reduce (fn [acc e] (render-block message acc e)) [:<>] tree))

(defn render-parsed-text-with-message-status [{:keys [edited-at in-popover?] :as message} tree]
  (let [elements       (render-parsed-text message tree)
        message-status [rn/text {:style (style/message-status-placeholder)}
                        (str (if (not in-popover?) "        " "  ") (when (and (not in-popover?) edited-at) edited-at-text))]
        last-element   (peek elements)]
    ;; Using `nth` here as slightly faster than `first`, roughly 30%
    ;; It's worth considering pure js structures for this code path as
    ;; it's perfomance critical
    (if (= rn/text (nth last-element 0))
      ;; Append message status to last text
      (conj (pop elements) (conj last-element message-status))
      ;; Append message status to new block
      (conj elements message-status))))

(defn unknown-content-type
  [{:keys [content-type content] :as message}]
  [rn/view (style/message-view message)
   [rn/text
    {:style {:color quo.colors/white-persist}}
    (if (seq (:text content))
      (:text content)
      (str "Unhandled content-type " content-type))]])

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

(defn pin-author-name [pinned-by]
  (let [user-contact  @(re-frame/subscribe [:multiaccount/contact])
        contact-names @(re-frame/subscribe [:contacts/contact-two-names-by-identity pinned-by])]
    ;; We append empty spaces to the name as a workaround to make one-line and multi-line label components show correctly
    (str " " (if (= pinned-by (user-contact :public-key)) (i18n/label :t/You) (first contact-names)))))

(defn pin-icon [color size]
  [icons/icon :i/pin {:color  color
                      :height size
                      :width  size}])

(defn pinned-by-indicator [pinned-by]
  [rn/view {:style               (style/pin-indicator)
            :accessibility-label :pinned-by}
   [pin-icon colors/primary-50 16]
   [quo/text {:size  :small
              :color :main
              :style (style/pin-author-text)}
    (pin-author-name pinned-by)]])

(defn message-delivery-status
  [{:keys [chat-id message-id outgoing-status message-type]}]
  (when (and (not= constants/message-type-private-group-system-message message-type)
             (= outgoing-status :not-sent))
    [message-not-sent-text chat-id message-id]))

;; TODO (Omar): a reminder to clean these defviews
(defview message-author-name [from opts max-length]
  (letsubs [contact-with-names [:contacts/contact-by-identity from]]
    (chat.utils/format-author contact-with-names opts max-length)))

(defview message-my-name [opts]
  (letsubs [contact-with-names [:multiaccount/contact]]
    (chat.utils/format-author contact-with-names opts nil)))

(defview community-content [{:keys [community-id] :as message}]
  (letsubs [{:keys [name description verified] :as community} [:communities/community community-id]
            communities-enabled?                              [:communities/enabled?]]
    (when (and communities-enabled? community)
      [rn/view {:style (assoc (style/message-wrapper message)
                              :margin-vertical 10
                              :margin-left 8
                              :width 271)}
       (when verified
         [rn/view (style/community-verified)
          [rn/text {:style {:font-size 13
                            :color     quo.colors/blue}} (i18n/label :t/communities-verified)]])
       [rn/view (style/community-message verified)
        [rn/view {:width        62
                  :padding-left 14}
         (if (= community-id constants/status-community-id)
           [rn/image {:source (resources/get-image :status-logo)
                      :style  {:width  40
                               :height 40}}]
           [communities.icon/community-icon community])]
        [rn/view {:padding-right 14 :flex 1}
         [rn/text {:style {:font-weight "700" :font-size 17}}
          name]
         [rn/text description]]]
       [rn/view (style/community-view-button)
        [rn/touchable-opacity {:on-press #(re-frame/dispatch
                                           [:communities/navigate-to-community
                                            {:community-id (:id community)}])}
         [rn/text {:style {:text-align :center
                           :color      quo.colors/blue}} (i18n/label :t/view)]]]])))

(defn display-name-view [display-name contact timestamp show-key?]
  [rn/view {:style {:flex-direction :row}}
   [text/text {:weight          :semi-bold
               :size            :paragraph-2
               :number-of-lines 1
               :style           {:width "45%"}}
    display-name]
   [home.chat-list-item/verified-or-contact-icon contact]
   (when show-key?
     (let [props {:size  :label
                  :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}]
       [rn/view {:style {:margin-left    8
                         :margin-top     2
                         :flex-direction :row}}
        [text/text
         (assoc props :accessibility-label :message-chat-key)
         (utils/get-shortened-address (:public-key contact))]
        [text/text props " • "]
        [text/text
         (assoc props :accessibility-label :message-timestamp)
         (time/to-short-str timestamp)]]))])

(defn message-content-wrapper
  "Author, userpic and delivery wrapper"
  [{:keys [last-in-group? timestamp-str timestamp deleted? deleted-undoable-till
           deleted-for-me? deleted-for-me-undoable-till pinned from chat-id]
    :as   message} content]
  (let [response-to  (:response-to (:content message))
        display-name (first (rf/sub [:contacts/contact-two-names-by-identity from]))
        contact      (rf/sub [:contacts/contact-by-address from])
        photo-path   (when-not (empty? (:images contact)) (rf/sub [:chats/photo-path from]))
        online?      (rf/sub [:visibility-status-updates/online? from])]
    (if (or deleted? deleted-for-me?)
      [system-message/system-message
       {:type             :deleted
        :label            (if deleted? :message-deleted :message-deleted-for-you)
        :labels           {:pinned-a-message        (i18n/label :pinned-a-message)
                           :message-deleted         (i18n/label :message-deleted-for-everyone)
                           :message-deleted-for-you (i18n/label :message-deleted-for-you)
                           :added                   (i18n/label :added)}
        :timestamp-str    timestamp-str
        :non-pressable?   true
        :animate-landing? (if (or deleted-undoable-till deleted-for-me-undoable-till)
                            true
                            false)}]
      [rn/view {:style               (style/message-wrapper message)
                :pointer-events      :box-none
                :accessibility-label :chat-item}
       (when (and (seq response-to) (:quoted-message message))
         [quoted-message {:message-id response-to :chat-id chat-id} (:quoted-message message)])
       [rn/view {:style          (style/message-body)
                 :pointer-events :box-none}
        [rn/view {:style {:width 40}}
         (when (or (and (seq response-to) (:quoted-message message)) last-in-group? pinned)
           [react/touchable-highlight {:on-press #(re-frame/dispatch [:chat.ui/show-profile from])}
            [user-avatar/user-avatar {:full-name         display-name
                                      :profile-picture   photo-path
                                      :status-indicator? true
                                      :online?           online?
                                      :size              :small
                                      :ring?             false}]])]
        [rn/view {:style (style/message-author-wrapper)}
         (when (or (and (seq response-to) (:quoted-message message)) last-in-group? pinned)
           [display-name-view display-name contact timestamp true])
         ;; MESSAGE CONTENT
         content
         [link-preview/link-preview-wrapper (:links (:content message)) false false]]]
       ;; delivery status
       [rn/view (style/delivery-status)
        [message-delivery-status message]]])))

(def image-max-width 260)
(def image-max-height 192)

(defn image-set-size [dimensions]
  (fn [evt]
    (let [width  (.-width (.-nativeEvent evt))
          height (.-height (.-nativeEvent evt))]
      (if (< width height)
        ;; if width less than the height we reduce width proportionally to height
        (let [k (/ height image-max-height)]
          (when (not= (/ width k) (first @dimensions))
            (reset! dimensions {:width (/ width k) :height image-max-height :loaded true})))
        (swap! dimensions assoc :loaded true)))))

(defn message-content-image
  [{:keys [content]} _]
  (let [dimensions (reagent/atom {:width image-max-width :height image-max-height :loaded false})
        visible    (reagent/atom false)
        uri        (:image content)]
    (fn [{:keys [in-popover?] :as message}
         {:keys [on-long-press]}]
      (let [style-opts {:outgoing false
                        :opacity  (if (:loaded @dimensions) 1 0)
                        :width    (:width @dimensions)
                        :height   (:height @dimensions)}]
        [:<>
         [preview/preview-image {:message  message
                                 :visible  @visible
                                 :on-close #(do (reset! visible false)
                                                (reagent/flush))}]
         [rn/touchable-opacity {:on-press      (fn []
                                                 (reset! visible true)
                                                 (rn/dismiss-keyboard!))
                                :on-long-press @on-long-press
                                :disabled      in-popover?}
          [rn/view {:style               (style/image-message style-opts)
                    :accessibility-label :image-message}
           (when (or (:error @dimensions) (not (:loaded @dimensions)))
             [rn/view
              (merge (dissoc style-opts :opacity)
                     {:flex 1 :align-items :center :justify-content :center :position :absolute})
              (if (:error @dimensions)
                [icons/icon :i/cancel]
                [rn/activity-indicator {:animating true}])])
           [fast-image/fast-image {:style    (dissoc style-opts :outgoing)
                                   :on-load  (image-set-size dimensions)
                                   :on-error #(swap! dimensions assoc :error true)
                                   :source   {:uri uri}}]
           [rn/view {:style (style/image-message-border style-opts)}]]]]))))

(defmulti ->message :content-type)

(defmethod ->message constants/content-type-command
  [message]
  [message.command/command-content message-content-wrapper message])

(defmethod ->message constants/content-type-gap
  [message]
  [message.gap/gap message])

(defn pin-message [{:keys [chat-id pinned] :as message}]
  (let [pinned-messages @(re-frame/subscribe [:chats/pinned chat-id])]
    (if (and (not pinned) (> (count pinned-messages) 2))
      (do
        (js/setTimeout (fn [] (re-frame/dispatch [:dismiss-keyboard])) 500)
        (re-frame/dispatch [:pin-message/show-pin-limit-modal chat-id]))
      (re-frame/dispatch [:pin-message/send-pin-message (assoc message :pinned (not pinned))]))))

(defn on-long-press-fn [on-long-press {:keys [pinned message-pin-enabled outgoing edit-enabled show-input? community? can-delete-message-for-everyone?] :as message} content]
  (on-long-press
   (concat
    (when (and outgoing edit-enabled)
      [{:type     :main
        :on-press #(re-frame/dispatch [:chat.ui/edit-message message])
        :label    (i18n/label :t/edit-message)
        :icon     :i/edit
        :id       :edit}])
    (when show-input?
      [{:type     :main
        :on-press #(re-frame/dispatch [:chat.ui/reply-to-message message])
        :label    (i18n/label :t/message-reply)
        :icon     :i/reply
        :id       :reply}])
    [{:type     :main
      :on-press #(react/copy-to-clipboard
                  (components.reply/get-quoted-text-with-mentions
                   (get content :parsed-text)))
      :label    (i18n/label :t/copy-text)
      :icon     :i/copy
      :id       :copy}]
    (when message-pin-enabled
      [{:type     :main
        :on-press #(pin-message message)
        :label    (i18n/label (if pinned (if community? :t/unpin-from-channel :t/unpin-from-chat) (if community? :t/pin-to-channel :t/pin-to-chat)))
        :icon     :i/pin
        :id       (if pinned :unpin :pin)}])
    (when-not pinned
      [{:type     :danger
        :on-press (fn []
                    (re-frame/dispatch
                     [:chat.ui/delete-message-for-me message
                      config/delete-message-for-me-undo-time-limit-ms]))
        :label    (i18n/label :t/delete-for-me)
        :icon     :i/delete
        :id       :delete-for-me}])
    (when (and (or outgoing can-delete-message-for-everyone?) config/delete-message-enabled?)
      [{:type     :danger
        :on-press #(re-frame/dispatch [:chat.ui/delete-message
                                       message
                                       config/delete-message-undo-time-limit-ms])
        :label    (i18n/label :t/delete-for-everyone)
        :icon     :i/delete
        :id       :delete-for-all}]))))

(defn collapsible-text-message [_ _]
  (let [collapsed?      (reagent/atom false)
        show-timestamp? (reagent/atom false)]
    (fn [{:keys [content in-popover?] :as message} on-long-press modal ref]
      (let [on-long-press (fn []
                            (if @collapsed?
                              (do (reset! collapsed? false)
                                  (js/setTimeout #(on-long-press-fn on-long-press message content) 200))
                              (on-long-press-fn on-long-press message content)))]
        (reset! ref on-long-press)
        [rn/touchable-opacity
         (when-not modal
           {:delay-long-press 100
            :on-long-press    on-long-press
            :disabled         in-popover?})
         [rn/view style/message-view-wrapper
          [message-timestamp message show-timestamp?]
          [rn/view {:style (style/message-view message)}
           [rn/view {:style (style/message-view-content)}
            [rn/view
             [render-parsed-text-with-message-status message (:parsed-text content)]]]]]]))))

(defmethod ->message constants/content-type-text
  [message {:keys [on-long-press modal ref] :as reaction-picker}]
  [message-content-wrapper message
   [collapsible-text-message message on-long-press modal ref]
   reaction-picker])

(defmethod ->message constants/content-type-community
  [message]
  [community-content message])

(defmethod ->message constants/content-type-status
  [{:keys [content content-type] :as message}]
  [message-content-wrapper message
   [rn/view style/status-container
    [rn/text {:style (style/status-text)}
     (reduce
      (fn [acc e] (render-inline (:text content) content-type acc e))
      [rn/text {:style (style/status-text)}]
      (-> content :parsed-text peek :children))]]])

(defmethod ->message constants/content-type-emoji []
  (let [show-timestamp? (reagent/atom false)]
    (fn [{:keys [content pinned in-popover? message-pin-enabled] :as message}
         {:keys [on-long-press modal ref]
          :as   reaction-picker}]
      (let [on-long-press (fn []
                            (on-long-press
                             (concat
                              [{:type     :main
                                :on-press #(re-frame/dispatch [:chat.ui/reply-to-message message])
                                :id       :reply
                                :icon     :i/reply
                                :label    (i18n/label :t/message-reply)}
                               {:type     :main
                                :on-press #(react/copy-to-clipboard (get content :text))
                                :id       :copy
                                :icon     :i/copy
                                :label    (i18n/label :t/copy-text)}]
                              (when message-pin-enabled [{:type     :main
                                                          :on-press #(pin-message message)
                                                          :id       :pin
                                                          :icon     :i/pin
                                                          :label    (if pinned (i18n/label :t/unpin) (i18n/label :t/pin))}]))))]
        (reset! ref on-long-press)
        [message-content-wrapper message
         [rn/touchable-opacity (when-not modal
                                 {:disabled         in-popover?
                                  :on-press         (fn []
                                                      (rn/dismiss-keyboard!)
                                                      (reset! show-timestamp? true))
                                  :delay-long-press 100
                                  :on-long-press    on-long-press})
          [rn/view style/message-view-wrapper
           [message-timestamp message show-timestamp?]
           [rn/view (style/message-view message)
            [rn/view {:style (style/message-view-content)}
             [rn/view {:style (style/style-message-text)}
              [rn/text {:style (style/emoji-message message)}
               (:text content)]]
             [message-status message]]]]]
         reaction-picker]))))

(defmethod ->message constants/content-type-sticker
  [{:keys [content from outgoing in-popover?]
    :as   message}
   {:keys [on-long-press modal ref]
    :as   reaction-picker}]
  (let [pack          (get-in content [:sticker :pack])
        on-long-press (fn []
                        (on-long-press
                         (when-not outgoing
                           [{:type     :main
                             :icon     :i/stickers
                             :on-press #(when pack
                                          (re-frame/dispatch [:chat.ui/show-profile from]))
                             :label    (i18n/label :t/see-sticker-set)}])))]
    (reset! ref on-long-press)
    [message-content-wrapper message
     [rn/touchable-opacity (when-not modal
                             {:disabled            in-popover?
                              :accessibility-label :sticker-message
                              :on-press            (fn [_]
                                                     (when pack
                                                       (re-frame/dispatch [:stickers/open-sticker-pack (str pack)]))
                                                     (rn/dismiss-keyboard!))
                              :delay-long-press    100
                              :on-long-press       on-long-press})
      [fast-image/fast-image {:style  {:margin 10 :width 140 :height 140}
                              :source {:uri (str (-> content :sticker :url) "&download=true")}}]]
     reaction-picker]))

(defmethod ->message constants/content-type-image
  [{:keys [content in-popover? outgoing] :as message}
   {:keys [on-long-press modal ref]
    :as   reaction-picker}]
  (let [on-long-press (fn []
                        (on-long-press
                         (concat [{:type     :main
                                   :on-press #(re-frame/dispatch [:chat.ui/reply-to-message message])
                                   :id       :reply
                                   :icon     :i/reply
                                   :label    (i18n/label :t/message-reply)}
                                  {:type     :main
                                   :on-press #(re-frame/dispatch [:chat.ui/save-image-to-gallery (:image content)])
                                   :id       :save
                                   :icon     :i/save
                                   :label    (i18n/label :t/save-image-library)}
                                  {:type     :main
                                   :on-press #(images/download-image-http
                                               (get-in message [:content :image]) preview/share)
                                   :id       :share
                                   :icon     :i/share
                                   :label    (i18n/label :t/share-image)}]
                                 [{:type     :danger
                                   :on-press #(re-frame/dispatch
                                               [:chat.ui/delete-message-for-me message
                                                config/delete-message-undo-time-limit-ms])
                                   :label    (i18n/label :t/delete-for-me)
                                   :icon     :i/delete
                                   :id       :delete-for-me}]
                                 (when (and outgoing config/delete-message-enabled?)
                                   [{:type     :danger
                                     :on-press #(re-frame/dispatch [:chat.ui/delete-message
                                                                    message
                                                                    config/delete-message-undo-time-limit-ms])
                                     :label    (i18n/label :t/delete-for-everyone)
                                     :icon     :i/delete
                                     :id       :delete}]))))]
    (reset! ref on-long-press)
    [message-content-wrapper message
     [message-content-image message
      {:modal            modal
       :disabled         in-popover?
       :delay-long-press 100
       :on-long-press    ref}]
     reaction-picker]))

(defmethod ->message constants/content-type-audio []
  (let [show-timestamp? (reagent/atom false)]
    (fn [{:keys [outgoing pinned] :as message}
         {:keys [on-long-press modal ref]
          :as   reaction-picker}]
      (let [on-long-press (fn [] (on-long-press [{:type     :main
                                                  :on-press #(re-frame/dispatch [:chat.ui/reply-to-message message])
                                                  :label    (i18n/label :t/message-reply)
                                                  :icon     :i/reply
                                                  :id       :reply}
                                                 {:type     :main
                                                  :on-press #(pin-message message)
                                                  :label    (i18n/label (if pinned :t/unpin-from-chat :t/pin-to-chat))
                                                  :icon     :i/pin
                                                  :id       (if pinned :unpin :pin)}
                                                 {:type     :danger
                                                  :on-press #(re-frame/dispatch
                                                              [:chat.ui/delete-message-for-me message
                                                               config/delete-message-for-me-undo-time-limit-ms])
                                                  :label    (i18n/label :t/delete-for-me)
                                                  :icon     :i/delete
                                                  :id       :delete-for-me}
                                                 (when (and outgoing config/delete-message-enabled?)
                                                   {:type     :danger
                                                    :on-press #(re-frame/dispatch [:chat.ui/delete-message
                                                                                   message
                                                                                   config/delete-message-undo-time-limit-ms])
                                                    :label    (i18n/label :t/delete-for-everyone)
                                                    :icon     :i/delete
                                                    :id       :delete})]))]
        (reset! ref on-long-press)
        [message-content-wrapper message
         [rn/touchable-opacity
          (when-not modal
            {:on-long-press on-long-press
             :on-press      (fn []
                              (reset! show-timestamp? true))})
          [rn/view style/message-view-wrapper
           [message-timestamp message show-timestamp?]
           [rn/view {:style (style/message-view message) :accessibility-label :audio-message}
            [rn/view {:style (style/message-view-content)}
             [message.audio/message-content message] [message-status message]]]]]
         reaction-picker]))))

(defn contact-request-status-pending []
  [rn/view {:style {:flex-direction :row}}
   [quo/text {:style  {:margin-right 5.27}
              :weight :medium
              :color  :secondary}
    (i18n/label :t/contact-request-pending)]
   [rn/activity-indicator {:animating true
                           :size      :small
                           :color     quo.colors/gray}]])

(defn contact-request-status-accepted []
  [quo/text {:style  {:color quo.colors/green}
             :weight :medium}
   (i18n/label :t/contact-request-accepted)])

(defn contact-request-status-declined []
  [quo/text {:style  {:color quo.colors/red}
             :weight :medium}
   (i18n/label :t/contact-request-declined)])

(defn contact-request-status-label [state]
  [rn/view {:style (style/contact-request-status-label state)}
   (case state
     constants/contact-request-message-state-pending [contact-request-status-pending]
     constants/contact-request-message-state-accepted [contact-request-status-accepted]
     constants/contact-request-message-state-declined [contact-request-status-declined])])

(defmethod ->message constants/content-type-contact-request
  [message _]
  [rn/view {:style (style/content-type-contact-request)}
   [rn/image {:source (resources/get-image :hand-wave)
              :style  {:width  112
                       :height 97}}]
   [quo/text {:style  {:margin-top 6}
              :weight :bold
              :size   :large}
    (i18n/label :t/contact-request)]
   [rn/view {:style {:padding-horizontal 16}}
    [quo/text {:style {:margin-top    2
                       :margin-bottom 14}}
     (get-in message [:content :text])]]
   [contact-request-status-label (:contact-request-state message)]])

(defmethod ->message :default [message]
  [message-content-wrapper message
   [unknown-content-type message]])

(defn chat-message [{:keys [pinned pinned-by mentioned in-pinned-view? last-in-group? deleted? deleted-for-me?] :as message}]
  (let [reactions      @(re-frame/subscribe [:chats/message-reactions (:message-id message) (:chat-id message)])
        own-reactions  (reduce (fn [acc {:keys [emoji-id own]}]
                                 (if own (conj acc emoji-id) acc))
                               [] reactions)
        send-emoji     (fn [{:keys [emoji-id]}]
                         (re-frame/dispatch [::models.reactions/send-emoji-reaction
                                             {:message-id (:message-id message)
                                              :emoji-id   emoji-id}]))
        retract-emoji  (fn [{:keys [emoji-id emoji-reaction-id]}]
                         (re-frame/dispatch [::models.reactions/send-emoji-reaction-retraction
                                             {:message-id        (:message-id message)
                                              :emoji-id          emoji-id
                                              :emoji-reaction-id emoji-reaction-id}]))
        on-emoji-press (fn [emoji-id]
                         (let [active ((set own-reactions) emoji-id)]
                           (if active
                             (retract-emoji {:emoji-id          emoji-id
                                             :emoji-reaction-id (reactions/extract-id reactions emoji-id)})
                             (send-emoji {:emoji-id emoji-id}))))
        on-open-drawer (fn [actions]
                         (re-frame/dispatch [:bottom-sheet/show-sheet
                                             {:content (reaction-drawer/message-options
                                                        actions
                                                        (into #{} (js->clj own-reactions))
                                                        #(on-emoji-press %))}]))
        on-long-press  (atom nil)]
    [rn/view
     {:style (merge (when (and (not in-pinned-view?) (or mentioned pinned)) {:background-color colors/primary-50-opa-5
                                                                             :border-radius    16
                                                                             :margin-bottom    4})
                    (when (or mentioned pinned last-in-group?) {:margin-top 8})
                    {:margin-horizontal 8})}

     (when (and pinned (not (or deleted? deleted-for-me?)))
       [rn/view {:style (style/pin-indicator-container)}
        [pinned-by-indicator pinned-by]])
     [->message message {:ref           on-long-press
                         :modal         false
                         :on-long-press on-open-drawer}]
     (when-not (or deleted? deleted-for-me?)
       [reaction-row/message-reactions message reactions nil on-emoji-press on-long-press])])) ;; TODO: pass on-open-drawer function

(defn message-render-fn
  [{:keys [outgoing whisper-timestamp] :as message}
   _
   {:keys [group-chat public? community? current-public-key show-input? edit-enabled]}]
  [chat-message
   (assoc message
          :incoming-group (and group-chat (not outgoing))
          :group-chat group-chat
          :public? public?
          :community? community?
          :current-public-key current-public-key
          :show-input? show-input?
          :message-pin-enabled true
          :in-pinned-view? true
          :pinned true
          :timestamp-str (time/timestamp->time whisper-timestamp)
          :edit-enabled edit-enabled)])

(defn pin-system-message [{:keys [from in-popover? timestamp-str chat-id] :as message} {:keys [modal close-modal]}]
  (let [response-to (:response-to (:content message))]
    [rn/touchable-opacity {:on-press       #(rf/dispatch [:bottom-sheet/show-sheet :pinned-messages-list chat-id])
                           :active-opacity 1
                           :style          (merge {:flex-direction :row :margin-vertical 8} (style/message-wrapper message))}
     [rn/view {:style               {:width            photos.style/default-size
                                     :height           photos.style/default-size
                                     :margin-right     16
                                     :border-radius    photos.style/default-size
                                     :justify-content  :center
                                     :align-items      :center
                                     :background-color colors/primary-50-opa-10}
               :accessibility-label :content-type-pin-icon}
      [pin-icon colors/primary-50 16]]
     [rn/view
      [rn/view {:style {:flex-direction :row :align-items :center}}
       [rn/touchable-opacity {:style    style/message-author-touchable
                              :disabled in-popover?
                              :on-press #(do (when modal (close-modal))
                                             (re-frame/dispatch [:chat.ui/show-profile from]))}
        [message-author-name from {:modal modal} 20]]
       [rn/text {:style {:font-size 13}} (str " " (i18n/label :t/pinned-a-message))]
       [rn/text
        {:style               (merge
                               {:padding-left 5
                                :margin-top   2}
                               (style/message-timestamp-text))
         :accessibility-label :message-timestamp}
        timestamp-str]]
      [quoted-message {:message-id response-to :chat-id chat-id} (:quoted-message message) true]]]))

(defmethod ->message constants/content-type-system-text [{:keys [content quoted-message] :as message}]
  (if quoted-message
    [pin-system-message message]
    [rn/view {:accessibility-label :chat-item}
     [rn/view (style/system-message-body message)
      [rn/view (style/message-view message)
       [rn/view (style/message-view-content)
        [render-parsed-text message (:parsed-text content)]]]]]))
