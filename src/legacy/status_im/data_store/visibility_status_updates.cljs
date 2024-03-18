(ns legacy.status-im.data-store.visibility-status-updates
  (:require
    [clojure.set :as set]
    [re-frame.core :as re-frame]
    [status-im.common.json-rpc.events :as json-rpc]
    [taoensso.timbre :as log]))

(defn <-rpc
  [visibility-status-update]
  (set/rename-keys visibility-status-update
                   {:publicKey  :public-key
                    :statusType :status-type}))

(defn <-rpc-settings
  [settings]
  (-> settings
      (set/rename-keys
       {:current-user-status :current-user-visibility-status})
      (update :current-user-visibility-status <-rpc)))

(re-frame/reg-fx :visibility-status-updates/fetch
 (fn []
   (json-rpc/call {:method     "wakuext_statusUpdates"
                   :params     []
                   :on-success #(re-frame/dispatch
                                 [:visibility-status-updates/visibility-status-updates-loaded
                                  (:statusUpdates ^js %)])
                   :on-error   #(log/error
                                 "failed to fetch visibility-status-updates"
                                 %)})))
