(ns status-im.acquisition.advertiser
  (:require [status-im.utils.fx :as fx]
            [status-im.popover.core :as popover]
            [status-im.ethereum.core :as ethereum]
            [status-im.acquisition.gateway :as gateway]
            [status-im.acquisition.claim :as claim]
            [status-im.acquisition.persistance :as persistence]))

(fx/defn start-acquisition
  [cofx _]
  (popover/show-popover cofx
                        {:prevent-closing? true
                         :view             :advertiser-invite}))

(fx/defn advertiser-decide
  {:events [::decision]}
  [{:keys [db] :as cofx} decision]
  (let [referral (get-in db [:acquisition :referrer])
        payload  {:chat_key    (get-in db [:multiaccount :public-key])
                  :address     (ethereum/default-address db)
                  :invite_code referral}]
    (fx/merge cofx
              (if (= decision :accept)
                (gateway/handle-acquisition {:message    payload
                                             :method     "PATCH"
                                             :url        [:clicks referral]
                                             :on-success [::claim/success-starter-pack-claim]})
                {::persistence/set-referrer-state :declined})
              (popover/hide-popover))))
