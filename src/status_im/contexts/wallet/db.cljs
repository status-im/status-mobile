(ns status-im.contexts.wallet.db
  (:require [status-im.constants :as constants]))

(def network-filter-defaults
  {:selector-state    :default
   :selected-networks (set constants/default-network-names)})

(def defaults
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
  {:ui {:network-filter network-filter-defaults}})
=======
=======
>>>>>>> 2d5d98343 (lint)
<<<<<<< HEAD
=======
>>>>>>> 42efc642a (lint)
  {:ui {:network-filter  network-filter-defaults
        :tokens-loading? true}})
>>>>>>> 3498d19b2 (lint)
