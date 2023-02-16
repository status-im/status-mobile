(ns status-im.communities.mock
  (:require [status-im2.constants :as constants]))

(def status-channel-to-mock "5c328f54-4d40-4984-ae96-b946247d8dba")

(def community-gates
  {:join [[{:token  "KNC"
            :amount 200}
           {:token  "MANA"
            :amount 10}
           {:token  "RARE"
            :amount 50}]
          [{:token  "KNC"
            :amount 200}
           {:token  "MANA"
            :amount 50}]]})

(def channel-gates
  {:read  [[{:token  "KNC"
             :amount 100}
            {:token  "RARE"
             :amount 50}]
           [{:token  "KNC"
             :amount 200}
            {:token  "MANA"
             :amount 50}]]
   :write [[{:token  "KNC"
             :amount 400}]
           [{:token  "KNC"
             :amount 100}
            {:token  "RARE"
             :amount 50}]]})

(defn add-mock-data
  [db]
  (if (get-in db [:communities constants/status-community-id])
    (-> db
        (assoc-in [:communities constants/status-community-id :gates] community-gates)
        (assoc-in [:communities constants/status-community-id :status] :gated)
        (assoc-in [:communities constants/status-community-id :chats status-channel-to-mock :gates]
                  channel-gates))
    db))
