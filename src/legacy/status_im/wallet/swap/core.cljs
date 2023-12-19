(ns legacy.status-im.wallet.swap.core
  (:require
    [re-frame.core :as re-frame]
    [re-frame.db :as re-frame.db]
    [status-im2.navigation.events :as navigation]
    [utils.re-frame :as rf]))

;; "source? true" means we are selecting the source asset. false implies
;; selection of sink asset."
(re-frame/reg-event-fx ::open-asset-selector-modal
 (fn [{:keys [db]} [source?]]
   {:db (assoc db :wallet-legacy/modal-selecting-source-token? source?)
    :fx [[:dispatch [:open-modal :swap-asset-selector {}]]]}))

(rf/defn set-from-token
  {:events [::set-from-token]}
  [{:keys [db]} from-symbol]
  (rf/merge {:db (assoc db :wallet-legacy/swap-from-token from-symbol)}
            (navigation/navigate-back)))

(rf/defn set-to-token
  {:events [::set-to-token]}
  [{:keys [db]} to-symbol]
  (rf/merge {:db (assoc db :wallet-legacy/swap-to-token to-symbol)}
            (navigation/navigate-back)))

(rf/defn set-from-token-amount
  [{:keys [db]} from-amount]
  {:db (assoc db :wallet-legacy/swap-from-token-amount from-amount)})

(rf/defn set-max-from-token-amount
  [{:keys [db]} _]
  {:db (assoc db :wallet-legacy/swap-from-token-amount 0)})

(rf/defn switch-from-token-with-to
  {:events [::switch-from-token-with-to]}
  [{:keys [db]}]
  {:db (assoc db
              :wallet-legacy/swap-from-token (:wallet-legacy/swap-to-token db)
              :wallet-legacy/swap-to-token   (:wallet-legacy/swap-from-token db))})

(rf/defn set-advanced-mode
  {:events [::set-advanced-mode]}
  [{:keys [db]} mode]
  {:db (assoc db :wallet-legacy/swap-advanced-mode? mode)})

(comment
  (->> re-frame.db/app-db
       deref
       :wallet-legacy/all-tokens
       vals
       (map #(str (:name %) "-" (:symbol %)))))
