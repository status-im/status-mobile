(ns status-im.ui.screens.chat.message.message
  (:require
   [quo.core :as quo]
   [quo.design-system.colors :as colors]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [status-im.chat.models.images :as images]
   [status-im.constants :as constants]
   [i18n.i18n :as i18n]
   [status-im.react-native.resources :as resources]
   [status-im.ui.components.animation :as animation]
   [status-im.ui.components.fast-image :as fast-image]
   [status-im.ui.components.icons.icons :as icons]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.chat.components.reply :as components.reply]
   [status-im.ui.screens.chat.image.preview.views :as preview]
   [status-im.ui.screens.chat.message.audio-old :as message.audio]
   [status-im.ui.screens.chat.message.command :as message.command]
   [status-im.ui.screens.chat.message.gap :as message.gap]
   [status-im.ui.screens.chat.message.link-preview :as link-preview]
   [status-im.ui.screens.chat.message.reactions-old :as reactions]
   [status-im.ui.screens.chat.photos :as photos]
   [status-im.ui.screens.chat.sheets :as sheets]
   [status-im.ui.screens.chat.styles.message.message-old :as style]
   [status-im.ui.screens.chat.utils :as chat.utils]
   [status-im.ui.screens.communities.icon :as communities.icon]
   [status-im.utils.config :as config]
   [utils.security.core :as security])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn message-timestamp-anim
  [anim-opacity show-timestamp?]
  (animation/start
   (animation/anim-sequence
    [(animation/timing
      anim-opacity
      {:toValue         1
       :duration        100
       :easing          (.-ease ^js animation/easing)
       :useNativeDriver true})
     (animation/timing
      anim-opacity
      {:toValue         0
       :delay           2000
       :duration        100
       :easing          (.-ease ^js animation/easing)
       :useNativeDriver true})])
   #(reset! show-timestamp? false)))

(defview mention-element
  [from]
  (letsubs [contact-name [:contacts/contact-name-by-identity from]]
    contact-name))

(def edited-at-text (str " ⌫ " (i18n/label :t/edited)))

(defn message-status
  [{:keys [outgoing content outgoing-status pinned edited-at in-popover?]}]
  (when-not in-popover? ;; We keep track if showing this message in a list in pin-limit-popover
    [react/view
     {:align-self                       :flex-end
      :position                         :absolute
      :bottom                           9 ; 6 Bubble bottom, 3 message baseline
      (if (:rtl? content) :left :right) 0
      :flex-direction                   :row
      :align-items                      :flex-end}
     (when outgoing
       [icons/icon
        (case outgoing-status
          :sending   :tiny-icons/tiny-pending
          :sent      :tiny-icons/tiny-sent
          :not-sent  :tiny-icons/tiny-warning
          :delivered :tiny-icons/tiny-delivered
          :tiny-icons/tiny-pending)
        {:width               16
         :height              12
         :color               (if pinned colors/gray colors/white)
         :accessibility-label (name outgoing-status)}])
     (when edited-at
       [react/text {:style (style/message-status-text (and outgoing (not pinned)))} edited-at-text])]))

(defn message-timestamp
  [{:keys [timestamp-str in-popover?] :as message} show-timestamp?]
  (when-not in-popover? ;; We keep track if showing this message in a list in pin-limit-popover
    (let [anim-opacity (animation/create-value 0)]
      [react/animated-view {:style (style/message-timestamp-wrapper message) :opacity anim-opacity}
       (when @show-timestamp? (message-timestamp-anim anim-opacity show-timestamp?))
       [react/text
        {:style               (style/message-timestamp-text)
         :accessibility-label :message-timestamp}
        timestamp-str]])))

(defn quoted-message
  [_ {:keys [from parsed-text image audio sticker id] :as message} outgoing current-public-key public?
   pinned chat-id]
  (let [contact-name    [:contacts/contact-name-by-identity from]
        replied-message (get @(re-frame/subscribe [:chats/chat-messages chat-id]) id)] ;; the message
                                                                                       ;; replied to
    [react/view {:style (style/quoted-message-container (and outgoing (not pinned)))}
     [react/view {:style style/quoted-message-author-container}
      [chat.utils/format-reply-author
       from
       contact-name
       current-public-key
       (partial style/quoted-message-author (and outgoing (not pinned)))
       (and outgoing (not pinned))]]
     (cond
       (and image (not public?)) ;; Disabling images for public-chats
       [fast-image/fast-image
        {:style  {:width            56
                  :height           56
                  :background-color :black
                  :border-radius    4}
         :source {:uri image}}]
       (and audio (not public?)) ;; Disabling audio for public-chats
       [react/view {:style (style/message-view message) :accessibility-label :audio-message}
        [react/view {:style (style/message-view-content)}
         [react/view {:style (style/style-message-text outgoing)}
          [message.audio/message-content message]
          [message-status message]]]]
       sticker
       (if replied-message
         [fast-image/fast-image
          {:style  {:margin 4 :width 56 :height 56}
           ;; Get sticker url of the message replied to
           :source {:uri (((replied-message :content) :sticker) :url)}}]
         ;; Let the user know if the message was deleted
         [react/text {:style (style/quoted-message-text (and outgoing (not pinned)))}
          ;; This hardcorded text can be modified to come from the parsed-text. Also, translations can be
          ;; added.
          "This message was deleted!"])
       :else                     [react/text
                                  {:style           (style/quoted-message-text (and outgoing
                                                                                    (not pinned)))
                                   :number-of-lines 5}
                                  (components.reply/get-quoted-text-with-mentions parsed-text)])]))

(defn render-inline
  [message-text outgoing pinned content-type acc {:keys [type literal destination]}]
  (case type
    ""
    (conj acc literal)

    "code"
    (conj acc
          [quo/text
           {:max-font-size-multiplier react/max-font-size-multiplier
            :style                    (style/inline-code-style)
            :monospace                true}
           literal])

    "emph"
    (conj acc [react/text-class (style/emph-style (and outgoing (not pinned))) literal])

    "strong"
    (conj acc [react/text-class (style/strong-style (and outgoing (not pinned))) literal])

    "strong-emph"
    (conj acc [quo/text (style/strong-emph-style (and outgoing (not pinned))) literal])

    "del"
    (conj acc [react/text-class (style/strikethrough-style (and outgoing (not pinned))) literal])

    "link"
    (conj acc
          [react/text-class
           {:style
            {:color                (if (and outgoing (not pinned)) colors/white-persist colors/blue)
             :text-decoration-line :underline}
            :on-press
            #(when (and (security/safe-link? destination)
                        (security/safe-link-text? message-text))
               (re-frame/dispatch
                [:browser.ui/message-link-pressed destination]))}
           destination])

    "mention"
    (conj acc
          [react/text-class
           {:style    {:color (cond
                                (= content-type constants/content-type-system-text) colors/black
                                (and outgoing (not pinned)) colors/mention-outgoing
                                :else colors/mention-incoming)}
            :on-press (when-not (= content-type constants/content-type-system-text)
                        #(re-frame/dispatch [:chat.ui/show-profile literal]))}
           [mention-element literal]])
    "status-tag"
    (conj
     acc
     [react/text-class
      {:style    {:color                (if (and outgoing (not pinned)) colors/white-persist colors/blue)
                  :text-decoration-line :underline}
       :on-press
       #(re-frame/dispatch
         [:chat.ui/start-public-chat literal])}
      "#"
      literal])

    (conj acc literal)))

