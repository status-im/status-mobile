(ns status-im.ipfs.core
  (:refer-clojure :exclude [cat])
  (:require [status-im.utils.fx :as fx]))

;; we currently use an ipfs gateway but this detail is not relevant
;; outside of this namespace
(def ^:const ipfs-add-url "https://ipfs.infura.io:5001/api/v0/add")
(def ^:const ipfs-cat-url "https://ipfs.infura.io/ipfs/")

(fx/defn cat
  [cofx {:keys [hash on-success on-failure]}]
  {:http-raw-get (cond-> {:url (str ipfs-cat-url hash)
                          :timeout-ms 5000
                          :success-event-creator
                          (fn [{:keys [status body]}]
                            (if (= 200 status)
                              (on-success {:value body})
                              (when on-failure
                                (on-failure {:value status}))))}
                   on-failure
                   (assoc :failure-event-creator on-failure))})

(defn- parse-ipfs-add-response
  [response]
  (when response
    (let [{:keys [Name Hash Size]} (js->clj (js/JSON.parse response)
                                            :keywordize-keys true)]
      {:name Name
       :hash Hash
       :size Size})))

(fx/defn add
  [cofx {:keys [value on-success on-failure]}]
  (let [formdata (doto (js/FormData.)
                   ;; the key is ignored so there is no need to provide one
                   (.append nil value))]
    {:http-raw-post (cond-> {:url  ipfs-add-url
                             :body formdata
                             :timeout-ms 5000
                             :success-event-creator
                             (fn [{:keys [status body]}]
                               (if (= 200 status)
                                 (on-success {:value (parse-ipfs-add-response body)})
                                 (when on-failure
                                   (on-failure {:value status}))))}
                      on-failure
                      (assoc :failure-event-creator on-failure))}))
