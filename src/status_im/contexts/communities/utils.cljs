(ns status-im.contexts.communities.utils
  (:require
    [status-im.constants :as constants]))

(defn role->translation-key
  ([role] (role->translation-key role nil))
  ([role fallback-to]
   (condp = role
     constants/community-token-permission-become-token-owner  :t/token-owner
     constants/community-token-permission-become-token-master :t/token-master
     constants/community-token-permission-become-admin        :t/admin
     constants/community-token-permission-become-member       :t/member
     fallback-to)))

(defn sorted-operable-non-watch-only-accounts
  [db]
  (->> (get-in db [:wallet :accounts])
       (vals)
       (remove :watch-only?)
       (filter :operable?)
       (sort-by :position)))
