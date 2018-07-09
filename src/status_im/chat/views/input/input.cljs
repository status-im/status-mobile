(ns status-im.chat.views.input.input
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.chat.constants :as constants]
            [status-im.chat.styles.input.input :as style]
            [status-im.chat.views.input.parameter-box :as parameter-box]
            [status-im.chat.views.input.result-box :as result-box]
            [status-im.chat.views.input.send-button :as send-button]
            [status-im.chat.views.input.suggestions :as suggestions]
            [status-im.chat.views.input.validation-messages :as validation-messages]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]))

(defview basic-text-input [{:keys [set-container-width-fn height single-line-input?]}]
  (letsubs [{:keys [input-text]} [:get-current-chat]
            cooldown-enabled?    [:chat-cooldown-enabled?]]
    [react/text-input
     (merge
      {:ref                    #(when % (re-frame/dispatch [:set-chat-ui-props {:input-ref %}]))
       :accessibility-label    :chat-message-input
       :multiline              (not single-line-input?)
       :default-value          (or input-text "")
       :editable               (not cooldown-enabled?)
       :blur-on-submit         false
       :on-focus               #(re-frame/dispatch [:set-chat-ui-props {:input-focused?    true
                                                                        :messages-focused? false}])
       :on-blur                #(re-frame/dispatch [:set-chat-ui-props {:input-focused? false}])
       :on-submit-editing      #(when single-line-input?
                                  (re-frame/dispatch [:send-current-message]))
       :on-layout              #(set-container-width-fn (.-width (.-layout (.-nativeEvent %))))
       :on-change              #(re-frame/dispatch [:set-chat-input-text (.-text (.-nativeEvent %))])
       :on-selection-change    #(let [s (-> (.-nativeEvent %)
                                            (.-selection))
                                      end (.-end s)]
                                  (re-frame/dispatch [:update-text-selection end]))
       :style                  (style/input-view single-line-input?)
       :placeholder-text-color colors/gray
       :auto-capitalize        :sentences}
      (when cooldown-enabled?
        {:placeholder (i18n/label :cooldown/text-input-disabled)}))]))

(defview invisible-input [{:keys [set-layout-width-fn value]}]
  (letsubs [{:keys [input-text]} [:get-current-chat]]
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
  (letsubs [placeholder   [:chat-input-placeholder]
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
  (letsubs [command [:selected-chat-command]]
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
  (letsubs [commands-responses [:get-available-commands-responses]]
    (when (seq commands-responses)
      [react/touchable-highlight
       {:on-press            #(do (re-frame/dispatch [:set-chat-input-text constants/command-char])
                                  (re-frame/dispatch [:chat-input-focus :input-ref]))
        :accessibility-label :chat-commands-button}
       [react/view
        [vi/icon :icons/input-commands {:container-style style/input-commands-icon
                                        :color           :dark}]]])))

(defview input-container []
  (letsubs [margin               [:chat-input-margin]
            {:keys [input-text]} [:get-current-chat]
            result-box           [:get-current-chat-ui-prop :result-box]]
    (let [single-line-input? (:singleLineInput result-box)]
      [react/view {:style     (style/root margin)
                   :on-layout #(let [h (-> (.-nativeEvent %)
                                           (.-layout)
                                           (.-height))]
                                 (when (> h 0)
                                   (re-frame/dispatch [:set-chat-ui-props {:input-height h}])))}
       [react/view {:style style/input-container}
        [input-view {:single-line-input? single-line-input?}]
        (if (string/blank? input-text)
          [commands-button]
          [send-button/send-button-view])]])))

(defn container []
  [react/view
   [parameter-box/parameter-box-view]
   [result-box/result-box-view]
   [suggestions/suggestions-view]
   [validation-messages/validation-messages-view]
   [input-container]])
