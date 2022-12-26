(ns status-im.data-store.visibility-status-updates
  (:require [clojure.set :as clojure.set]
            [re-frame.core :as re-frame]
            [utils.re-frame :as rf]
            [taoensso.timbre :as log]))

(defn <-rpc
  [visibility-status-update]
  (clojure.set/rename-keys visibility-status-update
                           {:publicKey  :public-key
                            :statusType :status-type}))
(defn <-rpc-settings
  [settings]
  (-> settings
      (clojure.set/rename-keys
       {:current-user-status :current-user-visibility-status})
      (update :current-user-visibility-status <-rpc)))

(rf/defn fetch-visibility-status-updates-rpc
  [_]
  {:json-rpc/call [{:method     "wakuext_statusUpdates"
                    :params     []
                    :on-success #(re-frame/dispatch
                                  [:visibility-status-updates/visibility-status-updates-loaded
                                   (:statusUpdates ^js %)])
                    :on-error   #(log/error
                                  "failed to fetch visibility-status-updates"
                                  %)}]})
