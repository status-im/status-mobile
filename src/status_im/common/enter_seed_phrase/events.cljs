(ns status-im.common.enter-seed-phrase.events
  (:require
    [utils.re-frame :as rf]))

(rf/reg-event-fx :enter-seed-phrase/set-error
 (fn [{:keys [db]} [error]]
   {:db (assoc db :enter-seed-phrase/error error)}))

(rf/reg-event-fx :enter-seed-phrase/clear-error
 (fn [{:keys [db]}]
   {:db (dissoc db :enter-seed-phrase/error)}))
