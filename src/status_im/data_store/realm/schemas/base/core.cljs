(ns status-im.data-store.realm.schemas.base.core
  (:require [status-im.data-store.realm.schemas.base.v1.core :as v1]
            [status-im.data-store.realm.schemas.base.v2.core :as v2]))

;; put schemas ordered by version
(def schemas [{:schema        v1/schema
               :schemaVersion 1
               :migration     v1/migration}
              {:schema        v2/schema
               :schemaVersion 2
               :migration     v2/migration}])
