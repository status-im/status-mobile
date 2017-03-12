(ns status-im.chat.views.input.input
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [taoensso.timbre :as log]
            [status-im.accessibility-ids :as id]
            [status-im.components.react :refer [view
                                                text
                                                scroll-view
                                                text-input
                                                icon
                                                touchable-highlight
                                                dismiss-keyboard!]]
            [status-im.chat.views.input.emoji :as emoji]
            [status-im.chat.views.input.parameter-box :as parameter-box]
            [status-im.chat.views.input.result-box :as result-box]
            [status-im.chat.views.input.suggestions :as suggestions]
            [status-im.chat.views.input.validation-messages :as validation-messages]
            [status-im.chat.styles.input.input :as style]
            [status-im.chat.utils :as utils]
            [status-im.chat.constants :as const]))

(defn command-view [{command-name :name :as command}]
  [touchable-highlight {:on-press #(dispatch [:select-chat-input-command command nil])}
   [view
    [text {:style style/command
           :font  :roboto-mono}
     (str const/command-char) command-name]]])

(defview commands-view []
  [commands [:chat :command-suggestions]]
  [view style/commands-root
   [view style/command-list-icon-container
    [touchable-highlight {:on-press #(do (dispatch [:toggle-chat-ui-props :show-suggestions?])
                                         (dispatch [:update-suggestions]))}
     [view style/commands-list-icon
      [icon :input_list style/commands-list-icon]]]]
   [scroll-view {:horizontal true}
    [view style/commands
     (for [[command-key command] (remove #(nil? (:title (second %))) commands)]
       ^{:key command-key}
       [command-view command])]]])

(defn- invisible-input [{:keys [set-layout-width value]}]
  [text {:style     style/invisible-input-text
         :on-layout #(let [w (-> (.-nativeEvent %)
                                 (.-layout)
                                 (.-width))]
                       (set-layout-width w))}
   (utils/safe-trim value)])

(defn- input-helper [{:keys [command width]}]
  (when (and command
             (empty? (:args command)))
    (when-let [placeholder (get-in command [:command :params 0 :placeholder])]
      [text {:style (style/input-helper-text width)}
       placeholder])))

(defn input-view []
  (let [component            (r/current-component)
        set-layout-width     #(r/set-state component {:width %})
        set-layout-height    #(r/set-state component {:height %})
        input-text           (subscribe [:chat :input-text])
        current-param        (subscribe [:chat :current-param])
        command              (subscribe [:selected-chat-command])
        sending-in-progress? (subscribe [:chat-ui-props :sending-in-progress?])]
    (r/create-class
      {:component-will-mount
       (fn []
         (dispatch [:update-suggestions]))

       :reagent-render
       (fn []
         (let [{:keys [width height]} (r/state component)
               command @command]
           [view (style/input-root height (str/blank? @input-text))
            [text-input
             {:ref                    #(dispatch [:set-chat-ui-props :input-ref %])
              :accessibility-label    id/chat-message-input
              :blur-on-submit         true
              :multiline              true
              :default-value          (or @input-text "")
              :editable               (not @sending-in-progress?)
              :on-blur                #(do (dispatch [:set-chat-ui-props :input-focused? false])
                                           (set-layout-height 0))
              :on-change-text         #(when-not (str/includes? % "\n")
                                         (do (dispatch [:set-chat-input-text %])
                                             (dispatch [:load-chat-parameter-box (:command command)])
                                             (when (not command)
                                               (dispatch [:set-chat-input-metadata nil])
                                               (dispatch [:set-chat-ui-props :result-box nil]))
                                             (dispatch [:set-chat-ui-props :validation-messages nil])))
              :on-content-size-change #(let [h (-> (.-nativeEvent %)
                                                   (.-contentSize)
                                                   (.-height))]
                                         (set-layout-height h))
              :on-selection-change    #(let [s (-> (.-nativeEvent %)
                                                   (.-selection))]
                                         (dispatch [:set-chat-ui-props :selection {:start (.-start s)
                                                                                   :end   (.-end s)}]))
              :on-submit-editing      #(do (dispatch [:set-chat-ui-props :sending-in-progress? true])
                                           (dispatch [:send-current-message]))
              :on-focus               #(do (dispatch [:set-chat-ui-props :input-focused? true])
                                           (dispatch [:set-chat-ui-props :show-emoji? false]))
              :style                  style/input-view}]
            [invisible-input {:value            @input-text
                              :set-layout-width set-layout-width}]
            [input-helper {:command command
                           :width   width}]
            (if-not command
              [touchable-highlight
               {:on-press #(do (dispatch [:toggle-chat-ui-props :show-emoji?])
                               (dismiss-keyboard!))}
               [view
                [icon :smile style/input-emoji-icon]]]
              [touchable-highlight
               {:on-press #(do (dispatch [:set-chat-input-text nil])
                               (dispatch [:set-chat-input-metadata nil])
                               (dispatch [:set-chat-ui-props :result-box nil])
                               (dispatch [:set-chat-ui-props :validation-messages nil]))}
               [view style/input-clear-container
                [icon :close_gray style/input-clear-icon]]])]))})))

(defview input-container []
  [command-complete? [:command-complete?]
   selected-command [:selected-chat-command]
   input-text [:chat :input-text]]
  [view style/input-container
   [input-view]
   (when (and (not (str/blank? input-text))
              (or command-complete? (not selected-command)))
     [touchable-highlight {:on-press #(do (dispatch [:set-chat-ui-props :sending-in-progress? true])
                                          (dispatch [:send-current-message]))}
      [view style/send-message-container
       [icon :arrow_top style/send-message-icon]]])])

(defview container []
  [margin [:chat-input-margin]
   show-emoji? [:chat-ui-props :show-emoji?]
   input-text [:chat :input-text]]
  [view
   [parameter-box/parameter-box-view]
   [result-box/result-box-view]
   [suggestions/suggestions-view]
   [validation-messages/validation-messages-view]
   [view {:style     (style/root margin)
          :on-layout #(let [h (-> (.-nativeEvent %)
                                  (.-layout)
                                  (.-height))]
                        (dispatch [:set-chat-ui-props :input-height h]))}
    [view (style/container (str/blank? input-text))
     (when (str/blank? input-text)
       [commands-view])
     [input-container]]
    (when show-emoji?
      [emoji/emoji-view])]])
