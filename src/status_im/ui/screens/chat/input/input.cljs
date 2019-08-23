(ns status-im.ui.screens.chat.input.input
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.chat.styles.input.input :as style]
            [status-im.ui.screens.chat.styles.message.message :as message-style]
            [status-im.ui.screens.chat.input.parameter-box :as parameter-box]
            [status-im.ui.screens.chat.input.send-button :as send-button]
            [status-im.ui.screens.chat.input.suggestions :as suggestions]
            [status-im.ui.screens.chat.input.validation-messages :as validation-messages]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.chat.utils :as chat-utils]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.utils.platform :as platform]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.utils :as utils]
            [status-im.utils.config :as config]
            [taoensso.timbre :as log]
            [status-im.ui.screens.chat.stickers.views :as stickers]))

(defview basic-text-input [{:keys [set-container-width-fn height single-line-input?]}]
  (letsubs [{:keys [input-text]} [:chats/current-chat]
            cooldown-enabled?    [:chats/cooldown-enabled?]]
    [react/text-input
     (merge
      {:ref                    #(when % (re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-ref %}]))
       :accessibility-label    :chat-message-input
       :multiline              (not single-line-input?)
       :default-value          (or input-text "")
       :editable               (not cooldown-enabled?)
       :blur-on-submit         false
       :on-focus               #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-focused?    true
                                                                                :show-stickers?    false
                                                                                :messages-focused? false}])
       :on-blur                #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-focused? false}])
       :on-submit-editing      #(when single-line-input?
                                  (re-frame/dispatch [:chat.ui/send-current-message]))
       :on-layout              #(set-container-width-fn (.-width (.-layout (.-nativeEvent %))))
       :on-change              #(re-frame/dispatch [:chat.ui/set-chat-input-text (.-text (.-nativeEvent %))])
       :on-selection-change    #(let [s (-> (.-nativeEvent %)
                                            (.-selection))
                                      end (.-end s)]
                                  (re-frame/dispatch [:chat.ui/set-chat-ui-props {:selection end}]))
       :style                  (style/input-view single-line-input?)
       :placeholder-text-color colors/gray
       :auto-capitalize        :sentences}
      (when cooldown-enabled?
        {:placeholder (i18n/label :cooldown/text-input-disabled)}))]))

(defview basic-text-input-desktop [{:keys [set-container-width-fn height single-line-input? set-text state-text]}]
  (letsubs [inp-ref       (atom nil)
            cooldown-enabled?    [:chats/cooldown-enabled?]]
    [react/text-input
     (merge
      {:ref                    #(when % (do
                                          (reset! inp-ref %)
                                          (re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-ref %}])))
       :accessibility-label    :chat-message-input
       :multiline              (not single-line-input?)
       :default-value          @state-text
       :editable               (not cooldown-enabled?)
       :blur-on-submit         false
       :on-focus               #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-focused?    true
                                                                                :show-stickers?    false
                                                                                :messages-focused? false}])
       :on-blur                #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-focused? false}])
       :submit-shortcut        {:key "Enter"}
       :on-submit-editing      #(do
                                  (.clear @inp-ref)
                                  (.focus @inp-ref)
                                  (re-frame/dispatch [:chat.ui/set-chat-input-text @state-text])
                                  (re-frame/dispatch [:chat.ui/send-current-message])
                                  (set-text ""))
       :on-layout              #(set-container-width-fn (.-width (.-layout (.-nativeEvent %))))
       :on-change              #(do
                                  (set-text (.-text (.-nativeEvent %))))
       :on-end-editing         #(re-frame/dispatch [:chat.ui/set-chat-input-text @state-text])
       :on-selection-change    #(let [s (-> (.-nativeEvent %)
                                            (.-selection))
                                      end (.-end s)]
                                  (re-frame/dispatch [:chat.ui/set-chat-ui-props {:selection end}]))
       :style                  (style/input-view single-line-input?)
       :placeholder-text-color colors/gray
       :auto-capitalize        :sentences}
      (when cooldown-enabled?
        {:placeholder (i18n/label :cooldown/text-input-disabled)}))]))
;)

(defview invisible-input [{:keys [set-layout-width-fn value]}]
  (letsubs [{:keys [input-text]} [:chats/current-chat]]
    [react/text {:style     style/invisible-input-text
                 :on-layout #(let [w (-> (.-nativeEvent %)
                                         (.-layout)
                                         (.-width))]
                               (set-layout-width-fn w))}
     (or input-text "")]))

(defn- input-helper-view-on-update [{:keys [opacity-value placeholder]}]
  (fn [_]
    (let [to-value (if @placeholder 1 0)]
      (animation/start
       (animation/timing opacity-value {:toValue         to-value
                                        :duration        300
                                        :useNativeDriver true})))))

