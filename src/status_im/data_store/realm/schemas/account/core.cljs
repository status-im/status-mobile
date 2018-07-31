(ns status-im.data-store.realm.schemas.account.core
  (:require
   [status-im.data-store.realm.schemas.account.v1.core :as v1]
   [status-im.data-store.realm.schemas.account.v2.core :as v2]
   [status-im.data-store.realm.schemas.account.v3.core :as v3]
   [status-im.data-store.realm.schemas.account.v4.core :as v4]
   [status-im.data-store.realm.schemas.account.v5.core :as v5]
   [status-im.data-store.realm.schemas.account.v6.core :as v6]
   [status-im.data-store.realm.schemas.account.v7.core :as v7]
   [status-im.data-store.realm.schemas.account.v8.core :as v8]
   [status-im.data-store.realm.schemas.account.v9.core :as v9]))

;; TODO(oskarth): Add failing test if directory vXX exists but isn't in schemas.

;; put schemas ordered by version
(def schemas [{:schema        v1/schema
               :schemaVersion 1
               :migration     v1/migration}
              {:schema        v2/schema
               :schemaVersion 2
               :migration     v2/migration}
              {:schema        v3/schema
               :schemaVersion 3
               :migration     v3/migration}
              {:schema        v4/schema
               :schemaVersion 4
               :migration     v4/migration}
              {:schema        v5/schema
               :schemaVersion 5
               :migration     v5/migration}
              {:schema        v6/schema
               :schemaVersion 6
               :migration     v6/migration}
              {:schema        v7/schema
               :schemaVersion 7
               :migration     v7/migration}
              {:schema        v8/schema
               :schemaVersion 8
               :migration     v8/migration}
              {:schema        v9/schema
               :schemaVersion 9
               :migration     v9/migration}])
