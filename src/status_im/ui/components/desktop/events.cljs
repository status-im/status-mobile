(ns status-im.ui.components.desktop.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.constants :as constants]
            [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [taoensso.timbre :as log]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.platform :as platform]))

(fx/defn change-tab
  [{:keys [db]} tab-name]
  {:db (assoc-in db [:desktop/desktop :tab-view-id] tab-name)})

(fx/defn navigate-to
  [{:keys [db] :as cofx} tab-name]
  (navigation/navigate-to-cofx cofx
                               (if (and (= tab-name :home) (:current-chat-id db))
                                 :chat
                                 :home)
                               nil))

(fx/defn show-desktop-tab
  [cofx tab-name]
  (fx/merge cofx
            (change-tab tab-name)
            (navigate-to tab-name)))

(handlers/register-handler-fx
 :show-desktop-tab
 (fn [cofx [_ tab-name]]
   (show-desktop-tab cofx tab-name)))

(defn packet-keys [direction]
  "helper for accessing deeply nested metrics keys"
  {:pre [(#{:in :out} direction)]}
  [:misc direction :packets :Overall])

(handlers/register-handler-fx
 :debug-metrics-success
 (fn [{:keys [db]} [_ {:keys [p2p mailserver les whisper], :as metrics}]]
   {:db (assoc-in db
                  [:desktop/desktop :debug-metrics]
                  {:mailserver-request-process-time (get-in mailserver [:requestProcessTime :Overall])
                   :mailserver-request-errors       (get-in mailserver [:requestErrors :Overall])
                   :les-packets-in                  (get-in les (packet-keys :in))
                   :les-packets-out                 (get-in les (packet-keys :out))
                   :p2p-inbound-traffic             (get-in p2p [:InboundTraffic :Overall])
                   :p2p-outbound-traffic            (get-in p2p [:OutboundTraffic :Overall])})}))

(defn debug-metrics-rpc-call [payload]
  "json rpc wrapper for debug metrics; dispatch :debug-metrics-success on success"
  (status/call-private-rpc
   payload
   (handlers/response-handler #(re-frame/dispatch [:debug-metrics-success %])
                              #(log/debug "we did not get the debug metrics" %))))

(handlers/register-handler-fx
 :load-debug-metrics
 (fn [{:keys [db]} _]
   (let [args    {:jsonrpc "2.0"
                  :id      2
                  :method  constants/debug-metrics
                  :params  [true]}
         payload (.stringify js/JSON (clj->js args))]
     (debug-metrics-rpc-call payload))))
