(ns legacy.status-im.data-store.switcher-cards
  (:require
    [clojure.set :as set]
    [clojure.walk :as walk]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn <-rpc
  [switcher-cards]
  (walk/postwalk-replace
   {:cardId   :card-id
    :screenId :screen-id}
   switcher-cards))

(defn rpc->
  [switcher-card]
  (set/rename-keys switcher-card
                   {:card-id   :cardId
                    :screen-id :screenId}))

(rf/defn upsert-switcher-card-rpc
  [_ switcher-card]
  {:json-rpc/call [{:method     "wakuext_upsertSwitcherCard"
                    :params     [(rpc-> switcher-card)]
                    :on-success #()
                    :on-error   #()}]})

(rf/defn delete-switcher-card-rpc
  [_ card-id]
  {:json-rpc/call [{:method     "wakuext_deleteSwitcherCard"
                    :params     [card-id]
                    :on-success #()
                    :on-error   #()}]})

(rf/defn fetch-switcher-cards-rpc
  [_]
  {:json-rpc/call [{:method     "wakuext_switcherCards"
                    :params     []
                    :on-success #(rf/dispatch
                                  [:shell/switcher-cards-loaded
                                   (:switcherCards ^js %)])
                    :on-error   #(log/error "Failed to fetch switcher cards" %)}]})
