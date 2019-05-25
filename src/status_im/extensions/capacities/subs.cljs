(ns status-im.extensions.capacities.subs)

(def all
  {'identity            {:data :extensions/identity :arguments {:value :map}}
   'store/get           {:data :store/get :arguments {:key :string :reverse? :boolean}}
   'store/get-in        {:data :store/get-in :arguments {:key :vector}}
   'store/get-vals      {:data :store/get-vals :arguments {:key :string}}
   'time/now            {:data :extensions.time/now}
   'contacts/all        {:data :extensions.contacts/all} ;; :photo :name :address :public-key
   'wallet/collectibles {:data :get-collectible-token :arguments {:token :string :symbol :string}}
   'wallet/balance      {:data :extensions.wallet/balance :arguments {:token :string}}
   'wallet/token        {:data :extensions.wallet/token :arguments {:token :string :amount? :number :amount-in-wei? :number}}
   'wallet/tokens       {:data :extensions.wallet/tokens :arguments {:filter? :vector :visible? :boolean}}})