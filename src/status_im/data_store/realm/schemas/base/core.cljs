(ns status-im.data-store.realm.schemas.base.core
  (:require [status-im.data-store.realm.schemas.base.network :as network]
            [status-im.data-store.realm.schemas.base.account :as account]
            [status-im.data-store.realm.schemas.base.bootnode :as bootnode]
            [status-im.data-store.realm.schemas.base.extension :as extension]
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

(def v12 [network/v1
          bootnode/v4
          extension/v12
          account/v12])

(def v13 [network/v1
          bootnode/v4
          extension/v12
          account/v13])

(def v14 v13)

(def v15 [network/v1
          bootnode/v4
          extension/v12
          account/v14])

(def v16 [network/v1
          bootnode/v4
          extension/v12
          account/v15])

(def v17 v16)

;; should go through migration of Infura project IDs from old format to new
(def v18 v17)

(def v19 [network/v1
          bootnode/v4
          extension/v12
          account/v16])

(def v20 v19)

(def v21 [network/v1
          bootnode/v4
          extension/v12
          account/v17])

(def v22 [network/v1
          bootnode/v4
          extension/v12
          account/v18])

(def v23 [network/v1
          bootnode/v4
          extension/v12
          account/v19])

(def v24 v23)

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
               :migration     migrations/v11}
              {:schema        v12
               :schemaVersion 12
               :migration     migrations/v12}
              {:schema        v13
               :schemaVersion 13
               :migration     migrations/v13}
              {:schema        v14
               :schemaVersion 14
               :migration     migrations/v14}
              {:schema        v15
               :schemaVersion 15
               :migration     migrations/v15}
              {:schema        v16
               :schemaVersion 16
               :migration     migrations/v16}
              {:schema        v17
               :schemaVersion 17
               :migration     migrations/v17}
              {:schema        v18
               :schemaVersion 18
               :migration     migrations/v18}
              {:schema        v19
               :schemaVersion 19
               :migration     migrations/v19}
              {:schema        v20
               :schemaVersion 20
               :migration     migrations/v20}
              {:schema        v21
               :schemaVersion 21
               :migration     migrations/v21}
              {:schema        v22
               :schemaVersion 22
               :migration     (constantly nil)}
              {:schema        v23
               :schemaVersion 23
               :migration     (constantly nil)}
              {:schema        v24
               :schemaVersion 24
               :migration     migrations/v24}])
