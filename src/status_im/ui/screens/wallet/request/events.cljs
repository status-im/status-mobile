(ns status-im.ui.screens.wallet.request.events
  (:require
    [re-frame.core :as re-frame :refer [dispatch reg-fx]]
    [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
  :wallet-send-request
  (fn [{{:wallet/keys [request-transaction]} :db} [_ {:keys [whisper-identity] :as contact}]]
    {:dispatch-n [[:navigate-back]
                  [:navigate-to-clean :chat-list]
                  [:chat-with-command whisper-identity :request
                   {:contact contact
                    :amount (:amount request-transaction)}]]}))