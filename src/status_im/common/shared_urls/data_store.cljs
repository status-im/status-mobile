(ns status-im.common.shared-urls.data-store
  (:require
    [clojure.set :as set]))

(defn <-rpc
  [{:keys [community channel contact]}]
  {:community (set/rename-keys
               community
               {:communityId  :community-id
                :displayName  :display-name
                :membersCount :members-count
                :tagIndices   :tag-indices})
   :channel   (set/rename-keys
               channel
               {:displayName :display-name
                :channelUuid :channel-uuid})
   :contact   (set/rename-keys
               contact
               {:displayName :display-name
                :publicKey   :public-key})})
