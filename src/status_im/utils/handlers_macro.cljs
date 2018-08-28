(ns status-im.utils.handlers-macro
  (:require-macros status-im.utils.handlers-macro
                   [taoensso.timbre :as log])
  (:require [clojure.set :as set]))

(defn update-db [cofx fx]
  (if-let [db (:db fx)]
    (assoc cofx :db db)
    cofx))

(def ^:private mergable-keys
  #{:data-store/tx :data-store/base-tx :chat-received-message/add-fx
    :shh/add-new-sym-keys :shh/get-new-sym-keys :shh/post
    :confirm-messages-processed :utils/dispatch-later})

(defn safe-merge [fx new-fx]
  (if (:merging-fx-with-common-keys fx)
    fx
    (let [common-keys (set/intersection (into #{} (keys fx))
                                        (into #{} (keys new-fx)))]
      (if (empty? (set/difference common-keys (conj mergable-keys :db)))
        (merge (apply dissoc fx mergable-keys)
               (apply dissoc new-fx mergable-keys)
               (merge-with into
                           (select-keys fx mergable-keys)
                           (select-keys new-fx mergable-keys)))
        (do (log/error "Merging fx with common-keys: " common-keys)
            {:merging-fx-with-common-keys common-keys})))))

(defn merge-effects
  ([{:keys [db] :as cofx} handler args]
   (merge-effects {:db db} cofx handler args))
  ([initial-fx {:keys [db] :as cofx} handler args]
   (reduce (fn [fx arg]
             (let [temp-cofx (update-db cofx fx)]
               (safe-merge
                fx
                (handler arg temp-cofx))))
           (or initial-fx
               {:db db})
           args)))
