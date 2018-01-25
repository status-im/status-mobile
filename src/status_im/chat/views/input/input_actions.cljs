(ns status-im.chat.views.input.input-actions
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.ui.components.react :as react]
            [status-im.chat.styles.input.input-actions :as style]))

(defmulti action-view (fn [{:keys [type]}] (keyword type)))

(defmethod action-view :default
  [{:keys [image executeJs]}]
  [react/touchable-highlight
   {:on-press #(re-frame/dispatch [:chat-webview-bridge/send-to-bridge
                                   {:event "actions-execute-js"
                                    :js    executeJs}])}
   [react/view (style/action-view true)
    [react/icon (str "action_" image) style/action-view-icon]]])

(defn input-actions-view []
  (let [result-box (re-frame/subscribe [:get-current-chat-ui-prop :result-box])]
    (fn []
      (let [{:keys [actions]} @result-box]
        [react/view style/actions-container
         (for [{:keys [type] :as action} actions]
           ^{:key type}
           [action-view action])]))))