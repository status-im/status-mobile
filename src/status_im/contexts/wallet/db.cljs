(ns status-im.contexts.wallet.db
  (:require [status-im.constants :as constants]))

(def network-filter-defaults
  {:selector-state    :default
   :selected-networks (set constants/default-network-names)})

(def defaults
<<<<<<< HEAD
<<<<<<< HEAD
  {:ui {:network-filter network-filter-defaults}})
=======
=======
>>>>>>> 2d5d98343 (lint)
<<<<<<< HEAD
  {:ui {:network-filter  network-filter-defaults
=======
  {:ui {:network-filter network-filter-defaults
>>>>>>> c7cd8e811 (lint)
=======
  {:ui {:network-filter  network-filter-defaults
>>>>>>> 54fa87586 (lint)
        :tokens-loading? true}})
>>>>>>> 3498d19b2 (lint)
