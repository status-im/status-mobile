(ns status-im.ui.components.desktop.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.chat.models :as chat.models]))

(fx/defn go-to-buidl
  [cofx]
  (fx/merge cofx
            {:db (assoc (:db cofx)
                        :current-chat-id "status-buidl-test"
                        :view-id :buidl)}
            (chat.models/start-public-chat-without-navigation "status-buidl-test")))

(fx/defn show-desktop-tab
  [{:keys [db] :as cofx} tab-name]
  (fx/merge cofx
            {:db (assoc-in db [:desktop/desktop :tab-view-id] tab-name)}
            (if (= tab-name :buidl)
              (go-to-buidl)
              (navigation/navigate-to-cofx (if (and (= tab-name :home)
                                                    (:current-chat-id db))
                                             :chat
                                             :home)
                                           {}))))

(handlers/register-handler-fx
 :show-desktop-tab
 (fn [cofx [_ tab-name]]
   (show-desktop-tab cofx tab-name)))
