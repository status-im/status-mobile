(ns status-im.chat.views.input.input-actions
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.ui.components.react :as react]
            [status-im.chat.styles.input.input-actions :as style]))

(defmulti action-view (fn [{:keys [type]}] (keyword type)))

(defmethod action-view :fullscreen
  [_]
  (let [fullscreen? (re-frame/subscribe [:chat-ui-props :fullscreen?])]
    (fn []
      [react/touchable-highlight
       {:on-press #(re-frame/dispatch [:set-chat-ui-props {:fullscreen? (not @fullscreen?)}])}
       (if @fullscreen?
         [react/view (style/action-view true)
          [react/icon :action_fullscreen_collapse style/action-view-icon]]
         [react/view (style/action-view true)
          [react/icon :action_fullscreen_expand style/action-view-fullscreen-expand-icon]])])))

(defmethod action-view :web-view-back
  [_]
  (let [result-box (re-frame/subscribe [:chat-ui-props :result-box])
        webview    (re-frame/subscribe [:get :webview-bridge])]
    (fn []
      [react/touchable-highlight
       {:on-press #(.goBack @webview)}
       [react/view (style/action-view true)
        [react/icon :action_back style/action-view-icon-tinted]]]
      #_(if (:can-go-back? @result-box)
          [react/view (style/action-view false)
           [react/icon :action_back style/action-view-icon]]))))

(defmethod action-view :web-view-forward
  [_]
  (let [result-box (re-frame/subscribe [:chat-ui-props :result-box])
        webview    (re-frame/subscribe [:get :webview-bridge])]
    (fn []
      [react/touchable-highlight
       {:on-press #(.goForward @webview)}
       [react/view (style/action-view true)
        [react/icon :action_forward style/action-view-icon-tinted]]]
      #_(if (:can-go-forward? @result-box)
          [react/view (style/action-view false)
           [react/icon :action_forward style/action-view-icon]]))))

(defmethod action-view :default
  [{:keys [image executeJs]}]
  [react/touchable-highlight
   {:on-press #(re-frame/dispatch [:chat-webview-bridge/send-to-bridge
                                   {:event "actions-execute-js"
                                    :js    executeJs}])}
   [react/view (style/action-view true)
    [react/icon (str "action_" image) style/action-view-icon]]])

(defn input-actions-view []
  (let [result-box (re-frame/subscribe [:chat-ui-props :result-box])]
    (fn []
      (let [{:keys [actions]} @result-box]
        [react/view style/actions-container
         (for [{:keys [type] :as action} actions]
           ^{:key type}
           [action-view action])]))))
