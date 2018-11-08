(ns status-im.ui.screens.chat.input.input
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.chat :as components.chat]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.input.parameter-box :as parameter-box]
            [status-im.ui.screens.chat.input.send-button :as send-button]
            [status-im.ui.screens.chat.input.suggestions :as suggestions]
            [status-im.ui.screens.chat.input.validation-messages :as validation-messages]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.chat.styles.input.input :as style]
            [status-im.ui.screens.chat.styles.message.message :as message-style])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview basic-text-input [{:keys [set-container-width-fn height single-line-input?]}]
  (letsubs [{:keys [input-text]} [:chats/current]
            cooldown-enabled?    [:chat/cooldown-enabled?]]
    [react/text-input
     (merge
      {:ref                    #(when % (re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-ref %}]))
       :accessibility-label    :chat-message-input
       :multiline              (not single-line-input?)
       :default-value          (or input-text "")
       :editable               (not cooldown-enabled?)
       :blur-on-submit         false
       :on-focus               #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-focused?    true
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

(defview invisible-input [{:keys [set-layout-width-fn value]}]
  (letsubs [{:keys [input-text]} [:chats/current]]
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
       (animation/timing opacity-value {:toValue  to-value
                                        :duration 300})))))

(defview input-helper [{:keys [width]}]
  (letsubs [placeholder   [:chat/input-placeholder]
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

(defview input-view [{:keys [single-line-input?]}]
  (letsubs [command [:chat/selected-command]]
    (let [component              (reagent/current-component)
          set-layout-width-fn    #(reagent/set-state component {:width %})
          set-container-width-fn #(reagent/set-state component {:container-width %})
          {:keys [width]} (reagent/state component)]
      [react/view {:style style/input-root}
       [react/animated-view {:style style/input-animated}
        [invisible-input {:set-layout-width-fn set-layout-width-fn}]
        [basic-text-input {:set-container-width-fn set-container-width-fn
                           :single-line-input?     single-line-input?}]
        [input-helper {:width width}]]])))

(defview commands-button []
  (letsubs [commands      [:chat/all-available-commands]
            reply-message [:chat/reply-message]]
    (when (and (not reply-message) (seq commands))
      [react/touchable-highlight
       {:on-press            #(re-frame/dispatch [:chat.ui/set-command-prefix])
        :accessibility-label :chat-commands-button}
       [react/view
        [vector-icons/icon :icons/input-commands {:container-style style/input-commands-icon
                                                  :color           :dark}]]])))

(defn reply-message
  [{:keys [text] :as message}]
  [react/view {:style style/reply-message-content}
   [components.chat/message-author-name {:style style/reply-message-author} message]
   [react/text {:style (message-style/style-message-text false)} text]])

(defn reply-message-view
  [{:keys [photo-path] :as message}]
  (when message
    [react/view {:style style/reply-message-container}
     [react/view {:style style/reply-message}
      [photos/member-photo photo-path]
      [reply-message message]]
     [react/touchable-highlight
      {:style               style/cancel-reply-highlight
       :on-press            #(re-frame/dispatch [:chat.ui/cancel-message-reply])
       :accessibility-label :cancel-message-reply}
      [react/view {:style style/cancel-reply-container}
       [vector-icons/icon :icons/close {:container-style style/cancel-reply-icon
                                        :color           colors/white}]]]]))

(defview input-container []
  (letsubs [margin               [:chat/input-margin]
            {:keys [input-text]} [:chats/current]
            result-box           [:chat/current-chat-ui-prop :result-box]
            reply-message        [:chat/reply-message]]
    (let [single-line-input? (:singleLineInput result-box)]
      [react/view {:style     (style/root margin)
                   :on-layout #(let [h (-> (.-nativeEvent %)
                                           (.-layout)
                                           (.-height))]
                                 (when (> h 0)
                                   (re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-height h}])))}
       [reply-message-view reply-message]
       [react/view {:style style/input-container}
        [input-view {:single-line-input? single-line-input?}]
        (if (string/blank? input-text)
          [commands-button]
          [send-button/send-button-view])]])))

(defn container []
  [react/view
   [parameter-box/parameter-box-view]
   [suggestions/suggestions-view]
   [validation-messages/validation-messages-view]
   [input-container]])
