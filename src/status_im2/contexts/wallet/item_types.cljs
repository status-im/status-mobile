(ns status-im2.contexts.wallet.item-types)

(def ^:const no-type 0)
(def ^:const account 1)
(def ^:const saved-address 2)
(def ^:const address 3)
(def ^:const saved-contact-address 4)

(def ^:const all-supported
  #{account
    saved-address
    address
    saved-contact-address})