(defn render-block
  [{:keys [content outgoing content-type pinned in-popover?]} acc
   {:keys [type ^js literal children]}]
  (case type

    "paragraph"
    (conj acc
          (reduce
           (fn [acc e] (render-inline (:text content) outgoing pinned content-type acc e))
           [react/text-class (style/text-style (and outgoing (not pinned)) content-type in-popover?)]
           children))

    "blockquote"
    (conj acc
          [react/view (style/blockquote-style (and outgoing (not pinned)))
           [react/text-class (style/blockquote-text-style (and outgoing (not pinned)))
            (.substring literal 0 (.-length literal))]])

    "codeblock"
    (conj acc
          [react/view {:style style/codeblock-style}
           [quo/text
            {:max-font-size-multiplier react/max-font-size-multiplier
             :style                    style/codeblock-text-style
             :monospace                true}
            (.substring literal 0 (dec (.-length literal)))]])

    acc))

(defn render-parsed-text
  [message tree]
  (reduce (fn [acc e] (render-block message acc e)) [:<>] tree))

(defn render-parsed-text-with-message-status
  [{:keys [outgoing edited-at in-popover?] :as message} tree]
  (let [elements       (render-parsed-text message tree)
        message-status [react/text {:style (style/message-status-placeholder)}
                        (str (if (and outgoing (not in-popover?)) "        " "  ")
                             (when (and (not in-popover?) edited-at) edited-at-text))]
        last-element   (peek elements)]
    ;; Using `nth` here as slightly faster than `first`, roughly 30%
    ;; It's worth considering pure js structures for this code path as
    ;; it's perfomance critical
    (if (= react/text-class (nth last-element 0))
      ;; Append message status to last text
      (conj (pop elements) (conj last-element message-status))
      ;; Append message status to new block
      (conj elements message-status))))

