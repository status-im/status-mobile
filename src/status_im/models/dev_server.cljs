(ns status-im.models.dev-server
  (:require [status-im.models.network :as models.network]
            [clojure.string :as string]))

(defn start-if-needed
  [{{:account/keys [account]} :db}]
  (let [{:keys [dev-mode?]} account]
    (when dev-mode?
      {:dev-server/start nil})))

;; Specific server operations

(defmulti process-request! (fn [{:keys [url type]}] [type (first url) (second url)]))

(defmethod process-request! [:POST "ping" nil]
  [_]
  {:dev-server/respond [200 {:message "Pong!"}]})

(defmethod process-request! [:POST "dapp" "open"]
  [{{:keys [url]} :data}]
  {:dispatch           [:open-url-in-browser url]
   :dev-server/respond [200 {:message "URL has been opened."}]})

(defmethod process-request! [:POST "network" nil]
  [{:keys [cofx data]}]
  (let [data (->> data
                  (map (fn [[k v]] [k {:value v}]))
                  (into {}))]
    (models.network/save
     cofx
     {:data       data
      :on-success (fn [network _]
                    {:dev-server/respond [200 {:message    "Network has been added."
                                               :network-id network}]})
      :on-failure (fn [_ _]
                    {:dev-server/respond [400 {:message "Please, check the validity of network information."}]})})))

(defmethod process-request! [:POST "network" "connect"]
  [{:keys [cofx data]}]
  (models.network/connect
   cofx
   {:network    (:id data)
    :on-success (fn [network _]
                  {:dev-server/respond [200 {:message    "Network has been connected."
                                             :network-id network}]})
    :on-failure (fn [_ _]
                  {:dev-server/respond [400 {:message "The network id you provided doesn't exist."}]})}))

(defmethod process-request! [:DELETE "network" nil]
  [{:keys [cofx data]}]
  (models.network/delete
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