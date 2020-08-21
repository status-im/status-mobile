(ns status-im.acquisition.chat
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.acquisition.claim :as claim]
            [status-im.ethereum.core :as ethereum]
            [status-im.acquisition.persistance :as persistence]
            [status-im.acquisition.gateway :as gateway]
            [status-im.chat.models :as chat]))

(fx/defn start-acquisition
  [{:keys [db] :as cofx} {:keys [key] :as referrer}]
  {:db                             (assoc-in db [:acquisition :chat-referrer key] referrer)
   ::persistence/chat-initialized? (fn [state]
                                     (when-not (= "initialized" state)
                                       (re-frame/dispatch [::start-chat referrer])))})

(fx/defn start-chat
  {:events [::start-chat]}
  [cofx {:keys [key] :as referrer}]
  (fx/merge cofx
            {::persistence/chat-initalized! true}
            (chat/start-chat key)))

(fx/defn accept-pack
  {:events [::accept-pack]}
  [{:keys [db] :as cofx}]
  (let [referral (get-in db [:acquisition :referrer])
        payload  {:chat_key    (get-in db [:multiaccount :public-key])
                  :address     (ethereum/default-address db)
                  :invite_code referral}]
    (fx/merge cofx
              {:db (update db :acquisition dissoc :chat-referrer)}
              (gateway/handle-acquisition {:message    payload
                                           :method     "PATCH"
                                           :url        [:clicks referral]
                                           :on-success [::claim/success-starter-pack-claim]}))))
