(ns status-im.models.dev-server
  (:require [clojure.string :as string]
            [status-im.browser.core :as browser]
            [status-im.network.core :as network]
            [status-im.utils.fx :as fx]))

(defn start
  []
  {:dev-server/start nil})

;; Specific server operations

(defmulti process-request! (fn [{:keys [url type]}] [type (first url) (second url)]))

(defmethod process-request! [:POST "ping" nil]
  [_]
  {:dev-server/respond [200 {:message "Pong!"}]})

(defmethod process-request! [:POST "dapp" "open"]
  [{{:keys [url]} :data cofx :cofx}]
  (fx/merge cofx
            {:dev-server/respond [200 {:message "URL has been opened."}]}
            (browser/open-url url)))

(defmethod process-request! [:POST "network" nil]
  [{:keys [cofx data]}]
  (let [data (->> data
                  (map (fn [[k v]] [k {:value v}]))
                  (into {}))]
    (network/save
     cofx
     {:data       data
      :on-success (fn [network _]
                    {:dev-server/respond [200 {:message    "Network has been added."
                                               :network-id network}]})
      :on-failure (fn [_ _]
                    {:dev-server/respond [400 {:message "Please, check the validity of network information."}]})})))

(defmethod process-request! [:POST "network" "connect"]
  [{:keys [cofx data]}]
  (network/connect
   cofx
   {:network    (:id data)
    :on-success (fn [network _]
                  {:dev-server/respond [200 {:message    "Network has been connected."
                                             :network-id network}]})
    :on-failure (fn [_ _]
                  {:dev-server/respond [400 {:message "The network id you provided doesn't exist."}]})}))

(defmethod process-request! [:DELETE "network" nil]
  [{:keys [cofx data]}]
  (network/delete
   cofx
   {:network    (:id data)
    :on-success (fn [network _]
                  {:dev-server/respond [200 {:message    "Network has been deleted."
                                             :network-id network}]})
    :on-failure (fn [_ _]
                  {:dev-server/respond [400 {:message "Cannot delete the provided network."}]})}))

(defmethod process-request! :default
  [{:keys [type url]}]
  {:dev-server/respond [404 {:message (str "Not found (" (name type) " " (string/join "/" url) ")")}]})
