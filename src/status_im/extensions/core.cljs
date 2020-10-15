(ns status-im.extensions.core
  (:require [status-im.utils.fx :as fx]
            [re-frame.core :as re-frame]
            ["status-ethereum-provider" :default status-ethereum-provider]
            [status-im.utils.types :as types]))

(def provider ^js (status-ethereum-provider #(re-frame/dispatch [:browser/extension-message-received %])))

(re-frame/reg-fx
 :on-message
 (fn [message]
   (when message
     (.onMessage provider (types/clj->json message)))))

(fx/defn send-to-bridge
  {:events [:extension/send-to-bridge]}
  [{:keys [db]} msg]
  {:on-message (update msg :messageId #(subs % 3))})

(defn send-command [ext]
  (fn [params]
    (re-frame/dispatch [:send-extension-command {:id     (:id ext)
                                                 :params (.stringify js/JSON ^js params)}])))

(defn send-text-message [_]
  (fn [value]
    (re-frame/dispatch [:send-plain-text-message value])))

(re-frame/reg-fx
 ::init-extension
 (fn [ext]
   (doseq [hook (:hooks ext)]
     (when (:init hook)
       ((:init hook) #js {:ethereum provider
                          :sendCommand (send-command ext)
                          :sendTextMessage (send-text-message ext)
                          :selectAddress #(re-frame/dispatch [:wallet.send/navigate-to-recipient-code])
                          :close #(if (= (:type hook) "WALLET_MAIN_SCREEN_WINDOW")
                                    (re-frame/dispatch [:navigate-back])
                                    (re-frame/dispatch [:set :current-extension-sheet nil]))})))))

(fx/defn join-time-messages-checked-for-chats
  {:events [:switch-extension]}
  [{:keys [db]} {:keys [id] :as ext}]
  (merge
   {:db (update-in db [:extensions id] #(when-not % ext))}
   (when-not (get-in db [:extensions id])
     {::init-extension ext})))