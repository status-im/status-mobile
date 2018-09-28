(ns status-im.data-store.realm.schemas.base.core
  (:require [status-im.data-store.realm.schemas.base.network :as network]
            [status-im.data-store.realm.schemas.base.account :as account]
            [status-im.data-store.realm.schemas.base.bootnode :as bootnode]
            [status-im.data-store.realm.schemas.base.migrations :as migrations]))

(def v1 [network/v1
         account/v1])

(def v2 [network/v1
         account/v2])

(def v3 [network/v1
         account/v3])

(def v4 [network/v1
         bootnode/v4
         account/v4])

(def v5 v4)

(def v6 [network/v1
         bootnode/v4
         account/v6])

(def v7 [network/v1
         bootnode/v4
         account/v7])

(def v8 [network/v1
         bootnode/v4
         account/v8])

(def v9 v8)

(def v10 [network/v1
          bootnode/v4
          account/v10])

(def v11 [network/v1
          bootnode/v4
          account/v11])

;; put schemas ordered by version
(def schemas [{:schema        v1
               :schemaVersion 1
               :migration     migrations/v1}
              {:schema        v2
               :schemaVersion 2
               :migration     migrations/v2}
              {:schema        v3
               :schemaVersion 3
               :migration     migrations/v3}
              {:schema        v4
               :schemaVersion 4
               :migration     migrations/v4}
              {:schema        v5
               :schemaVersion 5
               :migration     migrations/v5}
              {:schema        v6
               :schemaVersion 6
               :migration     migrations/v6}
              {:schema        v7
               :schemaVersion 7
               :migration     migrations/v7}
              {:schema        v8
               :schemaVersion 8
               :migration     migrations/v8}
              {:schema        v9
               :schemaVersion 9
               :migration     migrations/v9}
              {:schema        v10
               :schemaVersion 10
               :migration     migrations/v10}
              {:schema        v11
               :schemaVersion 11
               :migration     migrations/v11}])
