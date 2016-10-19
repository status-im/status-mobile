(ns status-im.network.handlers
  (:require [re-frame.core :refer [dispatch debug enrich after]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.utils.handlers :as u]
            [status-im.network.net-info :as ni]
            [clojure.string :as s]))

(register-handler :listen-to-network-status!
  (u/side-effect!
    (fn []
      (let [handler #(dispatch [:update-network-status %])]
        (ni/init handler)
        (ni/add-listener handler)))))

(register-handler :update-network-status
  (fn [db [_ is-connected?]]
    (let [status (if is-connected? :online :offline)]
      (assoc db :network-status status))))
