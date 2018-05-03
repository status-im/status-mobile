(ns status-im.utils.handlers-macro
  (:require-macros status-im.utils.handlers-macro)
  (:require [clojure.set :as set]))

(defn update-db [cofx fx]
  (if-let [db (:db fx)]
    (assoc cofx :db db)
    cofx))

(def ^:private tx-keys #{:data-store/tx :data-store/base-tx})

(defn safe-merge [fx new-fx]
  (if (:merging-fx-with-common-keys fx)
    fx
    (let [common-keys (set/intersection (into #{} (keys fx))
                                        (into #{} (keys new-fx)))]
      (if (empty? (set/difference common-keys (conj tx-keys :db)))
        (merge (apply dissoc fx tx-keys)
               (apply dissoc new-fx tx-keys)
               (merge-with into
                           (select-keys fx tx-keys)
                           (select-keys new-fx tx-keys)))
        {:merging-fx-with-common-keys common-keys}))))
