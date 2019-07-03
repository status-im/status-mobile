(ns status-im.models.dev-server
  (:require [clojure.string :as string]
            [status-im.browser.core :as browser]
            [status-im.network.module :as network]
            [status-im.utils.fx :as fx]))

(defn start
  []
  {:dev-server/start nil})

;; Specific server operations

(defmulti process-request! (fn [{:keys [url type]}] [type (first url) (second url)]))

(defmethod process-request! [:POST "ping" nil]
  [{:keys [request-id]}]
  {:dev-server/respond [request-id 200 {:message "Pong!"}]})

(defmethod process-request! [:POST "dapp" "open"]
  [{{:keys [request-id url]} :data cofx :cofx}]
  (fx/merge cofx
            {:dev-server/respond [request-id 200 {:message "URL has been opened."}]}
            (browser/open-url url)))

(defmethod process-request! [:POST "network" nil]
  [{:keys [cofx request-id data]}]
  (let [data (->> data
                  (map (fn [[k v]] [k {:value v}]))
                  (into {}))]
    (network/save
     cofx
     {:data       data
      :on-success (fn [network _]
                    {:dev-server/respond
                     [request-id 200 {:message    "Network has been added."
                                      :network-id network}]})
      :on-failure (fn [_ _]
                    {:dev-server/respond
                     [request-id 400 {:message "Please, check the validity of network information."}]})})))

(defmethod process-request! [:POST "network" "connect"]
  [{:keys [cofx request-id data]}]
  (network/connect
   cofx
   {:network-id (:id data)
    :on-success (fn [{:keys [network-id client-version]} _]
                  {:dev-server/respond
                   [request-id 200 {:message        "Network has been connected."
                                    :network-id     network-id
                                    :client-version client-version}]})
    :on-failure (fn [{:keys [network-id reason]} _]
                  {:dev-server/respond
                   [request-id 400 {:message    "Cannot connect the network."
                                    :network-id network-id
                                    :reason     reason}]})}))

(defmethod process-request! [:GET "networks" nil]
  [{:keys [cofx request-id]}]
  (let [{:keys [networks network]} (get-in cofx [:db :multiaccount])
        networks (->> networks
                      (map (fn [[id m]]
                             [id (-> m
                                     (select-keys [:id :name :config])
                                     (assoc :active? (= id network)))]))
                      (into {}))]
    {:dev-server/respond [request-id 200 {:networks networks}]}))

(defmethod process-request! [:DELETE "network" nil]
  [{:keys [cofx request-id data]}]
  (network/delete
   cofx
   {:network    (:id data)
    :on-success (fn [network _]
                  {:dev-server/respond [request-id 200 {:message    "Network has been deleted."
                                                        :network-id network}]})
    :on-failure (fn [_ _]
                  {:dev-server/respond [request-id 400 {:message "Cannot delete the provided network."}]})}))

(defmethod process-request! :default
  [{:keys [request-id type url]}]
  {:dev-server/respond [request-id 404 {:message (str "Not found (" (name type) " " (string/join "/" url) ")")}]})
