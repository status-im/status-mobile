(ns status-im.data-store.realm.schemas.account.membership-update)

(def v1 {:name       :membership-update
         :primaryKey :id
         :properties {:id          :string
                      :type        :string
                      :name        {:type :string
                                    :optional true}
                      :clock-value :int
                      :signature   :string
                      :from        :string
                      :member      {:type :string
                                    :optional true}
                      :members     {:type "string[]"
                                    :optional true}}})
