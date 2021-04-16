(ns status-im.anon-metrics.core
  (:require [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [re-frame.interceptor :refer [->interceptor]]
            [status-im.async-storage.core :as async-storage]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.utils.async :refer [async-periodic-exec async-periodic-stop!]]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.platform :as platform]
            [status-im.utils.build :as build]
            [status-im.utils.fx :as fx]
            [status-im.utils.config :as config]
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
       {:method     "appmetrics_saveAppMetrics"
        :params     [outstanding-events]
        :on-success #()
        :on-error   (fn [err]
                      (log/error {:error  err
                                  :events outstanding-events})
                      (log/warn "The logged events will be rejected"))}))))

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
       (when @periodic-tasks-chan
         (async-periodic-stop! @periodic-tasks-chan)
         (onboard-events) ; final onboard, will save and clear any pending events
         (reset! periodic-tasks-chan nil))))))

(fx/defn start-transferring
  [_]
  {::transfer-data true})

(fx/defn stop-transferring
  [_]
  {::transfer-data false})

(defn transform-and-log [context]
  (let [transformed-payload (txf/transform context)
        should-send?        (get-in context
                                    [:coeffects
                                     :db
                                     :multiaccount
                                     :anon-metrics/should-send?]
                                    false)]
    (when (and config/metrics-enabled? ;metrics are enabled
               should-send? ; user opted-in
               transformed-payload); we were able to transform metrics-shape
      (swap!
       events-foyer
       conj
       {:event       (-> context :coeffects :event first)
        :value       transformed-payload
        :app_version build/version
        :os          platform/os}))))

(re-frame/reg-fx ::transform-and-log transform-and-log)

(defn catch-events-before [context]
  (transform-and-log context)
  context)

(def interceptor
  (->interceptor
   :id     :catch-events
   :before catch-events-before))

(fx/defn hoax-capture-event
  "Due to usage of fx/defn with fx/merge, it might not be able to
  intercept some events (like navigate-to-cofx). In cases like that,
  this hoax capture event can be used in conjunction with `fx/merge`"
  {:events [::hoax-capture-event]}
  [_ {:keys [og-event]}]
  ;; re-shape event to look like a context object
  {::transform-and-log {:coeffects {:event og-event}}})

(fx/defn fetch-local-metrics-success
  {:events [::fetch-local-metrics-success]}
  [{:keys [db]} {:keys [metrics total-count clear-existing?]}]
  {:db (-> db
           (as-> db
                 (if clear-existing?
                   (assoc db
                          :anon-metrics/events metrics
                          :anon-metrics/total-count total-count)
                   (update db :anon-metrics/events concat metrics)))
           (dissoc :anon-metrics/fetching?))})

(fx/defn fetch-local-metrics
  {:events [::fetch-local-metrics]}
  [{:keys [db]} {:keys [limit offset clear-existing?]}]
  {::json-rpc/call [{:method     "appmetrics_getAppMetrics"
                     :params     [(or limit 3) (or offset 0)]
                     :on-success #(re-frame/dispatch
                                   [::fetch-local-metrics-success
                                    {:metrics (get % :AppMetrics [])
                                     :total-count (get % :TotalCount 0)
                                     :clear-existing? clear-existing?}])}]
   :db (assoc db :anon-metrics/fetching? true)})

(fx/defn set-opt-in-screen-displayed-flag
  [{:keys [db]}]
  {::async-storage/set! {:anon-metrics/opt-in-screen-displayed? true}
   :db (assoc db :anon-metrics/opt-in-screen-displayed? true)})

(fx/defn set-show-thank-you
  {:events [::set-show-thank-you]}
  [{:keys [db]} show?]
  {:db (assoc db :anon-metrics/show-thank-you? show?)})

(defn on-opt-in-success [enabled?]
  (if enabled?
    (do
      ;; animate
      (re-frame/dispatch [::set-show-thank-you true])
      ;; and redirect after 1 second
      (js/setTimeout
       (fn []
         (re-frame/dispatch [:navigate-reset {:index  0
                                              :routes [{:name :tabs}]}])
         (re-frame/dispatch [::set-show-thank-you false]))
       1000))
    (re-frame/dispatch [:navigate-reset {:index  0
                                         :routes [{:name :tabs}]}])))

(fx/defn fetch-opt-in-displayed-success
  {:events [::fetch-opt-in-displayed-success]}
  [{:keys [db]} [k v]]
  {:db (assoc db k v)})

(fx/defn fetch-opt-in-screen-displayed?
  {:events [::fetch-opt-in-screen-displayed?]}
  [cofx]
  {::async-storage/get
   {:keys [:anon-metrics/opt-in-screen-displayed?]
    :cb #(re-frame/dispatch [::fetch-opt-in-displayed-success (first %)])}})

(fx/defn opt-in
  {:events [::opt-in]}
  [cofx enabled?]
  (fx/merge cofx
            (set-opt-in-screen-displayed-flag)
            (if enabled?
              (start-transferring)
              (stop-transferring))
            (multiaccounts.update/multiaccount-update
             :anon-metrics/should-send? enabled?
             {:on-success #(on-opt-in-success enabled?)})))

(comment
  ;; read the database
  (def events-in-db (atom nil))
  (->> events-in-db
       deref
       (take-last 5))
  (json-rpc/call {:method     "appmetrics_getAppMetrics"
                  :params     [1000 0] ; limit, offset
                  :on-success #(reset! events-in-db %)}))

