(ns status-im.utils.fx
  (:require-macros status-im.utils.fx)
  (:require [clojure.set :as set]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [merge]))

(defn- update-db [cofx fx]
  (if-let [db (:db fx)]
    (assoc cofx :db db)
    cofx))

(def ^:private mergable-keys
  #{:data-store/tx :data-store/base-tx :chat-received-message/add-fx
    :shh/post :filters/load-filters
    :shh/send-direct-message :shh/remove-filter
    :shh/generate-sym-key-from-password  :transport/confirm-messages-processed
    :group-chats/extract-membership-signature :utils/dispatch-later :json-rpc/call})

(defn- safe-merge [fx new-fx]
  (if (:merging-fx-with-common-keys fx)
    fx
    (let [common-keys (set/intersection (into #{} (keys fx))
                                        (into #{} (keys new-fx)))]
      (if (empty? (set/difference common-keys (conj mergable-keys :db)))
        (clojure.core/merge (apply dissoc fx mergable-keys)
                            (apply dissoc new-fx mergable-keys)
                            (merge-with into
                                        (select-keys fx mergable-keys)
                                        (select-keys new-fx mergable-keys)))
        (do (log/error "Merging fx with common-keys: " common-keys)
            {:merging-fx-with-common-keys common-keys})))))

(defn merge
  "Takes a map of co-effects and forms as argument.
  The first optional form can be map of effects
  The next forms are functions applying effects and returning a map of effects.
  The fn ensures that updates to db are passed from function to function within the cofx :db key and
  that only a :merging-fx-with-common-keys effect is returned if some functions are trying
  to produce the same effects (excepted :db, :data-source/tx and :data-source/base-tx effects).
  :data-source/tx and :data-source/base-tx effects are handled specially and their results
  (list of transactions) are compacted to one transactions list (for each effect). "
  [{:keys [db] :as cofx} & args]
  (let [[first-arg & rest-args] args
        initial-fxs? (map? first-arg)
        fx-fns (if initial-fxs? rest-args args)]
    (reduce (fn [fxs fx-fn]
              (let [updated-cofx (update-db cofx fxs)]
                (if fx-fn
                  (safe-merge fxs (fx-fn updated-cofx))
                  fxs)))
            (if initial-fxs? first-arg {:db db})
            fx-fns)))
