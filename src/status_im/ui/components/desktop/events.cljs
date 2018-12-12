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

(fx/defn show-desktop-tab
  [cofx tab-name]
  (fx/merge cofx
            (change-tab tab-name)
            (navigate-to tab-name)))

(handlers/register-handler-fx
 :show-desktop-tab
 (fn [cofx [_ tab-name]]
   (show-desktop-tab cofx tab-name)))
