(ns status-im.ipfs.core
  (:refer-clojure :exclude [cat])
  (:require [status-im.utils.fx :as fx]))

;; we currently use an ipfs gateway but this detail is not relevant
;; outside of this namespace
(def ipfs-add-url "https://ipfs.infura.io:5001/api/v0/add?cid-version=1")
(def ipfs-cat-url "https://ipfs.infura.io/ipfs/")

(fx/defn cat
  [cofx {:keys [hash on-success on-failure]}]
  {:http-raw-get (cond-> {:url (str ipfs-cat-url hash)
                          :timeout-ms 5000
                          :success-event-creator
                          (fn [{:keys [status body]}]
                            (if (= 200 status)
                              (on-success body)
                              (when on-failure
                                (on-failure status))))}
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
  "Add `value` on ipfs, and returns its b58 encoded CID"
  [cofx {:keys [value on-success on-failure opts timeout-ms]}]
  (let [formdata (doto (js/FormData.)
                   ;; the key is ignored so there is no need to provide one
                   (.append "file" value))]
    {:http-raw-post {:url  ipfs-add-url
                     :body formdata
                     :opts opts
                     :timeout-ms (or 25000 timeout-ms)
                     :on-failure on-failure
                     :on-success
                     (fn [{:keys [status body]}]
                       (if (= 200 status)
                         (on-success (parse-ipfs-add-response body))
                         (when on-failure
                           (on-failure status))))}}))