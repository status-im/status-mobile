(ns status-im.ipfs.core
  (:refer-clojure :exclude [cat])
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]))

;; we currently use an ipfs gateway but this detail is not relevant
;; outside of this namespace
(def ipfs-add-url "https://ipfs.infura.io:5001/api/v0/add?cid-version=1")
(def ipfs-cat-url "https://ipfs.infura.io/ipfs/")

(fx/defn cat
  [cofx {:keys [hash on-success on-failure]}]
  {:http-get (cond-> {:url (str ipfs-cat-url hash)
                      :timeout-ms 5000
                      :on-success (fn [{:keys [status body]}]
                                    (if (= 200 status)
                                      (re-frame/dispatch (on-success body))
                                      (when on-failure
                                        (re-frame/dispatch (on-failure status)))))}
               on-failure
               (assoc :on-error
                      #(re-frame/dispatch (on-failure %))))})
