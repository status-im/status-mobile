(ns status-im.data-store.realm.schemas.account.core
  (:require [status-im.data-store.realm.schemas.account.chat :as chat]
            [status-im.data-store.realm.schemas.account.transport :as transport]
            [status-im.data-store.realm.schemas.account.contact :as contact]
            [status-im.data-store.realm.schemas.account.message :as message]
            [status-im.data-store.realm.schemas.account.user-status :as user-status]
            [status-im.data-store.realm.schemas.account.local-storage :as local-storage]
            [status-im.data-store.realm.schemas.account.mailserver :as mailserver]
            [status-im.data-store.realm.schemas.account.browser :as browser]
            [status-im.data-store.realm.schemas.account.dapp-permissions :as dapp-permissions]
            [status-im.data-store.realm.schemas.account.request :as request]
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

(def v14 [chat/v6
          transport/v6
          contact/v1
          message/v7
          mailserver/v11
          user-status/v1
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
               :migration     migrations/v14}])
