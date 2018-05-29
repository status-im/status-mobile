(ns status-im.utils.http
  (:require [status-im.utils.utils :as utils]
            [status-im.react-native.js-dependencies :as rn-dependencies])
  (:refer-clojure :exclude [get]))

;; Default HTTP request timeout ms
(def http-request-default-timeout-ms 3000)

(defn post
  "Performs an HTTP POST request"
  ([url data on-success]
   (post url data on-success nil))
  ([url data on-success on-error]
   (post url data on-success on-error nil))
  ([url data on-success on-error {:keys [timeout-ms headers]}]
   (-> (rn-dependencies/fetch
        url
        (clj->js (merge {:method  "POST"
                         :body    data
                         :timeout (or timeout-ms http-request-default-timeout-ms)}
                        (when headers
                          {:headers headers}))))
       (.then (fn [response]
                (on-success response)))
       (.catch (or on-error
                   (fn [error]
                     (utils/show-popup "Error" (str error))))))))

(defn get
  "Performs an HTTP GET request"
  ([url] (get url nil))
  ([url on-success] (get url on-success nil))
  ([url on-success on-error]
   (get url on-success on-error nil))
  ([url on-success on-error {:keys [valid-response? timeout-ms]}]
   (-> (rn-dependencies/fetch url
                              (clj->js {:method  "GET"
                                        :headers {"Cache-Control" "no-cache"}
                                        :timeout (or timeout-ms http-request-default-timeout-ms)}))
       (.then (fn [response]
                (let [ok?  (.-ok response)
                      ok?' (if valid-response?
                             (and ok? (valid-response? response))
                             ok?)]
                  [(.-_bodyText response) ok?'])))
       (.then (fn [[response ok?]]
                (cond
                  (and on-success ok?)
                  (on-success response)

                  (and on-error (not ok?))
                  (on-error response)

                  :else false)))
       (.catch (or on-error
                   (fn [error]
                     (utils/show-popup "Error" (str error))))))))

(defn normalize-url [url]
  (str (when (and url (not (re-find #"^[a-zA-Z-_]+:/" url))) "http://") url))
