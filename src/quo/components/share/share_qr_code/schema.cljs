(ns quo.components.share.share-qr-code.schema
  (:require
    [quo.foundations.resources :as resources]))

(defn- valid-network?
  [network]
  (-> network resources/get-network boolean))

(def ?base
  [:map
   [:type [:enum :profile :wallet :saved-address :watched-address]]
   [:full-name :string]
   [:qr-image-uri :string]
   [:qr-data :string]
   [:theme :schema.common/theme]
   [:on-text-press {:optional true} [:maybe fn?]]
   [:on-text-long-press {:optional true} [:maybe fn?]]
   [:on-share-press {:optional true} [:maybe fn?]]
   [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
   [:unblur-on-android? {:optional true} [:maybe :boolean]]])

(def ?emoji :string)

(def ?profile
  [:map
   [:type [:= :profile]]
   [:profile-picture :schema.common/image-source]])

(def ?wallet
  [:map
   [:type [:= :wallet]]
   [:emoji ?emoji]])

(def ?address-legacy
  [:map
   [:address [:= :legacy]]
   [:on-legacy-press {:optional true} [:maybe fn?]]])

(def ?address-multichain
  [:map
   [:address [:= :multichain]]
   [:on-multichain-press {:optional true} [:maybe fn?]]])

(def ?address-base
  [:merge
   [:map
    [:networks [:sequential [:fn valid-network?]]]
    [:address [:enum :legacy :multichain]]]
   [:multi {:dispatch :address}
    [:legacy ?address-legacy]
    [:multichain ?address-multichain]]])

(def ?saved-address
  [:map
   [:type [:= :saved-address]]
   [:on-settings-press {:optional true} fn?]])

(def ?watched-address
  [:map
   [:type [:= :watched-address]]
   [:on-settings-press {:optional true} fn?]
   [:emoji ?emoji]])

(def ?schema
  [:=>
   [:catn
    [:props
     [:multi {:dispatch :type}
      [:profile [:merge ?base ?profile]]
      [:wallet [:merge ?base ?address-base ?wallet]]
      [:saved-address [:merge ?base ?address-base ?saved-address]]
      [:watched-address [:merge ?base ?address-base ?watched-address]]]]]
   :any])

