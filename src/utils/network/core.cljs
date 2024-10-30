(ns utils.network.core
  (:require
    [clojure.string :as string]
    [taoensso.timbre :as log]))

(def url-regex
  #"https?://(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}(\.[a-z]{2,6})?\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)")

(defn valid-rpc-url?
  [url]
  (boolean (re-matches url-regex (str url))))

(defn validate-string
  [{:keys [value]}]
  {:value value
   :error (string/blank? value)})

(defn validate-network-id
  [{:keys [value]}]
  {:value value
   :error (and (not (string/blank? value))
               (= (int value) 0))})

(defn validate-url
  [{:keys [value]}]
  {:value value
   :error (not (valid-rpc-url? value))})

(defn validate-manage
  [manage]
  (-> manage
      (update :url validate-url)
      (update :name validate-string)
      (update :symbol validate-string)
      (update :chain validate-string)
      (update :network-id validate-network-id)))

(defn valid-manage?
  [manage]
  (->> (validate-manage manage)
       vals
       (map :error)
       (not-any? identity)))

(defn fetch
  [url {:keys [body] :as params} callback]
  (let [js-params (cond-> params
                    (map? body) (update :body (comp js/JSON.stringify clj->js))
                    :always     clj->js)]
    (-> (js/fetch url js-params)
        (.then (fn [response]
                 (.text response)))
        (.then callback)
        (.catch #(log/error (str "Error while fetching: " url) %)))))