(defview input-helper [{:keys [width]}]
  (letsubs [placeholder   [:chats/input-placeholder]
            opacity-value (animation/create-value 0)
            on-update     (input-helper-view-on-update {:opacity-value opacity-value
                                                        :placeholder   placeholder})]
    {:component-did-update on-update}
    [react/animated-view {:style (style/input-helper-view width opacity-value)}
     [react/text {:style (style/input-helper-text width)}
      placeholder]]))

(defn get-options [type]
  (case (keyword type)
    :phone {:keyboard-type "phone-pad"}
    :password {:secure-text-entry true}
    :number {:keyboard-type "numeric"}
    nil))

(defview input-view [{:keys [single-line-input? set-text state-text]}]
  (letsubs [command [:chats/selected-chat-command]]
    (let [component              (reagent/current-component)
          set-layout-width-fn    #(reagent/set-state component {:width %})
          set-container-width-fn #(reagent/set-state component {:container-width %})
          {:keys [width]} (reagent/state component)]
      [react/view {:style style/input-root}
       [react/animated-view {:style style/input-animated}
        [invisible-input {:set-layout-width-fn set-layout-width-fn}]
        (if platform/desktop?
          [basic-text-input-desktop {:set-container-width-fn set-container-width-fn
                                     :single-line-input?     single-line-input?
                                     :set-text               set-text
                                     :state-text             state-text}]
          [basic-text-input {:set-container-width-fn set-container-width-fn
                             :single-line-input?     single-line-input?}])
        [input-helper {:width width}]]])))

(defview commands-button []
  (letsubs [commands      [:chats/all-available-commands]
            reply-message [:chats/reply-message]]
    (when (and (not reply-message) (seq commands))
      [react/touchable-highlight
       {:on-press            #(re-frame/dispatch [:chat.ui/set-command-prefix])
        :accessibility-label :chat-commands-button}
       [react/view
        [vector-icons/icon :main-icons/commands {:container-style style/input-commands-icon
                                                 :color           colors/gray}]]])))

(defview reply-message [from message-text]
  (letsubs [username           [:contacts/contact-name-by-identity from]
            current-public-key [:multiaccount/public-key]]
    [react/scroll-view {:style style/reply-message-content}
     (chat-utils/format-reply-author from username current-public-key style/reply-message-author)
     [react/text {:style (message-style/style-message-text false)} message-text]]))

(defview reply-message-view []
  (letsubs [{:keys [content from] :as message} [:chats/reply-message]]
    (when message
      [react/view {:style style/reply-message-container}
       [react/view {:style style/reply-message}
        [photos/member-photo from]
        [reply-message from (:text content)]]
       [react/touchable-highlight
        {:style               style/cancel-reply-highlight
         :on-press            #(re-frame/dispatch [:chat.ui/cancel-message-reply])
         :accessibility-label :cancel-message-reply}
        [react/view {:style style/cancel-reply-container}
         [vector-icons/icon :main-icons/close {:container-style style/cancel-reply-icon
                                               :color           colors/white}]]]])))

(defview input-container []
  (letsubs [margin               [:chats/input-margin]
            mainnet?             [:mainnet?]
            {:keys [input-text]} [:chats/current-chat]
            result-box           [:chats/current-chat-ui-prop :result-box]
            show-stickers?       [:chats/current-chat-ui-prop :show-stickers?]
            state-text (reagent/atom "")]
    {:component-will-unmount #(when platform/desktop?
                                (re-frame/dispatch [:chat.ui/set-chat-input-text @state-text]))

     :component-did-mount    #(when-not (string/blank? input-text) (reset! state-text input-text))}
    (let [single-line-input? (:singleLineInput result-box)
          component          (reagent/current-component)
          set-text           #(reset! state-text %)
          input-text-empty? (if platform/desktop?
                              (string/blank? state-text)
                              (string/blank? input-text))]
      [react/view {:style     (style/root margin)
                   :on-layout #(let [h (-> (.-nativeEvent %)
                                           (.-layout)
                                           (.-height))]
                                 (when (> h 0)
                                   (re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-height h}])))}
       [reply-message-view]
       [react/view {:style style/input-container}
        [input-view {:single-line-input? single-line-input? :set-text set-text :state-text state-text}]
        (when (and input-text-empty? mainnet?)
          [stickers/button show-stickers?])
        (if input-text-empty?
          [commands-button]
          (if platform/desktop?
            [send-button/send-button-view {:input-text @state-text}
             #(do
                (re-frame/dispatch [:chat.ui/set-chat-input-text @state-text])
                (re-frame/dispatch [:chat.ui/send-current-message])
                (set-text ""))]
            [send-button/send-button-view {:input-text input-text}
             #(re-frame/dispatch [:chat.ui/send-current-message])]))]])))

(defn container []
  [react/view
   [parameter-box/parameter-box-view]
   [suggestions/suggestions-view]
   [validation-messages/validation-messages-view]
   [input-container]])
