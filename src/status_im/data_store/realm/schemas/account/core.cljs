(ns status-im.data-store.realm.schemas.account.core
  (:require [status-im.data-store.realm.schemas.account.chat :as chat]
            [status-im.data-store.realm.schemas.account.transport :as transport]
            [status-im.data-store.realm.schemas.account.transport-inbox-topic :as transport-inbox-topic]
            [status-im.data-store.realm.schemas.account.mailserver-topic :as mailserver-topic]
            [status-im.data-store.realm.schemas.account.contact :as contact]
            [status-im.data-store.realm.schemas.account.message :as message]
            [status-im.data-store.realm.schemas.account.user-status :as user-status]
            [status-im.data-store.realm.schemas.account.local-storage :as local-storage]
            [status-im.data-store.realm.schemas.account.mailserver :as mailserver]
            [status-im.data-store.realm.schemas.account.browser :as browser]
            [status-im.data-store.realm.schemas.account.dapp-permissions :as dapp-permissions]
            [status-im.data-store.realm.schemas.account.request :as request]
            [status-im.data-store.realm.schemas.account.membership-update :as membership-update]
            [status-im.data-store.realm.schemas.account.installation :as installation]
            [status-im.data-store.realm.schemas.account.migrations :as migrations]
            [taoensso.timbre :as log]))

(def v1 [chat/v1
         transport/v1
         contact/v1
         message/v1
         request/v1
         user-status/v1
         local-storage/v1
         browser/v1])

(def v2 [chat/v1
         transport/v1
         contact/v1
         message/v1
         request/v1
         mailserver/v2
         user-status/v1
         local-storage/v1
         browser/v1])

(def v3 [chat/v3
         transport/v1
         contact/v1
         message/v1
         request/v1
         mailserver/v2
         user-status/v1
         local-storage/v1
         browser/v1])

(def v4 [chat/v3
         transport/v4
         contact/v1
         message/v1
         request/v1
         mailserver/v2
         user-status/v1
         local-storage/v1
         browser/v1])

(def v5 [chat/v5
         transport/v4
         contact/v1
         message/v1
         request/v1
         mailserver/v2
         user-status/v1
         local-storage/v1
         browser/v1])

(def v6 [chat/v5
         transport/v6
         contact/v1
         message/v1
         request/v1
         mailserver/v2
         user-status/v1
         local-storage/v1
         browser/v1])

(def v7 [chat/v5
         transport/v6
         contact/v1
         message/v7
         request/v1
         mailserver/v2
         user-status/v1
         local-storage/v1
         browser/v1])

(def v8 [chat/v5
         transport/v6
         contact/v1
         message/v7
         request/v1
         mailserver/v2
         user-status/v1
         local-storage/v1
         browser/v8])

(def v9 [chat/v5
         transport/v6
         contact/v1
         message/v7
         request/v1
         mailserver/v2
         user-status/v1
         local-storage/v1
         browser/v8
         dapp-permissions/v9])

(def v10 [chat/v5
          transport/v6
          contact/v1
          message/v7
          mailserver/v2
          user-status/v1
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v11 [chat/v5
          transport/v6
          contact/v1
          message/v7
          mailserver/v11
          user-status/v1
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v12 [chat/v5
          transport/v6
          contact/v1
          message/v7
          mailserver/v11
          user-status/v1
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v13 [chat/v6
          transport/v6
          contact/v1
          message/v7
          mailserver/v11
          user-status/v1
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v14 v13)

(def v15 [chat/v7
          transport/v6
          contact/v1
          message/v7
          mailserver/v11
          user-status/v1
          membership-update/v1
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v16 [chat/v7
          transport/v7
          transport-inbox-topic/v1
          contact/v1
          message/v7
          mailserver/v11
          user-status/v1
          membership-update/v1
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v17 [chat/v8
          transport/v7
          transport-inbox-topic/v1
          contact/v2
          message/v7
          mailserver/v11
          user-status/v1
          membership-update/v1
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v18 v17)

(def v19 [chat/v8
          transport/v7
          transport-inbox-topic/v1
          contact/v2
          message/v7
          mailserver/v11
          user-status/v1
          membership-update/v1
          installation/v1
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v20 v19)

(def v21 [chat/v8
          transport/v7
          transport-inbox-topic/v1
          contact/v2
          message/v7
          mailserver/v11
          user-status/v1
          membership-update/v1
          installation/v2
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v22 [chat/v8
          transport/v7
          mailserver-topic/v1
          contact/v2
          message/v7
          mailserver/v11
          user-status/v1
          membership-update/v1
          installation/v2
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v23 [chat/v8
          transport/v7
          contact/v3
          message/v7
          mailserver/v11
          mailserver-topic/v1
          user-status/v2
          membership-update/v1
          installation/v1
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v24 [chat/v8
          transport/v7
          contact/v3
          message/v7
          mailserver/v11
          mailserver-topic/v1
          user-status/v2
          membership-update/v1
          installation/v2
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v25 [chat/v8
          transport/v7
          contact/v3
          message/v7
          mailserver/v11
          mailserver-topic/v1
          user-status/v2
          membership-update/v1
          installation/v2
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v26 [chat/v9
          transport/v7
          contact/v3
          message/v7
          mailserver/v11
          mailserver-topic/v1
          user-status/v2
          membership-update/v1
          installation/v2
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v27 [chat/v9
          transport/v7
          contact/v3
          message/v9
          mailserver/v11
          mailserver-topic/v1
          user-status/v2
          membership-update/v1
          installation/v2
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

(def v28 [chat/v10
          transport/v7
          contact/v3
          message/v9
          mailserver/v11
          mailserver-topic/v1
          user-status/v2
          membership-update/v1
          installation/v2
          local-storage/v1
          browser/v8
          dapp-permissions/v9])

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
               :migration     migrations/v22}
              {:schema        v23
               :schemaVersion 23
               :migration     migrations/v23}
              {:schema        v24
               :schemaVersion 24
               :migration     migrations/v24}
              {:schema        v25
               :schemaVersion 25
               :migration     migrations/v25}
              {:schema        v26
               :schemaVersion 26
               :migration     migrations/v26}
              {:schema        v27
               :schemaVersion 27
               :migration     migrations/v27}
              {:schema        v28
               :schemaVersion 28
               :migration     migrations/v28}])
