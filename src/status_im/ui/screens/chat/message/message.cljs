(ns status-im.ui.screens.chat.message.message
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.communities.core :as communities]
            [status-im.utils.config :as config]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.message.audio :as message.audio]
            [status-im.chat.models.reactions :as models.reactions]
            [status-im.ui.screens.chat.message.command :as message.command]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.chat.sheets :as sheets]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.screens.chat.styles.message.message :as style]
            [status-im.ui.screens.chat.utils :as chat.utils]
            [status-im.utils.contenthash :as contenthash]
            [status-im.utils.security :as security]
            [status-im.ui.screens.chat.message.reactions :as reactions]
            [status-im.ui.screens.chat.image.preview.views :as preview]
            [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.ui.screens.chat.components.reply :as components.reply]
            [status-im.ui.screens.chat.message.link-preview :as link-preview])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview mention-element [from]
  (letsubs [contact-name [:contacts/contact-name-by-identity from]]
    contact-name))

(defn message-timestamp
  ([message]
   [message-timestamp message false])
  ([{:keys [timestamp-str outgoing content outgoing-status]} justify-timestamp?]
   [react/view (when justify-timestamp?
                 {:align-self                       :flex-end
                  :position                         :absolute
                  :bottom                           9 ; 6 Bubble bottom, 3 message baseline
                  (if (:rtl? content) :left :right) 0
                  :flex-direction                   :row
                  :align-items                      :flex-end})
    (when (and outgoing justify-timestamp?)
      [icons/icon (case outgoing-status
                    :sending  :tiny-icons/tiny-pending
                    :sent     :tiny-icons/tiny-sent
                    :not-sent :tiny-icons/tiny-warning
                    :tiny-icons/tiny-pending)
       {:width  16
        :height 12
        :color  colors/white}])
    [react/text {:style (style/message-timestamp-text outgoing)}
     timestamp-str]]))

(defview quoted-message
  [_ {:keys [from parsed-text image]} outgoing current-public-key public?]
  (letsubs [contact-name [:contacts/contact-name-by-identity from]]
    [react/view {:style (style/quoted-message-container outgoing)}
     [react/view {:style style/quoted-message-author-container}
      [chat.utils/format-reply-author
       from
       contact-name
       current-public-key
       (partial style/quoted-message-author outgoing)
       outgoing]]
     (if (and image
              ;; Disabling images for public-chats
              (not public?))
       [react/image {:style  {:width            56
                              :height           56
                              :background-color :black
                              :border-radius    4}
                     :source {:uri image}}]
       [react/text {:style           (style/quoted-message-text outgoing)
                    :number-of-lines 5}
        (components.reply/get-quoted-text-with-mentions parsed-text)])]))

(defn render-inline [message-text outgoing content-type acc {:keys [type literal destination]}]
  (case type
    ""
    (conj acc literal)

    "code"
    (conj acc [quo/text {:max-font-size-multiplier react/max-font-size-multiplier
                         :style                    (style/inline-code-style)
                         :monospace                true}
               literal])

    "emph"
    (conj acc [react/text-class (style/emph-style outgoing) literal])

    "strong"
    (conj acc [react/text-class (style/strong-style outgoing) literal])

    "strong-emph"
    (conj acc [quo/text (style/strong-emph-style outgoing) literal])

    "del"
    (conj acc [react/text-class (style/strikethrough-style outgoing) literal])

    "link"
    (conj acc
          [react/text-class
           {:style
            {:color                (if outgoing colors/white-persist colors/blue)
             :text-decoration-line :underline}
            :on-press
            #(when (and (security/safe-link? destination)
                        (security/safe-link-text? message-text))
               (re-frame/dispatch
                [:browser.ui/message-link-pressed destination]))}
           destination])

    "mention"
    (conj acc [react/text-class
               {:style    {:color (cond
                                    (= content-type constants/content-type-system-text) colors/black
                                    outgoing                                            colors/mention-outgoing
                                    :else                                               colors/mention-incoming)}
                :on-press (when-not (= content-type constants/content-type-system-text)
                            #(re-frame/dispatch [:chat.ui/show-profile-without-adding-contact literal]))}
               [mention-element literal]])
    "status-tag"
    (conj acc [react/text-class
               {:style {:color                (if outgoing colors/white-persist colors/blue)
                        :text-decoration-line :underline}
                :on-press
                #(re-frame/dispatch
                  [:chat.ui/start-public-chat literal {:navigation-reset? true}])}
               "#"
               literal])

    (conj acc literal)))

