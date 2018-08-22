(ns status-im.utils.semaphores)

(defn lock [semaphore {:keys [db]}]
  {:pre [(keyword? semaphore)]}
  {:db (update db :semaphores conj semaphore)})

(defn free [semaphore {:keys [db]}]
  {:pre [(keyword? semaphore)]}
  (update db :semaphores disj semaphore))

(defn locked? [semaphore cofx]
  {:pre [(keyword? semaphore)]}
  ((get-in cofx [:db :semaphores]) semaphore))
