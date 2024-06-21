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
   [:on-text-press {:optional true} [:maybe fn?]]
   [:on-text-long-press {:optional true} [:maybe fn?]]
   [:on-share-press {:optional true} [:maybe fn?]]
   [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
   [:unblur-on-android? {:optional true} [:maybe :boolean]]])

(def ?emoji :string)

(def ?profile
  [:map
   [:type [:= :profile]]
   [:profile-picture [:maybe :schema.quo/profile-picture-source]]])

(def ?channel
  [:map
   [:type [:= :channel]]
   [:emoji {:optional true} [:maybe ?emoji]]])

(def ?wallet
  [:map
   [:type [:= :wallet]]
   [:emoji {:optional true} [:maybe ?emoji]]])

(def ?address-multichain
  [:map
   [:address [:= :multichain]]
   [:on-settings-press {:optional true} fn?]])

(def ?address-base
  [:merge
   [:map
    [:networks [:sequential [:fn valid-network?]]]
    [:on-legacy-press {:optional true} [:maybe fn?]]
    [:on-multichain-press {:optional true} [:maybe fn?]]
    [:address [:enum :legacy :multichain]]]
   [:multi {:dispatch :address}
    [:multichain ?address-multichain]]])

(def ?saved-address
  [:map
   [:type [:= :saved-address]]
   [:on-settings-press {:optional true} fn?]])

(def ?watched-address
  [:map
   [:type [:= :watched-address]]
   [:emoji {:optional true} [:maybe ?emoji]]])

(def ?schema
  [:=>
   [:catn
    [:props
     [:multi {:dispatch :type}
      [:channel [:merge ?base ?channel]]
      [:profile [:merge ?base ?profile]]
      [:wallet [:merge ?base ?address-base ?wallet]]
      [:saved-address [:merge ?base ?address-base ?saved-address]]
      [:watched-address [:merge ?base ?address-base ?watched-address]]]]]
   :any])

