(ns status-im.utils.handlers-macro
  (:require-macros status-im.utils.handlers-macro))

(defn update-db [cofx fx]
  (if-let [db (:db fx)]
    (assoc cofx :db db)
    cofx))

(defn safe-merge [fx new-fx]
  (if (:merging-fx-with-common-keys fx)
    fx
    (let [common-keys (clojure.set/intersection (into #{} (keys fx))
                                                (into #{} (keys new-fx)))]
      (if (empty? (disj common-keys :db))
        (merge fx new-fx)
        {:merging-fx-with-common-keys common-keys}))))