(defn render-block [{:keys [content outgoing content-type]} acc
                    {:keys [type ^js literal children]}]
  (case type

    "paragraph"
    (conj acc (reduce
               (fn [acc e] (render-inline (:text content) outgoing content-type acc e))
               [react/text-class (style/text-style outgoing content-type)]
               children))

    "blockquote"
    (conj acc [react/view (style/blockquote-style outgoing)
               [react/text-class (style/blockquote-text-style outgoing)
                (.substring literal 0 (dec (.-length literal)))]])

    "codeblock"
    (conj acc [react/view {:style style/codeblock-style}
               [quo/text {:max-font-size-multiplier react/max-font-size-multiplier
                          :style                    style/codeblock-text-style
                          :monospace                true}
                (.substring literal 0 (dec (.-length literal)))]])

    acc))

(defn render-parsed-text [message tree]
  (reduce (fn [acc e] (render-block message acc e)) [:<>] tree))

(defn render-parsed-text-with-timestamp [{:keys [timestamp-str outgoing] :as message} tree]
  (let [elements (render-parsed-text message tree)
        timestamp [react/text {:style (style/message-timestamp-placeholder)}
                   (str (if outgoing "        " "  ") timestamp-str)]
        last-element (peek elements)]
    ;; Using `nth` here as slightly faster than `first`, roughly 30%
    ;; It's worth considering pure js structures for this code path as
    ;; it's perfomance critical
    (if (= react/text-class (nth last-element 0))
      ;; Append timestamp to last text
      (conj (pop elements) (conj last-element timestamp))
      ;; Append timestamp to new block
      (conj elements timestamp))))

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

(defn message-delivery-status
  [{:keys [chat-id message-id outgoing-status message-type]}]
  (when (and (not= constants/message-type-private-group-system-message message-type)
             (= outgoing-status :not-sent))
    [message-not-sent-text chat-id message-id]))

(defview message-author-name [from opts]
  (letsubs [contact-with-names [:contacts/contact-by-identity from]]
    (chat.utils/format-author contact-with-names opts)))

(defview message-my-name [opts]
  (letsubs [contact-with-names [:multiaccount/contact]]
    (chat.utils/format-author contact-with-names opts)))

