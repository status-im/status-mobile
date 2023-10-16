(ns status-im.wallet.swap.core
  (:require
    [re-frame.core :as re-frame]
    [re-frame.db :as re-frame.db]
    [status-im2.navigation.events :as navigation]
    [utils.re-frame :as rf]))

;; "source? true" means we are selecting the source asset. false implies
;; selection of sink asset."
(re-frame/reg-event-fx ::open-asset-selector-modal
 (fn [{:keys [db]} [source?]]
   {:db (assoc db :wallet/modal-selecting-source-token? source?)
    :fx [[:dispatch [:open-modal :swap-asset-selector {}]]]}))

(rf/defn set-from-token
  {:events [::set-from-token]}
  [{:keys [db]} from-symbol]
  (rf/merge {:db (assoc db :wallet/swap-from-token from-symbol)}
            (navigation/navigate-back)))

(rf/defn set-to-token
  {:events [::set-to-token]}
  [{:keys [db]} to-symbol]
  (rf/merge {:db (assoc db :wallet/swap-to-token to-symbol)}
            (navigation/navigate-back)))

(rf/defn set-from-token-amount
  [{:keys [db]} from-amount]
  {:db (assoc db :wallet/swap-from-token-amount from-amount)})

(rf/defn set-max-from-token-amount
  [{:keys [db]} _]
  {:db (assoc db :wallet/swap-from-token-amount 0)})

(rf/defn switch-from-token-with-to
  {:events [::switch-from-token-with-to]}
  [{:keys [db]}]
  {:db (assoc db
              :wallet/swap-from-token (:wallet/swap-to-token db)
              :wallet/swap-to-token   (:wallet/swap-from-token db))})

(rf/defn set-advanced-mode
  {:events [::set-advanced-mode]}
  [{:keys [db]} mode]
  {:db (assoc db :wallet/swap-advanced-mode? mode)})

(comment
  (->> re-frame.db/app-db
       deref
       :wallet/all-tokens
       vals
       (map #(str (:name %) "-" (:symbol %)))))