(defn unknown-content-type
  [{:keys [outgoing content-type content] :as message}]
  [react/view (style/message-view message)
   [react/text
    {:style {:color (if outgoing colors/white-persist colors/black)}}
    (if (seq (:text content))
      (:text content)
      (str "Unhandled content-type " content-type))]])

(defn message-not-sent-text
  [chat-id message-id]
  [react/touchable-highlight
   {:on-press
    (fn []
      (re-frame/dispatch
       [:bottom-sheet/show-sheet
        {:content        (sheets/options chat-id message-id)
         :content-height 200}])
      (react/dismiss-keyboard!))}
   [react/view style/not-sent-view
    [react/text {:style style/not-sent-text}
     (i18n/label :t/status-not-sent-tap)]
    [react/view style/not-sent-icon
     [icons/icon :main-icons/warning {:color colors/red}]]]])

(defn pin-author-name
  [pinned-by]
  (let [user-contact  @(re-frame/subscribe [:multiaccount/contact])
        contact-names @(re-frame/subscribe [:contacts/contact-two-names-by-identity pinned-by])]
    ;; We append empty spaces to the name as a workaround to make one-line and multi-line label
    ;; components show correctly
    (str "                   "
         (if (= pinned-by (user-contact :public-key)) (i18n/label :t/You) (first contact-names)))))

(def pin-icon-width 9)

(def pin-icon-height 15)

(defn pinned-by-indicator
  [outgoing display-photo? pinned-by]
  [react/view
   {:style               (style/pin-indicator outgoing display-photo?)
    :accessibility-label :pinned-by}
   [react/view {:style (style/pinned-by-text-icon-container)}
    [react/view {:style (style/pin-icon-container)}
     [icons/icon :main-icons/pin
      {:color            colors/gray
       :height           pin-icon-height
       :width            pin-icon-width
       :background-color :red}]]
    [quo/text
     {:weight :regular
      :size   :small
      :color  :main
      :style  (style/pinned-by-text)}
     (i18n/label :t/pinned-by)]]
   [quo/text
    {:weight :medium
     :size   :small
     :color  :main
     :style  (style/pin-author-text)}
    (pin-author-name pinned-by)]])

(defn message-delivery-status
  [{:keys [chat-id message-id outgoing-status message-type]}]
  (when (and (not= constants/message-type-private-group-system-message message-type)
             (= outgoing-status :not-sent))
    [message-not-sent-text chat-id message-id]))

