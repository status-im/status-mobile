(ns status-im.ui.screens.wallet.collectibles.cryptokitties.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.wallet.collectibles.events :as collectibles]
            [status-im.utils.http :as http]))

(def ck :CK)

(handlers/register-handler-fx
 :load-kitties
 (fn [{db :db} [_ ids]]
   {:db db
    :http-get-n (mapv (fn [id]
                        {:url (str "https://api.cryptokitties.co/kitties/" id)
                         :success-event-creator (fn [o]
                                                  [:load-collectible-success ck {id (http/parse-payload o)}])
                         :failure-event-creator (fn [o]
                                                  [:load-collectible-failure ck {id (http/parse-payload o)}])})
                      ids)}))

;; TODO(andrey) Each HTTP call will return up to 100 kitties. Maybe we need to implement some kind of paging later
(defmethod collectibles/load-collectibles-fx ck [_ _ _ items-number address _]
  {:http-get {:url                   (str "https://api.cryptokitties.co/kitties?offset=0&limit="
                                          items-number
                                          "&owner_wallet_address="
                                          address
                                          "&parents=false")
              :success-event-creator (fn [o]
                                       [:load-kitties (map :id (:kitties (http/parse-payload o)))])
              :failure-event-creator (fn [o]
                                       [:load-collectibles-failure (http/parse-payload o)])
              :timeout-ms            10000}})