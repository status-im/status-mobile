(ns status-im.common.peer-stats.events
  (:require
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/reg-event-fx :peer-stats/get-count
 (fn []
   {:fx [[:json-rpc/call
          [{:method     :wakuext_peers
            :on-success [:peer-stats/get-count-success]
            :on-error   (fn [error]
                          (log/error "failed to fetch wakuv2 peer count"
                                     {:error error}))}]]]}))

(rf/reg-event-fx :peer-stats/get-count-success
 (fn [{:keys [db]} [response]]
   {:db (assoc db :peer-stats/count (count response))}))
