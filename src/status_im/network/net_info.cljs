(ns status-im.network.net-info
  (:require [taoensso.timbre :as log]
            [status-im.ui.components.react :as react-components]
            [status-im.utils.platform :as platform]
            [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.mailserver.core :as mailserver]
            [status-im.utils.handlers :as handlers]
            [status-im.chaos-mode.core :as chaos-mode]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.mobile-network-settings.events :as mobile-network]))

(defn is-connected? [callback]
  (when react-components/net-info
    (.then (.fetch (.-isConnected react-components/net-info))
           (fn [is-connected?]
             (log/debug "Is connected?" is-connected?)
             (callback is-connected?)))))

(defn- wrap-net-info [callback]
  (fn [info-js]
    (let [info       (js->clj info-js :keywordize-keys true)
          on-success #(callback {:type (:type info) :expensive? %})]
      (if platform/ios?
        (on-success false)
        (.. react-components/net-info
            isConnectionExpensive
            (then on-success)
            (catch (fn [error] (log/warn "isConnectionExpensive: " error))))))))

(defn net-info [callback]
  (when react-components/net-info
    (.then (.getConnectionInfo react-components/net-info)
           (wrap-net-info callback))))

(defn add-connection-listener [listener]
  (when react-components/net-info
    (.addEventListener (.-isConnected react-components/net-info) "connectionChange" listener)))

(defn add-net-info-listener [listener]
  (when react-components/net-info
    (.addEventListener react-components/net-info "connectionChange"
                       (wrap-net-info listener))))

(re-frame/reg-fx
 :network/listen-to-network-status
 (fn []
   (let [callback-event #(re-frame/dispatch [:network/network-status-changed %])]
     (net-info callback-event)
     (add-net-info-listener callback-event))))

(re-frame/reg-fx
 :network/listen-to-connection-status
 (fn []
   (let [callback-event #(re-frame/dispatch [:network/connection-status-changed %])]
     (is-connected? callback-event)
     (add-connection-listener callback-event))))

(fx/defn handle-connection-status-change
  [{:keys [db] :as cofx} is-connected?]
  (fx/merge cofx
            {:db (assoc db :network-status (if is-connected? :online :offline))}
            (mailserver/network-connection-status-changed is-connected?)))

(handlers/register-handler-fx
 :network/connection-status-changed
 (fn [{db :db :as cofx} [_ is-connected?]]
   (handle-connection-status-change cofx is-connected?)))

(fx/defn handle-network-status-change
  [{:keys [db] :as cofx} {:keys [type] :as data}]
  (let [old-network-type (:network/type db)]
    (fx/merge
     cofx
     {:db                       (assoc db :network/type type)
      :network/notify-status-go data}
     (when (= "none" old-network-type)
       (chaos-mode/check-chaos-mode))
     (mobile-network/on-network-status-change))))

(handlers/register-handler-fx
 :network/network-status-changed
 (fn [cofx [_ data]]
   (handle-network-status-change cofx data)))

(re-frame/reg-fx
 :network/notify-status-go
 (fn [data]
   (status/connection-change data)))
