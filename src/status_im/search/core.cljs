(ns status-im.search.core
  (:require [status-im.utils.fx :as fx]))

(fx/defn filter-changed [cofx search-filter]
  {:db (assoc-in (:db cofx) [:ui/search :filter] search-filter)})