(defview message-author-name
  [from opts]
  (letsubs [contact-with-names [:contacts/contact-by-identity from]]
    (chat.utils/format-author-old contact-with-names opts)))

(defview message-my-name
  [opts]
  (letsubs [contact-with-names [:multiaccount/contact]]
    (chat.utils/format-author-old contact-with-names opts)))

(defview community-content
  [{:keys [community-id] :as message}]
  (letsubs [{:keys [name description verified] :as community} [:communities/community community-id]
            communities-enabled?                              [:communities/enabled?]]
    (when (and communities-enabled? community)
      [react/view
       {:style (assoc (style/message-wrapper message)
                      :margin-vertical 10
                      :margin-left     8
                      :width           271)}
       (when verified
         [react/view (style/community-verified)
          [react/text
           {:style {:font-size 13
                    :color     colors/blue}} (i18n/label :t/communities-verified)]])
       [react/view (style/community-message verified)
        [react/view
         {:width        62
          :padding-left 14}
         (if (= community-id constants/status-community-id)
           [react/image
            {:source (resources/get-image :status-logo)
             :style  {:width  40
                      :height 40}}]
           [communities.icon/community-icon community])]
        [react/view {:padding-right 14 :flex 1}
         [react/text {:style {:font-weight "700" :font-size 17}}
          name]
         [react/text description]]]
       [react/view (style/community-view-button)
        [react/touchable-highlight
         {:on-press #(re-frame/dispatch [:navigate-to
                                         :community
                                         {:community-id (:id community)}])}
         [react/text
          {:style {:text-align :center
                   :color      colors/blue}} (i18n/label :t/view)]]]])))

(defn message-content-wrapper
  "Author, userpic and delivery wrapper"
  [{:keys [first-in-group? display-photo? display-username?
           identicon
           from outgoing in-popover?]
    :as   message} content {:keys [modal close-modal]}]
  [react/view
   {:style               (style/message-wrapper message)
    :pointer-events      :box-none
    :accessibility-label :chat-item}
   [react/view
    {:style          (style/message-body message)
     :pointer-events :box-none}
    (when display-photo?
      [react/view (style/message-author-userpic outgoing)
       (when first-in-group?
         [react/touchable-highlight
          {:on-press #(do (when modal (close-modal))
                          (re-frame/dispatch [:chat.ui/show-profile from]))}
          [photos/member-photo from identicon]])])
    [react/view {:style (style/message-author-wrapper outgoing display-photo? in-popover?)}
     (when display-username?
       [react/touchable-opacity
        {:style    style/message-author-touchable
         :disabled in-popover?
         :on-press #(do (when modal (close-modal))
                        (re-frame/dispatch [:chat.ui/show-profile from]))}
        [message-author-name from {:modal modal}]])
     ;;MESSAGE CONTENT
     content
     [link-preview/link-preview-wrapper (:links (:content message)) outgoing false]]]
   ; delivery status
   [react/view (style/delivery-status outgoing)
    [message-delivery-status message]]])

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

(defn message-content-image
  [{:keys [content outgoing in-popover?] :as message}
   {:keys [on-long-press]}]
  (let [dimensions (reagent/atom {:width image-max-width :height image-max-height :loaded false})
        visible    (reagent/atom false)
        uri        (:image content)]
    (fn []
      (let [style-opts {:outgoing outgoing
                        :opacity  (if (:loaded @dimensions) 1 0)
                        :width    (:width @dimensions)
                        :height   (:height @dimensions)}]
        [:<>
         [preview/preview-image
          {:message  message
           :visible  @visible
           :on-close #(do (reset! visible false)
                          (reagent/flush))}]
         [react/touchable-highlight
          {:on-press      (fn []
                            (reset! visible true)
                            (react/dismiss-keyboard!))
           :on-long-press on-long-press
           :disabled      in-popover?}
          [react/view
           {:style               (style/image-message style-opts)
            :accessibility-label :image-message}
           (when (or (:error @dimensions) (not (:loaded @dimensions)))
             [react/view
              (merge (dissoc style-opts :opacity)
                     {:flex 1 :align-items :center :justify-content :center :position :absolute})
              (if (:error @dimensions)
                [icons/icon :main-icons/cancel]
                [react/activity-indicator {:animating true}])])
           [fast-image/fast-image
            {:style    (dissoc style-opts :outgoing)
             :on-load  (image-set-size dimensions)
             :on-error #(swap! dimensions assoc :error true)
             :source   {:uri uri}}]
           [react/view {:style (style/image-message-border style-opts)}]]]]))))

