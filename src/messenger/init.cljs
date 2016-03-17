(ns messenger.init
  (:require [messenger.persistence.simple-kv-store :as kv]
            [messenger.state :as state]))

(defn init-simple-store []
  (swap! state/app-state assoc-in state/simple-store-path (kv/->SimpleKvStore)))
