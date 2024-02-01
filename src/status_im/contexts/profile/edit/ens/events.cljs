(ns status-im.contexts.profile.edit.ens.events
  (:require [utils.re-frame :as rf]))

(defn remove-ens-name
  [{:keys [db]} [name]]
  {:db (update db :ens/names dissoc name)})

(rf/reg-event-fx :ens/remove-ens remove-ens-name)