(defmulti ->message :content-type)

(defmethod ->message constants/content-type-command
  [message]
  [message.command/command-content message-content-wrapper message])

(defmethod ->message constants/content-type-gap
  [message]
  [message.gap/gap message])

(defmethod ->message constants/content-type-system-text
  [{:keys [content] :as message}]
  [react/view {:accessibility-label :chat-item}
   [react/view (style/system-message-body message)
    [react/view (style/message-view message)
     [react/view (style/message-view-content)
      [render-parsed-text message (:parsed-text content)]]]]])

(def message-height-px 200)
(def max-message-height-px 150)

(defn pin-message
  [{:keys [chat-id pinned] :as message}]
  (let [pinned-messages @(re-frame/subscribe [:chats/pinned chat-id])]
    (if (and (not pinned) (> (count pinned-messages) 2))
      (do
        (js/setTimeout (fn [] (re-frame/dispatch [:dismiss-keyboard])) 500)
        (re-frame/dispatch [:pin-message/show-pin-limit-modal chat-id]))
      (re-frame/dispatch [:pin-message/send-pin-message (assoc message :pinned (not pinned))]))))

(defn on-long-press-fn
  [on-long-press {:keys [pinned message-pin-enabled outgoing edit-enabled show-input?] :as message}
   content]
  (on-long-press
   (concat
    (when (and outgoing edit-enabled)
      [{:on-press #(re-frame/dispatch [:chat.ui/edit-message message])
        :label    (i18n/label :t/edit)
        :id       :edit}])
    (when show-input?
      [{:on-press #(re-frame/dispatch [:chat.ui/reply-to-message message])
        :label    (i18n/label :t/message-reply)
        :id       :reply}])
    [{:on-press #(react/copy-to-clipboard
                  (components.reply/get-quoted-text-with-mentions
                   (get content :parsed-text)))
      :label    (i18n/label :t/sharing-copy-to-clipboard)
      :id       :copy}]
    (when message-pin-enabled
      [{:on-press #(pin-message message)
        :label    (if pinned (i18n/label :t/unpin) (i18n/label :t/pin))
        :id       (if pinned :unpin :pin)}])
    (when (and outgoing config/delete-message-enabled?)
      [{:on-press #(re-frame/dispatch [:chat.ui/soft-delete-message message])
        :label    (i18n/label :t/delete)
        :id       :delete}]))))

