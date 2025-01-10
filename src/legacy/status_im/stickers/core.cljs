(ns legacy.status-im.stickers.core
  (:require
    [legacy.status-im.utils.utils :as utils]
    [re-frame.core :as re-frame]
    [status-im.constants :as constants]
    [utils.ethereum.chain :as chain]
    [utils.re-frame :as rf]))

(re-frame/reg-fx
 :stickers/set-pending-timeout-fx
 (fn []
   (utils/set-timeout #(re-frame/dispatch [:stickers/pending-timeout])
                      10000)))

(rf/defn install-stickers-pack
  {:events [:stickers/install-pack]}
  [{db :db :as cofx} id]
  (rf/merge
   cofx
   {:db            (assoc-in db [:stickers/packs id :status] constants/sticker-pack-status-installed)
    :json-rpc/call [{:method     "stickers_install"
                     :params     [(chain/chain-id db) id]
                     :on-success #()}]}))

(rf/defn pending-pack
  {:events [:stickers/pending-pack]}
  [{db :db} id]
  {:db                              (-> db
                                        (assoc-in [:stickers/packs id :status]
                                                  constants/sticker-pack-status-pending)
                                        (update :stickers/packs-pending conj id))
   :stickers/set-pending-timeout-fx nil
   :json-rpc/call                   [{:method     "stickers_addPending"
                                      :params     [(chain/chain-id db) (int id)]
                                      :on-success #()}]})

(rf/defn pending-timeout
  {:events [:stickers/pending-timeout]}
  [{{:stickers/keys [packs-pending] :as db} :db}]
  (when (seq packs-pending)
    {:json-rpc/call [{:method     "stickers_processPending"
                      :params     [(chain/chain-id db)]
                      :on-success #(re-frame/dispatch [:stickers/stickers-process-pending-success
                                                       %])}]}))

(rf/defn stickers-process-pending-success
  {:events [:stickers/stickers-process-pending-success]}
  [{{:stickers/keys [packs-pending packs] :as db} :db} purchased]
  (let [purchased-ids (map :id (vals purchased))
        packs-pending (apply disj packs-pending purchased-ids)
        packs         (reduce (fn [packs id]
                                (assoc-in packs [id :status] constants/sticker-pack-status-owned))
                              packs
                              purchased-ids)]
    (merge
     {:db (-> db
              (assoc :stickers/packs packs)
              (assoc :stickers/packs-pending packs-pending))}
     (when (seq packs-pending)
       {:stickers/set-pending-timeout-fx nil}))))

(rf/defn stickers-market-success
  {:events [:stickers/stickers-market-success]}
  [{:keys [db]} packs]
  (let [packs (reduce (fn [acc pack] (assoc acc (:id pack) pack)) {} packs)]
    {:db (update db :stickers/packs merge packs)}))

(rf/defn stickers-installed-success
  {:events [:stickers/stickers-installed-success]}
  [{:keys [db]} packs]
  (let [packs (reduce (fn [acc [_ pack]] (assoc acc (:id pack) pack)) {} packs)]
    {:db (update db :stickers/packs merge packs)}))

(rf/defn stickers-pending-success
  {:events [:stickers/stickers-pending-success]}
  [{:keys [db]} packs]
  (let [packs (reduce (fn [acc [_ pack]] (assoc acc (:id pack) pack)) {} packs)]
    (merge
     {:db (-> db
              (assoc :stickers/packs-pending (into #{} (keys packs)))
              (update :stickers/packs merge packs))}
     (when (seq packs)
       {:stickers/set-pending-timeout-fx nil}))))

(rf/defn stickers-recent-success
  {:events [:stickers/stickers-recent-success]}
  [{:keys [db]} packs]
  {:db (assoc db :stickers/recent-stickers packs)})

(rf/defn select-pack
  {:events [:stickers/select-pack]}
  [{:keys [db]} id]
  {:db (assoc db :stickers/selected-pack id)})
