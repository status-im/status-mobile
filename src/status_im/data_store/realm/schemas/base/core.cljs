(ns status-im.data-store.realm.schemas.base.core
  (:require [status-im.data-store.realm.schemas.base.v1.core :as v1]))

;; put schemas ordered by version
(def schemas [{:schema        v1/schema
               :schemaVersion 1
               :migration     v1/migration}
              ;; dirty hotfix just for `0.9.17` -> `0.9.18` upgrade
              {:schema        v1/schema
               :schemaVersion 2
               :migration     v1/migration}])
