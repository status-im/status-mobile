(ns status-im.contexts.wallet.routes.utils)

(defn valid-error?
  [{:keys [code details]}]
  (println :error code details)
  (cond
    (and (= code "0")
         (contains? details "context changed")) false

    :else                                       true))
