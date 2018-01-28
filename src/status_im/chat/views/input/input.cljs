(ns status-im.chat.views.input.input
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [taoensso.timbre :as log]
            [status-im.chat.constants :as const]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.models.commands :as commands-model]
            [status-im.chat.styles.input.input :as style] 
            [status-im.chat.views.input.emoji :as emoji]
            [status-im.chat.views.input.parameter-box :as parameter-box]
            [status-im.chat.views.input.input-actions :as input-actions]
            [status-im.chat.views.input.result-box :as result-box]
            [status-im.chat.views.input.suggestions :as suggestions]
            [status-im.chat.views.input.validation-messages :as validation-messages]
            [status-im.ui.components.animation :as anim]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]))

(defn command-view [first? command]
  [react/touchable-highlight {:on-press #(dispatch [:select-chat-input-command command nil])}
   [react/view
    [react/text {:style (style/command first?)
                 :font  :roboto-mono}
     (commands-model/command-name command)]]])

(defview commands-view []
  [all-commands-responses [:get-available-commands-responses]
   show-suggestions? [:show-suggestions?]]
  [react/view style/commands-root
   [react/view style/command-list-icon-container
    [react/touchable-highlight {:on-press #(dispatch [:show-suggestions])}
     [react/view style/commands-list-icon
      (if show-suggestions?
        [vi/icon :icons/close]
        [vi/icon :icons/commands-list])]]]
   [react/scroll-view {:horizontal                     true
                       :showsHorizontalScrollIndicator false
                       :keyboardShouldPersistTaps      :always}
    [react/view style/commands
     (for [[index command] (map-indexed vector all-commands-responses)]
       ^{:key (str "command-" index)}
       [command-view (= index 0) command])]]])

(defn- basic-text-input [_]
  (let [input-text     (subscribe [:chat :input-text])
        command        (subscribe [:selected-chat-command])
        input-focused? (subscribe [:get-current-chat-ui-prop :input-focused?])
        input-ref      (atom nil)]
    (fn [{:keys [set-layout-height-fn set-container-width-fn height single-line-input?]}]
      [react/text-input
       {:ref                    #(when %
                                   (dispatch [:set-chat-ui-props {:input-ref %}])
                                   (reset! input-ref %))
        :accessibility-label    :chat-message-input
        :multiline              (not single-line-input?)
        :default-value          (or @input-text "")
        :editable               true
        :blur-on-submit         false
        :on-focus               #(dispatch [:set-chat-ui-props {:input-focused? true
                                                                :show-emoji?    false}])
        :on-blur                #(dispatch [:set-chat-ui-props {:input-focused? false}])
        :on-submit-editing      (fn [e]
                                  (if single-line-input?
                                    (dispatch [:send-current-message])
                                    (.setNativeProps @input-ref (clj->js {:text (str @input-text "\n")}))))
        :on-layout              (fn [e]
                                  (set-container-width-fn (.-width (.-layout (.-nativeEvent e)))))
        :on-change              (fn [e]
                                  (let [native-event (.-nativeEvent e)
                                        text         (.-text native-event)
                                        content-size (.. native-event -contentSize)]
                                    (when (and (not single-line-input?)
                                               content-size)
                                      (set-layout-height-fn (.-height content-size)))
                                    (when (not= text @input-text)
                                      (dispatch [:set-chat-input-text text])
                                      (when @command
                                        (dispatch [:load-chat-parameter-box (:command @command)]))
                                      (dispatch [:update-input-data]))))
        :on-content-size-change (when (and (not @input-focused?)
                                           (not single-line-input?))
                                  #(let [h (-> (.-nativeEvent %)
                                               (.-contentSize)
                                               (.-height))]
                                     (set-layout-height-fn h)))
        :on-selection-change    #(let [s   (-> (.-nativeEvent %)
                                               (.-selection)) 
                                       end (.-end s)] 
                                   (dispatch [:update-text-selection end]))
        :style                  (style/input-view height single-line-input?)
        :placeholder-text-color style/color-input-helper-placeholder
        :auto-capitalize        :sentences}])))

