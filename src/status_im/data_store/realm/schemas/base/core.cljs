(ns status-im.data-store.realm.schemas.base.core
  (:require [status-im.data-store.realm.schemas.base.v1.core :as v1]
            [status-im.data-store.realm.schemas.base.v2.core :as v2]
            [status-im.data-store.realm.schemas.base.v3.core :as v3]
            [status-im.data-store.realm.schemas.base.v4.core :as v4]
            [status-im.data-store.realm.schemas.base.v5.core :as v5]))

; put schemas ordered by version
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
               :migration     v5/migration}])