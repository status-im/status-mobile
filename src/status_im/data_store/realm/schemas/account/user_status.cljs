(ns status-im.data-store.realm.schemas.account.user-status)

(def v1 {:name       :user-status
         :primaryKey :status-id
         :properties {;; Unfortunately, realm doesn't support composite primary keys,
                      ;; so we have to keep separate `:status-id` property, which is just
                      ;; `:message-id`-`:whisper-identity` concatenated
                      :status-id        :string
                      :message-id       :string
                      :chat-id          :string
                      :whisper-identity :string
                      :status           :string}})

(def v2 {:name       :user-status
         :primaryKey :status-id
         :properties {;; Unfortunately, realm doesn't support composite primary keys,
                      ;; so we have to keep separate `:status-id` property, which is just
                      ;; `:message-id`-`:public-key` concatenated
                      :status-id        :string
                      :message-id       :string
                      :chat-id          :string
                      :public-key       :string
                      :status           :string}})
