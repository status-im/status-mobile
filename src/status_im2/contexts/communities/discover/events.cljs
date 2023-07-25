(ns status-im2.contexts.communities.discover.events
  (:require [camel-snake-kebab.core :as csk]
            [clojure.string :as string]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(defn rename-contract-community-key
  [k]
  (let [s                  (name k)
        lower-cased        (csk/->kebab-case-string s)
        starts-with-digit? (re-matches #"^\d.*" s)
        predicate?         (some #(string/starts-with? lower-cased %)
                                 ["can-" "is-"])]
    (cond starts-with-digit? s
          predicate?         (keyword (str lower-cased "?"))
          :else              (keyword lower-cased))))

(defn rename-contract-community-keys
  [m]
  (reduce (fn [acc [k v]]
            (let [new-key (if (keyword? k) (rename-contract-community-key k) k)]
              (cond
                (map? v) (assoc acc new-key (rename-contract-community-keys v))
                :else    (assoc acc new-key v))))
          {}
          m))

(rf/defn handle-contract-communities
  {:events [:fetched-contract-communities]}
  [{:keys [db]} contract-communities]
  (let [cc       (rename-contract-community-keys contract-communities)
        featured (:contract-featured-communities cc)
        other    (remove (set featured) (:contract-communities cc))]
    {:db (assoc db
                :contract-communities
                {:featured (select-keys (:communities cc) featured)
                 :other    (select-keys (:communities cc) other)})}))

(rf/defn fetch-contract-communities
  [_]
  {:json-rpc/call [{:method     "wakuext_curatedCommunities"
                    :params     []
                    :on-success #(rf/dispatch [:fetched-contract-communities %])
                    :on-error   #(log/error "failed to fetch contract communities" %)}]})

