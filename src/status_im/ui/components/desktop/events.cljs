(ns status-im.ui.components.desktop.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.platform :as platform]))

(fx/defn change-tab
  [{:keys [db]} tab-name]
  {:db (assoc-in db [:desktop/desktop :tab-view-id] tab-name)})

(fx/defn navigate-to
  [{:keys [db] :as cofx} tab-name]
  (navigation/navigate-to-cofx cofx
                               (if (and (= tab-name :home) (:current-chat-id db))
                                 :chat
                                 :home)
                               nil))

(fx/defn fetch-desktop-version
  [_ tab-name]
  (when (and platform/isMacOs?
             (= tab-name :profile))
    {:http-get
     {:url
      "https://raw.githubusercontent.com/status-im/status-im.github.io/develop/env.sh"
      :success-event-creator
      (fn [o]
        [:fetch-desktop-version-success o])}}))

(fx/defn show-desktop-tab
  [cofx tab-name]
  (fx/merge cofx
            (change-tab tab-name)
            (navigate-to tab-name)
            (fetch-desktop-version tab-name)))

(handlers/register-handler-fx
 :show-desktop-tab
 (fn [cofx [_ tab-name]]
   (show-desktop-tab cofx tab-name)))
