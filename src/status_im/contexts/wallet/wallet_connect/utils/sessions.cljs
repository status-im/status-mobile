(ns status-im.contexts.wallet.wallet-connect.utils.sessions
  (:require
    [clojure.string :as string]
    [utils.transforms :as transforms]))

(defn sdk-session->db-session
  [{:keys [topic expiry pairingTopic] :as session}]
  {:topic        topic
   :expiry       expiry
   :sessionJson  (transforms/clj->json session)
   :pairingTopic pairingTopic
   :name         (get-in session [:peer :metadata :name])
   :iconUrl      (get-in session [:peer :metadata :icons 0])
   :url          (get-in session [:peer :metadata :url])
   :accounts     (get-in session [:namespaces :eip155 :accounts])
   :chains       (get-in session [:namespaces :eip155 :chains])
   :disconnected false})

(defn filter-operable-accounts
  [accounts]
  (filter #(and (:operable? %)
                (not (:watch-only? %)))
          accounts))

(defn filter-sessions-for-account-addresses
  [account-addresses sessions]
  (filter (fn [{:keys [accounts]}]
            (some (fn [account]
                    (some (fn [account-address]
                            (string/includes? account account-address))
                          account-addresses))
                  accounts))
          sessions))

(defn latest-connected-account-address
  [sessions]
  (let [all-accounts (->> sessions
                          (sort-by :expiry >)
                          first
                          :accounts)]
    (-> all-accounts
        first
        (string/split #":")
        last)))