(defview community-content [{:keys [community-id] :as message}]
  (letsubs [{:keys [joined verified] :as community} [:communities/community community-id]]
    (when (and
           config/communities-enabled?
           community)
      [react/view {:style (assoc (style/message-wrapper message)
                                 :margin-vertical 10
                                 :width 271)}
       (when verified
         [react/view {:border-right-width 1
                      :border-left-width 1
                      :border-top-width 1
                      :border-left-color colors/gray-lighter
                      :border-right-color colors/gray-lighter
                      :border-top-left-radius 10
                      :border-top-right-radius 10
                      :padding-vertical 8
                      :padding-horizontal 15
                      :border-top-color colors/gray-lighter}
          [react/text {:style {:font-size 13
                               :color colors/blue}} (i18n/label :t/communities-verified)]])

       [react/view {:flex-direction :row
                    :padding-vertical 12
                    :border-top-left-radius (when-not verified 10)
                    :border-top-right-radius (when-not verified 10)
                    :border-right-width 1
                    :border-left-width 1
                    :border-top-width 1
                    :border-color colors/gray-lighter}

        [react/view {:width 62
                     :padding-left 14}
         (if (= community-id constants/status-community-id)
           [react/image {:source (resources/get-image :status-logo)
                         :style {:width 40
                                 :height 40}}]

           (let [display-name (get-in community [:description :identity :display-name])]
             [chat-icon/chat-icon-view-chat-list
              display-name
              true
              display-name
              colors/default-community-color]))]
        [react/view {:padding-right 14}
         [react/text {:style {:font-weight "700"
                              :font-size 17}}
          (get-in community [:description :identity :display-name])]
         [react/text (get-in community [:description :identity :description])]]]
       [react/view {:border-width 1
                    :padding-vertical 8
                    :border-bottom-left-radius 10
                    :border-bottom-right-radius 10
                    :border-color colors/gray-lighter}
        [react/touchable-highlight {:on-press #(re-frame/dispatch [(if joined ::communities/leave ::communities/join) (:id community)])}
         [react/text {:style {:text-align :center
                              :color colors/blue}} (if joined (i18n/label :t/leave) (i18n/label :t/join))]]]])))

(defn message-content-wrapper
  "Author, userpic and delivery wrapper"
  [{:keys [first-in-group? display-photo? display-username?
           identicon
           from outgoing]
    :as   message} content {:keys [modal close-modal]}]
  [react/view {:style               (style/message-wrapper message)
               :pointer-events      :box-none
               :accessibility-label :chat-item}
   [react/view {:style          (style/message-body message)
                :pointer-events :box-none}
    (when display-photo?
      [react/view (style/message-author-userpic outgoing)
       (when first-in-group?
         [react/touchable-highlight {:on-press #(do (when modal (close-modal))
                                                    (re-frame/dispatch [:chat.ui/show-profile-without-adding-contact from]))}
          [photos/member-photo from identicon]])])
    [react/view {:style (style/message-author-wrapper outgoing display-photo?)}
     (when display-username?
       [react/touchable-opacity {:style    style/message-author-touchable
                                 :on-press #(do (when modal (close-modal))
                                                (re-frame/dispatch [:chat.ui/show-profile-without-adding-contact from]))}
        [message-author-name from {:modal modal}]])
     ;;MESSAGE CONTENT
     [react/view
      content]
     [link-preview/link-preview-wrapper (:links (:content message)) outgoing false]]]
   ; delivery status
   [react/view (style/delivery-status outgoing)
    [message-delivery-status message]]])

(def image-max-width 260)
(def image-max-height 192)

(defn image-set-size [dimensions]
  (fn [width height]
    (when (< width height)
      ;; if width less than the height we reduce width proportionally to height
      (let [k (/ height image-max-height)]
        (when (not= (/ width k) (first @dimensions))
          (reset! dimensions [(/ width k) image-max-height]))))))

(defn message-content-image [{:keys [content outgoing] :as message} {:keys [on-long-press]}]
  (let [dimensions (reagent/atom [image-max-width image-max-height])
        visible (reagent/atom false)
        uri (:image content)]
    (react/image-get-size uri (image-set-size dimensions))
    (fn []
      (let [style-opts {:outgoing outgoing
                        :width    (first @dimensions)
                        :height   (second @dimensions)}]
        [:<>
         [preview/preview-image {:message  message
                                 :visible  @visible
                                 :on-close #(do (reset! visible false)
                                                (reagent/flush))}]
         [react/touchable-highlight {:on-press      (fn []
                                                      (reset! visible true)
                                                      (react/dismiss-keyboard!))
                                     :on-long-press on-long-press}
          [react/view {:style               (style/image-message style-opts)
                       :accessibility-label :message-image}
           [react/image {:style       (dissoc style-opts :outgoing)
                         :resize-mode :cover
                         :source      {:uri uri}}
            [react/view {:style (style/image-message-border style-opts)}]]]]]))))

(defmulti ->message :content-type)

(defmethod ->message constants/content-type-command
  [message]
  [message.command/command-content message-content-wrapper message])

(defmethod ->message constants/content-type-system-text [{:keys [content] :as message}]
  [react/view {:accessibility-label :chat-item}
   [react/view (style/system-message-body message)
    [react/view (style/message-view message)
     [react/view (style/message-view-content)
      [render-parsed-text message (:parsed-text content)]]]]])