(defn collapsible-text-message
  [{:keys [mentioned]} _]
  (let [collapsed?      (reagent/atom false)
        collapsible?    (reagent/atom false)
        show-timestamp? (reagent/atom false)]
    (fn [{:keys [content outgoing current-public-key public? pinned in-popover? chat-id] :as message}
         on-long-press modal]
      (let [max-height (when-not (or outgoing modal)
                         (if @collapsible?
                           (if @collapsed? message-height-px nil)
                           message-height-px))]
        [react/touchable-highlight
         (when-not modal
           {:on-press         (fn [_]
                                (react/dismiss-keyboard!)
                                (reset! show-timestamp? true))
            :delay-long-press 100
            :on-long-press    (fn []
                                (if @collapsed?
                                  (do (reset! collapsed? false)
                                      (js/setTimeout #(on-long-press-fn on-long-press message content)
                                                     200))
                                  (on-long-press-fn on-long-press message content)))
            :disabled         in-popover?})
         [react/view (style/message-view-wrapper outgoing)
          [message-timestamp message show-timestamp?]
          [react/view {:style (style/message-view message)}
           [react/view
            {:style      (style/message-view-content)
             :max-height max-height}
            (let [response-to (:response-to content)]
              [react/view
               {:on-layout
                #(when (and (> (.-nativeEvent.layout.height ^js %) max-message-height-px)
                            (not @collapsible?)
                            (not outgoing)
                            (not modal))
                   (reset! collapsed? true)
                   (reset! collapsible? true))}
               (when (and (seq response-to) (:quoted-message message))
                 [quoted-message response-to (:quoted-message message) outgoing current-public-key
                  public? pinned chat-id])
               [render-parsed-text-with-message-status message (:parsed-text content)]])
            (when-not @collapsible? [message-status message])
            (when (and @collapsible? (not modal))
              (if @collapsed?
                (let [color (if pinned
                              colors/pin-background
                              (if mentioned colors/mentioned-background colors/blue-light))]
                  [react/touchable-highlight
                   {:on-press #(swap! collapsed? not)
                    :style    {:position :absolute :bottom 0 :left 0 :right 0 :height 72}}
                   [react/linear-gradient
                    {:colors [(str color "00") color]
                     :start  {:x 0 :y 0}
                     :end    {:x 0 :y 0.9}}
                    [react/view
                     {:height          72
                      :align-self      :center
                      :justify-content :flex-end
                      :padding-bottom  10}
                     [react/view (style/collapse-button)
                      [icons/icon :main-icons/dropdown
                       {:color colors/white}]]]]])
                [react/touchable-highlight
                 {:on-press #(swap! collapsed? not)
                  :style    {:align-self :center :margin 5}}
                 [react/view (style/collapse-button)
                  [icons/icon :main-icons/dropdown-up
                   {:color colors/white}]]]))]]]]))))

(defmethod ->message constants/content-type-text
  [message {:keys [on-long-press modal] :as reaction-picker}]
  [message-content-wrapper message
   [collapsible-text-message message on-long-press modal]
   reaction-picker])

(defmethod ->message constants/content-type-community
  [message]
  [community-content message])

(defmethod ->message constants/content-type-status
  [{:keys [content content-type pinned] :as message}]
  [message-content-wrapper message
   [react/view style/status-container
    [react/text {:style (style/status-text)}
     (reduce
      (fn [acc e] (render-inline (:text content) false pinned content-type acc e))
      [react/text-class {:style (style/status-text)}]
      (-> content :parsed-text peek :children))]]])

(defmethod ->message constants/content-type-emoji
  []
  (let [show-timestamp? (reagent/atom false)]
    (fn [{:keys [content current-public-key outgoing edit-enabled public? pinned in-popover?
                 message-pin-enabled content-type edited-at]
          :as   message}
         {:keys [on-long-press modal]
          :as   reaction-picker}]
      ;; Makes sure to render a text-message and not an emoji-message if it has been edited with text
      (if (= content-type constants/content-type-text)
        [message-content-wrapper message
         [collapsible-text-message message on-long-press modal] reaction-picker]
        (let [response-to (:response-to content)]
          [message-content-wrapper message
           [react/touchable-highlight
            (when-not modal
              {:disabled         in-popover?
               :on-press         (fn []
                                   (react/dismiss-keyboard!)
                                   (reset! show-timestamp? true))
               :delay-long-press 100
               :on-long-press    (fn []
                                   (on-long-press
                                    (concat
                                     (when (and outgoing edit-enabled)
                                       [{:on-press #(re-frame/dispatch [:chat.ui/edit-message message])
                                         :label    (i18n/label :t/edit)
                                         :id       :edit}])
                                     [{:on-press #(re-frame/dispatch [:chat.ui/reply-to-message message])
                                       :id       :reply
                                       :label    (i18n/label :t/message-reply)}
                                      {:on-press #(react/copy-to-clipboard (get content :text))
                                       :id       :copy
                                       :label    (i18n/label :t/sharing-copy-to-clipboard)}]
                                     (when message-pin-enabled
                                       [{:on-press #(pin-message message)
                                         :label    (if pinned
                                                     (i18n/label :t/unpin)
                                                     (i18n/label :t/pin))}])
                                     (when (and outgoing config/delete-message-enabled?)
                                       [{:on-press #(re-frame/dispatch [:chat.ui/soft-delete-message
                                                                        message])
                                         :label    (i18n/label :t/delete)
                                         :id       :delete}]))))})
            [react/view (style/message-view-wrapper outgoing)
             [message-timestamp message show-timestamp?]
             [react/view (style/message-view message)
              [react/view {:style (style/message-view-content)}
               [react/view {:style (style/style-message-text outgoing)}
                (when (and (seq response-to) (:quoted-message message))
                  [quoted-message response-to (:quoted-message message) outgoing current-public-key
                   public? pinned])
                [react/text {:style (style/emoji-message message)}
                 (if edited-at (str (content :text) "      ") (str (content :text)))]]
               [message-status message]]]]]
           reaction-picker])))))