(defn- invisible-input [{:keys [set-layout-width-fn value]}]
  (let [input-text    (subscribe [:chat :input-text])]
    [react/text {:style     style/invisible-input-text
                 :on-layout #(let [w (-> (.-nativeEvent %)
                                         (.-layout)
                                         (.-width))]
                               (set-layout-width-fn w))}
     (or @input-text "")]))

(defn- invisible-input-height [{:keys [set-layout-height-fn container-width]}]
  (let [input-text    (subscribe [:chat :input-text])]
    [react/text {:style     (style/invisible-input-text-height container-width)
                 :on-layout #(let [h (-> (.-nativeEvent %)
                                         (.-layout)
                                         (.-height))]
                               (set-layout-height-fn h))}
     (or @input-text "")]))

(defn- input-helper [_]
  (let [input-text (subscribe [:chat :input-text])]
    (fn [{:keys [command width]}]
      (when-not (get-in command [:command :sequential-params])
        (let [input (str/trim (or @input-text ""))
              real-args (remove str/blank? (:args command))]
          (when-let [placeholder (cond
                                   (= const/command-char input)
                                   (i18n/label :t/type-a-command)

                                   (and command (empty? real-args))
                                   (get-in command [:command :params 0 :placeholder])

                                   (and command
                                        (= (count real-args) 1)
                                        (input-model/text-ends-with-space? input))
                                   (get-in command [:command :params 1 :placeholder]))]
            [react/text {:style (style/input-helper-text width)}
             placeholder]))))))

(defn get-options [type]
  (case (keyword type)
    :phone {:keyboard-type "phone-pad"}
    :password {:secure-text-entry true}
    :number {:keyboard-type "numeric"}
    nil))

(defn- seq-input [_]
  (let [command            (subscribe [:selected-chat-command])
        arg-pos            (subscribe [:current-chat-argument-position])
        seq-arg-input-text (subscribe [:chat :seq-argument-input-text])]
    (fn [{:keys [command-width container-width]}]
      (when (get-in @command [:command :sequential-params])
        (let [{:keys [placeholder hidden type]} (get-in @command [:command :params @arg-pos])]
          [react/text-input (merge {:ref                 #(dispatch [:set-chat-ui-props {:seq-input-ref %}])
                                    :style               (style/seq-input-text command-width container-width)
                                    :default-value       (or @seq-arg-input-text "")
                                    :on-change-text      #(do (dispatch [:set-chat-seq-arg-input-text %])
                                                              (dispatch [:load-chat-parameter-box (:command @command)])
                                                              (dispatch [:set-chat-ui-props {:validation-messages nil}]))
                                    :placeholder         placeholder
                                    :accessibility-label :chat-request-input
                                    :blur-on-submit      false
                                    :editable            true
                                    :on-focus            #(dispatch [:set-chat-ui-props {:show-emoji? false}])
                                    :on-submit-editing   (fn []
                                                           (when-not (or (str/blank? @seq-arg-input-text)
                                                                         (get-in @command [:command :hide-send-button]))
                                                             (dispatch [:send-seq-argument]))
                                                           (js/setTimeout
                                                             #(dispatch [:chat-input-focus :seq-input-ref])
                                                             100))}
                                   (get-options type))])))))

(defn input-view [_]
  (let [component              (r/current-component)
        set-layout-width-fn    #(r/set-state component {:width %})
        set-layout-height-fn   #(r/set-state component {:height %})
        set-container-width-fn #(r/set-state component {:container-width %})
        command                (subscribe [:selected-chat-command])]
    (r/create-class
      {:display-name "input-view"
       :reagent-render
       (fn [{:keys [anim-margin single-line-input?]}]
         (let [{:keys [width height container-width]} (r/state component)
               command @command]
           [react/animated-view {:style (style/input-root height anim-margin)}
            [invisible-input {:set-layout-width-fn set-layout-width-fn}]
            [invisible-input-height {:set-layout-height-fn set-layout-height-fn
                                     :container-width container-width}]
            [basic-text-input {:set-layout-height-fn   set-layout-height-fn
                               :set-container-width-fn set-container-width-fn
                               :height                 height
                               :single-line-input?     single-line-input?}]
            [input-helper {:command command
                           :width   width}]
            [seq-input {:command-width   width
                        :container-width container-width}]
            (if-not command
              [react/touchable-highlight
               {:on-press #(do (dispatch [:toggle-chat-ui-props :show-emoji?])
                               (react/dismiss-keyboard!))}
               [react/view
                [vi/icon :icons/smile {:container-style style/input-emoji-icon}]]]
              (when-not single-line-input?
                [react/touchable-highlight
                 {:on-press #(do (dispatch [:set-chat-input-text nil])
                                 (dispatch [:set-chat-input-metadata nil])
                                 (dispatch [:set-chat-ui-props {:result-box          nil
                                                                :validation-messages nil}])
                                 (dispatch [:clear-seq-arguments]))}
                 [react/view style/input-clear-container
                  [vi/icon :icons/close]]]))]))})))

