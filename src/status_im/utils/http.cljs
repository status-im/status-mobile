(ns status-im.utils.http
  (:require [status-im.utils.utils :as utils]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [taoensso.timbre :as log]
            [clojure.string :as string])
  (:refer-clojure :exclude [get]))

;; Default HTTP request timeout ms
(def http-request-default-timeout-ms 3000)

(defn- headers [response]
  (let [entries (es6-iterator-seq (.entries (.-headers response)))]
    (reduce #(assoc %1 (string/trim (string/lower-case (first %2))) (string/trim (second %2))) {} entries)))

(defn raw-post
  "Performs an HTTP POST request and returns raw results :status :headers :body."
  ([url body on-success] (raw-post url body on-success nil))
  ([url body on-success on-error]
   (raw-post url body on-success on-error nil))
  ([url body on-success on-error {:keys [timeout-ms]}]
   (-> ((rn-dependencies/fetch)
        url
        (clj->js {:method  "POST"
                  :headers {"Cache-Control" "no-cache"}
                  :body    body
                  :timeout (or timeout-ms http-request-default-timeout-ms)}))
       (.then (fn [response]
                (->
                 (.text response)
                 (.then (fn [body]
                          (on-success {:status  (.-status response)
                                       :headers (headers response)
                                       :body    body}))))))
       (.catch (or on-error
                   (fn [error]
                     (utils/show-popup "Error" url (str error))))))))

(defn post
  "Performs an HTTP POST request"
  ([url data on-success]
   (post url data on-success nil))
  ([url data on-success on-error]
   (post url data on-success on-error nil))
  ([url data on-success on-error {:keys [valid-response? timeout-ms headers]}]
   (-> ((rn-dependencies/fetch)
        url
        (clj->js (merge {:method  "POST"
                         :body    data
                         :timeout (or timeout-ms http-request-default-timeout-ms)}
                        (when headers
                          {:headers headers}))))
       (.then (fn [response]
                (->
                 (.text response)
                 (.then (fn [response-body]
                          (let [ok?  (.-ok response)
                                ok?' (if valid-response?
                                       (and ok? (valid-response? response))
                                       ok?)]
                            {:response-body response-body
                             :ok?           ok?'
                             :status-text   (.-statusText response)
                             :status-code   (.-status response)}))))))
       (.then (fn [{:keys [ok?] :as data}]
                (cond
                  (and on-success ok?)
                  (on-success data)

                  (and on-error (not ok?))
                  (on-error data)

                  :else false)))
       (.catch (fn [error]
                 (if on-error
                   (on-error {:response-body error})
                   (utils/show-popup "Error" url (str error))))))))

(defn raw-get
  "Performs an HTTP GET request and returns raw results :status :headers :body."
  ([url] (raw-get url nil))
  ([url on-success] (raw-get url on-success nil))
  ([url on-success on-error]
   (raw-get url on-success on-error nil))
  ([url on-success on-error {:keys [timeout-ms]}]
   (-> ((rn-dependencies/fetch)
        url
        (clj->js {:method  "GET"
                  :headers {"Cache-Control" "no-cache"}
                  :timeout (or timeout-ms http-request-default-timeout-ms)}))
       (.then (fn [response]
                (->
                 (.text response)
                 (.then (fn [body]
                          (on-success {:status  (.-status response)
                                       :headers (headers response)
                                       :body    body}))))))
       (.catch (or on-error
                   (fn [error]
                     (utils/show-popup "Error" url (str error))))))))

(defn get
  "Performs an HTTP GET request"
  ([url] (get url nil))
  ([url on-success] (get url on-success nil))
  ([url on-success on-error]
   (get url on-success on-error nil))
  ([url on-success on-error params]
   (get url on-success on-error params nil))
  ([url on-success on-error {:keys [valid-response? timeout-ms]} headers]
   (-> ((rn-dependencies/fetch)
        url
        (clj->js {:method  "GET"
                  :headers (merge {"Cache-Control" "no-cache"} headers)
                  :timeout (or timeout-ms http-request-default-timeout-ms)}))
       (.then (fn [response]
                (->
                 (.text response)
                 (.then (fn [response-body]
                          (let [ok?  (.-ok response)
                                ok?' (if valid-response?
                                       (and ok? (valid-response? response))
                                       ok?)]
                            [response-body ok?']))))))
       (.then (fn [[response ok?]]
                (cond
                  (and on-success ok?)
                  (on-success response)

                  (and on-error (not ok?))
                  (on-error response)

                  :else false)))
       (.catch (or on-error
                   (fn [error]
                     (utils/show-popup "Error" url (str error))))))))

(defn normalize-url [url]
  (str (when (and (string? url) (not (re-find #"^[a-zA-Z-_]+:/" url))) "http://") url))

(def normalize-and-decode-url (comp js/decodeURI normalize-url))

(defn url-host [url]
  (try
    (when-let [host (.getDomain (goog.Uri. url))]
      (when-not (string/blank? host)
        (string/replace host #"www." "")))
    (catch :default _ nil)))

(defn parse-payload [o]
  (when o
    (try
      (js->clj (js/JSON.parse o)
               :keywordize-keys true)
      (catch :default _
        (log/debug (str "Failed to parse " o))))))

(defn url-sanitized? [uri]
  (not (nil? (re-find #"^(https:)([/|.|\w|\s|-])*\.(?:jpg|svg|png)$" uri))))