(defmethod ->message constants/content-type-sticker
  [{:keys [content from outgoing in-popover?]
    :as   message}
   {:keys [on-long-press modal]
    :as   reaction-picker}]
  (let [pack (get-in content [:sticker :pack])]
    [message-content-wrapper message
     [react/touchable-highlight
      (when-not modal
        {:disabled            in-popover?
         :accessibility-label :sticker-message
         :on-press            (fn [_]
                                (when pack
                                  (re-frame/dispatch [:stickers/open-sticker-pack (str pack)]))
                                (react/dismiss-keyboard!))
         :delay-long-press    100
         :on-long-press       (fn []
                                (on-long-press
                                 (concat
                                  (when-not outgoing
                                    [{:on-press #(when pack
                                                   (re-frame/dispatch [:chat.ui/show-profile from]))
                                      :label    (i18n/label :t/view-details)}])
                                  [{:on-press #(re-frame/dispatch [:chat.ui/reply-to-message message])
                                    :id       :reply
                                    :label    (i18n/label :t/message-reply)}]
                                  (if (and outgoing config/delete-message-enabled?)
                                    [{:on-press #(re-frame/dispatch [:chat.ui/soft-delete-message
                                                                     message])
                                      :label    (i18n/label :t/delete)
                                      :id       :delete}]
                                    []))))})
      [fast-image/fast-image
       {:style  {:margin 10 :width 140 :height 140}
        :source {:uri (str (-> content :sticker :url) "&download=true")}}]]
     reaction-picker]))

(defmethod ->message constants/content-type-image
  [{:keys [content in-popover? outgoing] :as message}
   {:keys [on-long-press modal]
    :as   reaction-picker}]
  [message-content-wrapper message
   [message-content-image message
    {:modal            modal
     :disabled         in-popover?
     :delay-long-press 100
     :on-long-press    (fn []
                         (on-long-press
                          (concat
                           [{:on-press #(re-frame/dispatch [:chat.ui/reply-to-message message])
                             :id       :reply
                             :label    (i18n/label :t/message-reply)}
                            {:on-press #(re-frame/dispatch [:chat.ui/save-image-to-gallery
                                                            (:image content)])
                             :id       :save
                             :label    (i18n/label :t/save)}
                            {:on-press #(images/download-image-http
                                         (get-in message [:content :image])
                                         preview/share)
                             :id       :share
                             :label    (i18n/label :t/share)}]
                           (when (and outgoing config/delete-message-enabled?)
                             [{:on-press #(re-frame/dispatch [:chat.ui/soft-delete-message message])
                               :label    (i18n/label :t/delete)
                               :id       :delete}]))))}]
   reaction-picker])

