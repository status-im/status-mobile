(ns status-im.utils.semaphores
  (:require [status-im.utils.fx :as fx]))

(fx/defn lock [{:keys [db]} semaphore]
  #_{:pre [(keyword? semaphore)]}
  {:db (update db :semaphores conj semaphore)})

(fx/defn free [{:keys [db]} semaphore]
  #_{:pre [(keyword? semaphore)]}
  {:db (update db :semaphores disj semaphore)})

(defn locked? [{:keys [db]} semaphore]
  #_{:pre [(keyword? semaphore)]}
  ((get db :semaphores) semaphore))
