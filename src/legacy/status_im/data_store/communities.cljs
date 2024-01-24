(ns legacy.status-im.data-store.communities
  (:require [clojure.set :as set]))

(defn rpc->channel-permissions
  [rpc-channels-permissions]
  (update-vals rpc-channels-permissions
               (fn [{:keys [viewAndPostPermissions viewOnlyPermissions]}]
                 {:view-only     (set/rename-keys viewOnlyPermissions {:satisfied :satisfied?})
                  :view-and-post (set/rename-keys viewAndPostPermissions {:satisfied :satisfied?})})))
