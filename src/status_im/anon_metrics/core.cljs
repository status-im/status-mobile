(ns status-im.anon-metrics.core
  (:require [status-im.ethereum.json-rpc :as json-rpc]
            [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [re-frame.interceptor :refer [->interceptor]]
            [status-im.utils.async :refer [async-periodic-exec async-periodic-stop!]]
            [status-im.utils.platform :as platform]
            [status-im.utils.build :as build]
            [status-im.utils.fx :as fx]
            [status-im.anon-metrics.transformers :as txf]))

(defonce events-foyer (atom []))
(defonce periodic-tasks-chan (atom nil))

(defn onboard-events
  "Check if there are any events in the foyer,
  flush them to the backend and clear foyer on-success."
  []
  (let [outstanding-events @events-foyer]
    (when (seq outstanding-events)
      (reset! events-foyer [])
      (json-rpc/call
       {:method   "appmetrics_saveAppMetrics"
        :params   [outstanding-events]
        :on-success #()
        :on-error (fn [err]
                    (log/error {:error  err
                                :events outstanding-events})
                    (log/warn "All outstanding events will be rejected"))}))))

(re-frame/reg-fx
 ::transfer-data
 (fn [transfer?]
   (if (and transfer?
            ;; double run safety
            (not @periodic-tasks-chan))
     (do
       (log/info "[anon-metrics] Start collection service")
       (reset! periodic-tasks-chan
               ;; interval = 4000 ms (run every `interval` ms)
               ;; timeout = 5000 ms (exit if the fn doesn't exit within `timeout` ms)
               (async-periodic-exec onboard-events 4000 5000)))
     (do
       (log/info "[anon-metrics] Stop collection service")
       (async-periodic-stop! @periodic-tasks-chan)
       (reset! periodic-tasks-chan nil)

       ;; final onboard, will save and clear any pending events
       (onboard-events)))))

(fx/defn start-transferring
  [_]
  {::transfer-data true})

(fx/defn stop-transferring
  [_]
  {::transfer-data false})

(defn transform-and-log [context]
  (when-let [transformed-payload (txf/transform context)]
    (swap!
     events-foyer
     conj
     {:event       (-> context :coeffects :event first)
      :value       transformed-payload
      :app_version build/version
      :os          platform/os})))

(defn catch-events-before [context]
  (transform-and-log context)
  context)

(def interceptor
  (->interceptor
   :id     :catch-events
   :before catch-events-before))

(re-frame/reg-fx ::transform-and-log transform-and-log)

(fx/defn hoax-capture-event
  "Due to usage of fx/defn with fx/merge, it might not be able to
  intercept some events (like navigate-to-cofx). In cases like that,
  this hoax capture event can be used in conjunction with `fx/merge`"
  {:events [::hoax-capture-event]}
  [_ {:keys [og-event]}]
  ;; re-shape event to look like a context object
  {::transform-and-log {:coeffects {:event og-event}}})

(comment
  ;; read the database
  (def events-in-db (atom nil))
  (->> events-in-db
       deref
       (take-last 5))
  (json-rpc/call {:method     "appmetrics_getAppMetrics"
                  :params     [1000 0] ; limit, offset
                  :on-success #(reset! events-in-db %)}))
