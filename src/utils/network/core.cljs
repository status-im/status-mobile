(ns utils.network.core
  (:require
    [clojure.string :as string]))

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
