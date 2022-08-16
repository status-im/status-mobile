(ns status-im.search.core
  (:require [status-im.utils.fx :as fx]))

(fx/defn home-filter-changed
  {:events [:search/home-filter-changed]}
  [cofx search-filter]
  {:db (assoc-in (:db cofx) [:ui/search :home-filter] search-filter)})

(fx/defn currency-filter-changed
  {:events [:search/currency-filter-changed]}
  [cofx search-filter]
  {:db (assoc-in (:db cofx) [:ui/search :currency-filter] search-filter)})

(fx/defn token-filter-changed
  {:events [:search/token-filter-changed]}
  [cofx search-filter]
  {:db (assoc-in (:db cofx) [:ui/search :token-filter] search-filter)})

(fx/defn recipient-filter-changed
  {:events [:search/recipient-filter-changed]}
  [cofx search-filter]
  {:db (assoc-in (:db cofx) [:ui/search :recipient-filter] search-filter)})