(ns status-im.chat.views.input.input-actions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [taoensso.timbre :as log]
            [status-im.ui.components.react :refer [view
                                                text
                                                icon
                                                touchable-highlight]]
            [status-im.chat.styles.input.input-actions :as style]))

(defmulti action-view (fn [{:keys [type]}] (keyword type)))

(defmethod action-view :fullscreen
  [_]
  (let [fullscreen? (subscribe [:chat-ui-props :fullscreen?])]
    (fn []
      [touchable-highlight
       {:on-press #(dispatch [:set-chat-ui-props {:fullscreen? (not @fullscreen?)}])}
       (if @fullscreen?
         [view (style/action-view true)
          [icon :action_fullscreen_collapse style/action-view-icon]]
         [view (style/action-view true)
          [icon :action_fullscreen_expand style/action-view-fullscreen-expand-icon]])])))

(defmethod action-view :web-view-back
  [_]
  (let [result-box (subscribe [:chat-ui-props :result-box])
        webview    (subscribe [:get :webview-bridge])]
    (fn []
      [touchable-highlight
       {:on-press #(.goBack @webview)}
       [view (style/action-view true)
        [icon :action_back style/action-view-icon-tinted]]]
      #_(if (:can-go-back? @result-box)

        [view (style/action-view false)
         [icon :action_back style/action-view-icon]]))))

(defmethod action-view :web-view-forward
  [_]
  (let [result-box (subscribe [:chat-ui-props :result-box])
        webview    (subscribe [:get :webview-bridge])]
    (fn []
      [touchable-highlight
       {:on-press #(.goForward @webview)}
       [view (style/action-view true)
        [icon :action_forward style/action-view-icon-tinted]]]
      #_(if (:can-go-forward? @result-box)

        [view (style/action-view false)
         [icon :action_forward style/action-view-icon]]))))

(defmethod action-view :default
  [{:keys [image executeJs]}]
  [touchable-highlight
   {:on-press #(dispatch [:send-to-webview-bridge {:event "actions-execute-js"
                                                   :js    executeJs}])}
   [view (style/action-view true)
    [icon (str "action_" image) style/action-view-icon]]])

(defn input-actions-view []
  (let [result-box (subscribe [:chat-ui-props :result-box])]
    (fn []
      (let [{:keys [actions]} @result-box]
        [view style/actions-container
         (for [{:keys [type] :as action} actions]
           ^{:key type}
           [action-view action])]))))
