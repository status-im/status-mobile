(ns status-im.acquisition.dapp
  (:require [status-im.utils.fx :as fx]
            [status-im.popover.core :as popover]
            [status-im.ethereum.core :as ethereum]
            [status-im.acquisition.gateway :as gateway]
            [status-im.acquisition.claim :as claim]
            [status-im.browser.core :as browser]
            [status-im.utils.security :as security]
            [status-im.acquisition.persistance :as persistence]))

(fx/defn start-acquisition
  [cofx _]
  (popover/show-popover cofx
                        {:prevent-closing? true
                         :view             :dapp-invite}))

(fx/defn succes-claim
  {:events [::success-claim]}
  [cofx response]
  (let [link (get-in cofx [:db :acquisition :metadata :url])]
    (fx/merge cofx
              (when (security/safe-link? link)
                (browser/open-url link))
              (claim/success-starter-pack-claim response))))

(fx/defn dapp-decision
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
                                             :on-success [::success-claim]})
                {::persistence/set-referrer-state :declined})
              (popover/hide-popover))))
