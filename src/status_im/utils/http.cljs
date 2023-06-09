(ns status-im.utils.http
  (:require ["react-native-fetch-polyfill" :default fetch]
            [clojure.string :as string]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [get]))

;; Default HTTP request timeout ms
(def http-request-default-timeout-ms 3000)

(defn- response-headers
  [^js response]
  (let [entries (es6-iterator-seq (.entries ^js (.-headers response)))]
    (reduce #(assoc %1 (string/trim (string/lower-case (first %2))) (string/trim (second %2)))
            {}
            entries)))

(defn raw-post
  "Performs an HTTP POST request and returns raw results :status :headers :body."
  ([url body on-success] (raw-post url body on-success nil))
  ([url body on-success on-error]
   (raw-post url body on-success on-error nil))
  ([url body on-success on-error {:keys [timeout-ms headers]}]
   (-> (fetch
        url
        (clj->js {:method  "POST"
                  :headers (merge {"Cache-Control" "no-cache"} headers)
                  :body    body
                  :timeout (or timeout-ms http-request-default-timeout-ms)}))
       (.then (fn [^js response]
                (->
                  (.text response)
                  (.then (fn [body]
                           (on-success {:status  (.-status response)
                                        :headers (response-headers response)
                                        :body    body}))))))
       (.catch (or on-error
                   (fn [error]
                     (utils/show-popup "Error" url (str error))))))))

;; FIXME: Should be more extensible and accept multiple methods
(defn post
  "Performs an HTTP POST request"
  ([url data on-success]
   (post url data on-success nil))
  ([url data on-success on-error]
   (post url data on-success on-error nil))
  ([url data on-success on-error
    {:keys [valid-response? method timeout-ms headers]
     :or   {method "POST"}}]
   (-> (fetch
        url
        (clj->js (merge {:method  method
                         :body    data
                         :timeout (or timeout-ms http-request-default-timeout-ms)}
                        (when headers
                          {:headers headers}))))
       (.then (fn [^js response]
                (-> (.text response)
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

(defn get
  "Performs an HTTP GET request"
  ([url] (get url nil))
  ([url on-success] (get url on-success nil))
  ([url on-success on-error]
   (get url on-success on-error nil))
  ([url on-success on-error params]
   (get url on-success on-error params nil))
  ([url on-success on-error {:keys [valid-response? timeout-ms]} headers]
   (-> (fetch
        url
        (clj->js {:method  "GET"
                  :headers (merge {"Cache-Control" "no-cache"} headers)
                  :timeout (or timeout-ms http-request-default-timeout-ms)}))
       (.then (fn [^js response]
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
       (.catch (or on-error #())))))

(defn normalize-url
  [url]
  (str (when (and (string? url)
                  (not (re-find #"^[a-zA-Z-_]+:/" url)))
         "https://")
       ((fnil string/trim "") url)))

(def normalize-and-decode-url (comp js/decodeURI normalize-url))

(defn url-host
  [url]
  (try
    (when-let [host (.getDomain ^js (goog.Uri. url))]
      (when-not (string/blank? host)
        (string/replace host #"www." "")))
    (catch :default _ nil)))

(defn url?
  [str]
  (try
    (when-let [host (.getDomain ^js (goog.Uri. str))]
      (not (string/blank? host)))
    (catch :default _ nil)))

(defn parse-payload
  [o]
  (when o
    (try
      (js->clj (js/JSON.parse o)
               :keywordize-keys
               true)
      (catch :default _
        (log/debug (str "Failed to parse " o))))))

(defn url-sanitized?
  [uri]
  (not (nil? (re-find #"^(https:)([/|.|\w|\s|-])*\.(?:jpg|svg|png)$" uri))))

(defn- split-param
  [param]
  (->
    (string/split param #"=")
    (concat (repeat ""))
    (->>
      (take 2))))

(defn- url-decode
  [string]
  (some-> string
          str
          (string/replace #"\+" "%20")
          (js/decodeURIComponent)))

(defn query->map
  [qstr]
  (when-not (string/blank? qstr)
    (some->> (string/split qstr #"&")
             seq
             (mapcat split-param)
             (map url-decode)
             (apply hash-map))))

(defn filter-letters-numbers-and-replace-dot-on-dash
  [^js value]
  (let [cc (.charCodeAt value 0)]
    (cond (or (and (> cc 96) (< cc 123))
              (and (> cc 64) (< cc 91))
              (and (> cc 47) (< cc 58)))
          value
          (= cc 46)
          "-")))

(defn topic-from-url
  [url]
  (string/lower-case (apply str (map filter-letters-numbers-and-replace-dot-on-dash (url-host url)))))
<<<<<<< HEAD

(defn replace-port
  [url new-port]
  (string/replace url #"(:\d+)" (str ":" new-port)))
=======
>>>>>>> 392397536 (updates)