(def message-height-px 200)
(def max-message-height-px 150)

(defn on-long-press-fn [on-long-press message content]
  (on-long-press
   [{:on-press #(re-frame/dispatch [:chat.ui/reply-to-message message])
     :label    (i18n/label :t/message-reply)}
    {:on-press #(react/copy-to-clipboard
                 (components.reply/get-quoted-text-with-mentions
                  (get content :parsed-text)))
     :label    (i18n/label :t/sharing-copy-to-clipboard)}]))

(defn collapsible-text-message [_ _]
  (let [collapsed?   (reagent/atom false)
        collapsible? (reagent/atom false)]
    (fn [{:keys [content outgoing current-public-key public?] :as message} on-long-press modal]
      (let [max-height (when-not (or outgoing modal)
                         (if @collapsible?
                           (if @collapsed? message-height-px nil)
                           message-height-px))]
        [react/touchable-highlight
         (when-not modal
           {:on-press      (fn [_]
                             (react/dismiss-keyboard!))
            :on-long-press (fn []
                             (if @collapsed?
                               (do (reset! collapsed? false)
                                   (js/setTimeout #(on-long-press-fn on-long-press message content) 200))
                               (on-long-press-fn on-long-press message content)))})
         [react/view {:style (style/message-view message)}
          [react/view {:style      (style/message-view-content)
                       :max-height max-height}
           (let [response-to (:response-to content)]
             [react/view {:on-layout
                          #(when (and (> (.-nativeEvent.layout.height ^js %) max-message-height-px)
                                      (not @collapsible?)
                                      (not outgoing)
                                      (not modal))
                             (reset! collapsed? true)
                             (reset! collapsible? true))}
              (when (and (seq response-to) (:quoted-message message))
                [quoted-message response-to (:quoted-message message) outgoing current-public-key public?])
              [render-parsed-text-with-timestamp message (:parsed-text content)]])
           (when-not @collapsed?
             [message-timestamp message true])
           (when (and @collapsible? (not modal))
             (if @collapsed?
               [react/touchable-highlight
                {:on-press #(swap! collapsed? not)
                 :style    {:position :absolute :bottom 0 :left 0 :right 0 :height 72}}
                [react/linear-gradient {:colors [(str colors/blue-light "00") colors/blue-light]
                                        :start  {:x 0 :y 0} :end {:x 0 :y 0.9}}
                 [react/view {:height         72 :align-self :center :justify-content :flex-end
                              :padding-bottom 10}
                  [react/view (style/collapse-button)
                   [icons/icon :main-icons/dropdown
                    {:color colors/white}]]]]]
               [react/touchable-highlight {:on-press #(swap! collapsed? not)
                                           :style    {:align-self :center :margin 5}}
                [react/view (style/collapse-button)
                 [icons/icon :main-icons/dropdown-up
                  {:color colors/white}]]]))]]]))))

(defmethod ->message constants/content-type-text
  [message {:keys [on-long-press modal] :as reaction-picker}]
  [message-content-wrapper message
   [collapsible-text-message message on-long-press modal]
   reaction-picker])

(defmethod ->message constants/content-type-community
  [message]
  [community-content message])

(defmethod ->message constants/content-type-status
  [{:keys [content content-type] :as message}]
  [message-content-wrapper message
   [react/view style/status-container
    [react/text {:style (style/status-text)}
     (reduce
      (fn [acc e] (render-inline (:text content) false content-type acc e))
      [react/text-class {:style (style/status-text)}]
      (-> content :parsed-text peek :children))]]])

