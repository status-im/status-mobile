(ns status-im.data-store.realm.schemas.account.core
  (:require [status-im.data-store.realm.schemas.account.v2.core :as v2]))

;; TODO(oskarth): Add failing test if directory vXX exists but isn't in schemas.

;; put schemas ordered by version
(def schemas [{:schema        v2/schema
               :schemaVersion 2
               :migration     v2/migration}])