(defview input-container [{:keys [anim-margin]}]
  (letsubs [command-completion [:command-completion]
            selected-command   [:selected-chat-command]
            input-text         [:chat :input-text]
            seq-arg-input-text [:chat :seq-argument-input-text]
            result-box         [:get-current-chat-ui-prop :result-box]]
    (let [single-line-input? (:singleLineInput result-box)
          {:keys [hide-send-button sequential-params]} (:command selected-command)]
      [react/view style/input-container
       [input-view {:anim-margin        anim-margin
                    :single-line-input? single-line-input?}]
       (if (:actions result-box)
         [input-actions/input-actions-view]
         (when (and (not (str/blank? input-text))
                    (or (not selected-command)
                        (some #{:complete :less-than-needed} [command-completion]))
                    (not hide-send-button))
           [react/touchable-highlight {:on-press #(if sequential-params
                                                    (do
                                                      (when-not (str/blank? seq-arg-input-text)
                                                        (dispatch [:send-seq-argument]))
                                                      (js/setTimeout
                                                        (fn [] (dispatch [:chat-input-focus :seq-input-ref]))
                                                        100))
                                                    (dispatch [:send-current-message]))}
            [react/view {:style               style/send-message-container
                         :accessibility-label :send-message-button}
             [react/icon :arrow_top style/send-message-icon]]]))])))

(defn container []
  (let [margin                (subscribe [:chat-input-margin])
        show-emoji?           (subscribe [:get-current-chat-ui-prop :show-emoji?])
        input-text            (subscribe [:chat :input-text])
        anim-margin           (anim/create-value 10)
        container-anim-margin (anim/create-value 16)
        bottom-anim-margin    (anim/create-value 14)]
    (r/create-class
      {:display-name "input-container"
       :component-did-mount
       (fn []
         (when-not (str/blank? @input-text)
           (.setValue anim-margin 0)
           (.setValue container-anim-margin 8)
           (.setValue bottom-anim-margin 8)))
       :component-did-update
       (fn [component]
         (let [{:keys [text-empty?]} (reagent.core/props component)]
           (let [to-anim-value           (if text-empty? 10 0)
                 to-container-anim-value (if text-empty? 16 8)
                 to-bottom-anim-value    (if text-empty? 14 8)]
             (anim/start
               (anim/timing anim-margin {:toValue  to-anim-value
                                         :duration 100}))
             (anim/start
               (anim/timing container-anim-margin {:toValue  to-container-anim-value
                                                   :duration 100}))
             (anim/start
               (anim/timing bottom-anim-margin {:toValue  to-bottom-anim-value
                                                :duration 100})))))

       :reagent-render
       (fn []
         [react/view style/input-container-view
          [parameter-box/parameter-box-view]
          [result-box/result-box-view]
          [suggestions/suggestions-view]
          [validation-messages/validation-messages-view]
          [react/view {:style     (style/root @margin)
                       :on-layout #(let [h (-> (.-nativeEvent %)
                                               (.-layout)
                                               (.-height))]
                                     (when (> h 0)
                                       (dispatch [:set-chat-ui-props {:input-height h}])))}
           [react/animated-view {:style (style/container container-anim-margin bottom-anim-margin)}
            (when (str/blank? @input-text)
              [commands-view])
            [input-container {:anim-margin anim-margin}]]
           (when @show-emoji?
             [emoji/emoji-view])]])})))