(defmethod ->message constants/content-type-emoji
  [{:keys [content current-public-key outgoing public?] :as message} {:keys [on-long-press modal]
                                                                      :as   reaction-picker}]
  (let [response-to (:response-to content)]
    [message-content-wrapper message
     [react/touchable-highlight (when-not modal
                                  {:on-press      (fn []
                                                    (react/dismiss-keyboard!))
                                   :on-long-press (fn []
                                                    (on-long-press
                                                     [{:on-press #(re-frame/dispatch [:chat.ui/reply-to-message message])
                                                       :label    (i18n/label :t/message-reply)}
                                                      {:on-press #(react/copy-to-clipboard (get content :text))
                                                       :label    (i18n/label :t/sharing-copy-to-clipboard)}]))})
      [react/view (style/message-view message)
       [react/view {:style (style/message-view-content)}
        [react/view {:style (style/style-message-text outgoing)}
         (when (and (seq response-to) (:quoted-message message))
           [quoted-message response-to (:quoted-message message) outgoing current-public-key public?])
         [react/text {:style (style/emoji-message message)}
          (:text content)]]
        [message-timestamp message]]]]
     reaction-picker]))

(defmethod ->message constants/content-type-sticker
  [{:keys [content from outgoing]
    :as   message}
   {:keys [on-long-press modal]
    :as   reaction-picker}]
  (let [pack (get-in content [:sticker :pack])]
    [message-content-wrapper message
     [react/touchable-highlight (when-not modal
                                  {:accessibility-label :sticker-message
                                   :on-press            (fn [_]
                                                          (when pack
                                                            (re-frame/dispatch [:stickers/open-sticker-pack pack]))
                                                          (react/dismiss-keyboard!))
                                   :on-long-press       (fn []
                                                          (on-long-press
                                                           (when-not outgoing
                                                             [{:on-press #(when pack
                                                                            (re-frame/dispatch [:chat.ui/show-profile-without-adding-contact from]))
                                                               :label    (i18n/label :t/view-details)}])))})
      [react/image {:style  {:margin 10 :width 140 :height 140}
                    ;;TODO (perf) move to event
                    :source {:uri (contenthash/url (-> content :sticker :hash))}}]]
     reaction-picker]))

(defmethod ->message constants/content-type-image [{:keys [content] :as message} {:keys [on-long-press modal]
                                                                                  :as   reaction-picker}]
  [message-content-wrapper message
   [message-content-image message {:modal modal
                                   :on-long-press (fn []
                                                    (on-long-press
                                                     [{:on-press #(re-frame/dispatch [:chat.ui/reply-to-message message])
                                                       :label    (i18n/label :t/message-reply)}
                                                      {:on-press #(re-frame/dispatch [:chat.ui/save-image-to-gallery (:image content)])
                                                       :label    (i18n/label :t/save)}]))}]
   reaction-picker])

(defmethod ->message constants/content-type-audio [message {:keys [on-long-press modal]
                                                            :as   reaction-picker}]
  [message-content-wrapper message
   [react/touchable-highlight (when-not modal
                                {:on-long-press
                                 (fn [] (on-long-press []))})
    [react/view {:style (style/message-view message) :accessibility-label :audio-message}
     [react/view {:style (style/message-view-content)}
      [message.audio/message-content message [message-timestamp message false]]]]]
   reaction-picker])

(defmethod ->message :default [message]
  [message-content-wrapper message
   [unknown-content-type message]])

(defn chat-message [message space-keeper]
  [reactions/with-reaction-picker
   {:message         message
    :reactions       @(re-frame/subscribe [:chats/message-reactions (:message-id message)])
    :picker-on-open  (fn []
                       (space-keeper true))
    :picker-on-close (fn []
                       (space-keeper false))
    :send-emoji      (fn [{:keys [emoji-id]}]
                       (re-frame/dispatch [::models.reactions/send-emoji-reaction
                                           {:message-id (:message-id message)
                                            :emoji-id   emoji-id}]))
    :retract-emoji   (fn [{:keys [emoji-id emoji-reaction-id]}]
                       (re-frame/dispatch [::models.reactions/send-emoji-reaction-retraction
                                           {:message-id        (:message-id message)
                                            :emoji-id          emoji-id
                                            :emoji-reaction-id emoji-reaction-id}]))
    :render          ->message}])