(defmethod ->message constants/content-type-audio
  []
  (let [show-timestamp? (reagent/atom false)]
    (fn [{:keys [outgoing] :as message}
         {:keys [on-long-press modal]
          :as   reaction-picker}]
      [message-content-wrapper message
       [react/touchable-highlight
        (when-not modal
          {:on-long-press
           (fn []
             (on-long-press
              (concat
               [{:on-press #(re-frame/dispatch [:chat.ui/reply-to-message message])
                 :id       :reply
                 :label    (i18n/label :t/message-reply)}]
               (when (and outgoing config/delete-message-enabled?)
                 [{:on-press #(re-frame/dispatch [:chat.ui/soft-delete-message message])
                   :label    (i18n/label :t/delete)
                   :id       :delete}])
               [])))
           :on-press      (fn []
                            (reset! show-timestamp? true))})
        [react/view (style/message-view-wrapper (:outgoing message))
         [message-timestamp message show-timestamp?]
         [react/view {:style (style/message-view message) :accessibility-label :audio-message}
          [react/view {:style (style/message-view-content)}
           [message.audio/message-content message] [message-status message]]]]]
       reaction-picker])))

(defn contact-request-status-pending
  []
  [react/view {:style {:flex-direction :row}}
   [quo/text
    {:style  {:margin-right 5.27}
     :weight :medium
     :color  :secondary}
    (i18n/label :t/contact-request-pending)]
   [react/activity-indicator
    {:animating true
     :size      :small
     :color     colors/gray}]])

(defn contact-request-status-accepted
  []
  [quo/text
   {:style  {:color colors/green}
    :weight :medium}
   (i18n/label :t/contact-request-accepted)])

(defn contact-request-status-declined
  []
  [quo/text
   {:style  {:color colors/red}
    :weight :medium}
   (i18n/label :t/contact-request-declined)])

(defn contact-request-status-label
  [state]
  [react/view {:style (style/contact-request-status-label state)}
   (case state
     constants/contact-request-message-state-pending  [contact-request-status-pending]
     constants/contact-request-message-state-accepted [contact-request-status-accepted]
     constants/contact-request-message-state-declined [contact-request-status-declined])])

(defmethod ->message constants/content-type-contact-request
  [{:keys [outgoing] :as message} _]
  [react/view {:style (style/content-type-contact-request outgoing)}
   [react/image
    {:source (resources/get-image :hand-wave)
     :style  {:width  112
              :height 97}}]
   [quo/text
    {:style  {:margin-top 6}
     :weight :bold
     :size   :large}
    (i18n/label :t/contact-request)]
   [react/view {:style {:padding-horizontal 16}}
    [quo/text
     {:style {:margin-top    2
              :margin-bottom 14}}
     (get-in message [:content :text])]]
   [contact-request-status-label (:contact-request-state message)]])

(defmethod ->message :default
  [message]
  [message-content-wrapper message
   [unknown-content-type message]])

(defn chat-message
  [{:keys [outgoing display-photo? pinned pinned-by] :as message} space-keeper]
  [:<>
   [reactions/with-reaction-picker
    {:message         message
     :reactions       @(re-frame/subscribe [:chats/message-reactions (:message-id message)
                                            (:chat-id message)])
     :picker-on-open  (fn []
                        (space-keeper true))
     :picker-on-close (fn []
                        (space-keeper false))
     :send-emoji      (fn [{:keys [emoji-id]}]
                        (re-frame/dispatch [:models.reactions/send-emoji-reaction
                                            {:message-id (:message-id message)
                                             :emoji-id   emoji-id}]))
     :retract-emoji   (fn [{:keys [emoji-id emoji-reaction-id]}]
                        (re-frame/dispatch [:models.reactions/send-emoji-reaction-retraction
                                            {:message-id        (:message-id message)
                                             :emoji-id          emoji-id
                                             :emoji-reaction-id emoji-reaction-id}]))
     :render          ->message}]
   (when pinned
     [react/view {:style (style/pin-indicator-container outgoing)}
      [pinned-by-indicator outgoing display-photo? pinned-by]])])
