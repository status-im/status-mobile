(ns ^{:doc "API for whisper filters"}
 status-im.transport.filters
  (:require [re-frame.core :as re-frame]
            [status-im.mailserver.core :as mailserver]
            [status-im.transport.utils :as utils]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [taoensso.timbre :as log]))

(defn remove-filter! [{:keys [chat-id filter success-callback?]
                       :or   {success-callback? true}}]
  (.stopWatching filter
                 (fn [error _]
                   (if error
                     (log/warn :remove-filter-error filter error)
                     (when success-callback?
                       (re-frame/dispatch [:shh.callback/filter-removed chat-id])))))
  (log/debug :stop-watching filter))

(defn add-filter!
  [web3 {:keys [topics] :as options} callback chat-id]
  (let [options  (assoc options :allowP2P true)]
    (log/debug :add-filter options)
    (when-let [filter (.newMessageFilter (utils/shh web3)
                                         (clj->js options)
                                         callback
                                         #(log/warn :add-filter-error (.stringify js/JSON (clj->js options)) %))]
      (re-frame/dispatch [:shh.callback/filter-added (first topics) chat-id filter]))))

(defn add-filters!
  [web3 filters]
  (log/debug "PERF" :add-filters (first filters))
  (re-frame/dispatch
   [:shh.callback/filters-added
    (keep
     (fn [{:keys [options callback chat-id]}]
       (when-let [filter (.newMessageFilter
                          (utils/shh web3)
                          (clj->js (assoc options :allowP2P true))
                          callback
                          #(log/warn :add-filter-error
                                     (.stringify js/JSON (clj->js options)) %))]
         {:topic   (first (:topics options))
          :chat-id chat-id
          :filter  filter}))
     filters)]))

(re-frame/reg-fx
 :shh/add-filter
 (fn [{:keys [web3 sym-key-id topic chat-id]}]
   (let [params   {:topics [topic]
                   :symKeyID sym-key-id}
         callback (fn [js-error js-message]
                    (re-frame/dispatch [:transport/messages-received js-error js-message chat-id]))]
     (add-filter! web3 params callback chat-id))))

(re-frame/reg-fx
 :shh/add-filters
 (fn [{:keys [web3 filters]}]
   (log/debug "PERF" :shh/add-filters)
   (let [filters
         (reduce
          (fn [acc {:keys [sym-key-id topic chat-id]}]
            (conj acc
                  {:options  {:topics   [topic]
                              :symKeyID sym-key-id}
                   :callback (fn [js-error js-message]
                               (re-frame/dispatch [:transport/messages-received
                                                   js-error js-message chat-id]))
                   :chat-id  chat-id}))
          []
          filters)]
     (add-filters! web3 filters))))

(re-frame/reg-fx
 :shh/add-discovery-filters
 (fn [{:keys [web3 private-key-id topics]}]
   (let [params   {:topics (mapv :topic topics)
                   :privateKeyID private-key-id}
         callback (fn [js-error js-message]
                    (re-frame/dispatch [:transport/messages-received js-error js-message]))]
     (doseq [{:keys [chat-id topic]} topics]
       (add-filter! web3 params callback chat-id)))))

(handlers/register-handler-fx
 :shh.callback/filter-added
 (fn [{:keys [db] :as cofx} [_ topic chat-id filter]]
   (log/debug "PERF" :shh.callback/filter-added)
   (fx/merge cofx
             {:db (assoc-in db [:transport/filters chat-id] filter)}
             (mailserver/reset-request-to)
             (mailserver/upsert-mailserver-topic {:topic topic
                                                  :chat-id chat-id})
             (mailserver/process-next-messages-request))))

(fx/defn add-filter
  [{:keys [db]} chat-id filter]
  {:db (assoc-in db [:transport/filters chat-id] filter)})

(handlers/register-handler-fx
 :shh.callback/filters-added
 (fn [cofx [_ filters]]
   (log/debug "PERF" :shh.callback/filters-added)
   (let [filters-fx-fns
         (mapcat
          (fn [{:keys [topic chat-id filter]}]
            [(add-filter chat-id filter)
             (mailserver/upsert-mailserver-topic {:topic   topic
                                                  :chat-id chat-id})])
          filters)]
     (apply fx/merge cofx
            (mailserver/reset-request-to)
            (concat
             filters-fx-fns
             [(mailserver/process-next-messages-request)])))))

(handlers/register-handler-fx
 :shh.callback/filter-removed
 (fn [{:keys [db]} [_ chat-id]]
   {:db (update db :transport/filters dissoc chat-id)}))

(re-frame/reg-fx
 :shh/remove-filter
 (fn [{:keys [filter] :as params}]
   (when filter (remove-filter! params))))

(re-frame/reg-fx
 :shh/remove-filters
 (fn [[filters callback]]
   (doseq [[chat-id filter] filters]
     (when filter (remove-filter!
                   {:chat-id          chat-id
                    :filter           filter
                    :success-callback false})))
   (callback)))
