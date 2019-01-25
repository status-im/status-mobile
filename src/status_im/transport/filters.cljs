(ns ^{:doc "API for whisper filters"}
 status-im.transport.filters
  (:require [re-frame.core :as re-frame]
            [status-im.mailserver.core :as mailserver]
            [status-im.transport.utils :as utils]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.transport.partitioned-topic :as transport.topic]
            [taoensso.timbre :as log]
            [status-im.contact.core :as contact]))

(defn- receive-message [chat-id js-error js-message]
  (re-frame/dispatch [:transport/messages-received js-error js-message chat-id]))

(defn remove-filter! [{:keys [chat-id filter success-callback?]
                       :or   {success-callback? true}}]
  (.stopWatching filter
                 (fn [error _]
                   (if error
                     (log/warn :remove-filter-error filter error)
                     (when success-callback?
                       (re-frame/dispatch [:shh.callback/filter-removed chat-id])))))
  (log/debug :stop-watching filter))

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
 :shh/add-filters
 (fn [{:keys [web3 filters]}]
   (log/debug "PERF" :shh/add-filters)
   (let [filters
         (reduce
          (fn [acc {:keys [sym-key-id topic chat-id]}]
            (conj acc
                  {:options  {:topics   [topic]
                              :symKeyID sym-key-id}
                   :callback (partial receive-message chat-id)
                   :chat-id  chat-id}))
          []
          filters)]
     (add-filters! web3 filters))))

(re-frame/reg-fx
 :shh/add-discovery-filters
 (fn [{:keys [web3 private-key-id topics]}]
   (let [params   {:privateKeyID private-key-id}]
     (add-filters!
      web3
      (map (fn [{:keys [chat-id topic callback minPow]}]
             {:options  (cond-> (assoc params :topics [topic])
                          minPow
                          (assoc :minPow minPow))
              ;; We don't pass a chat id on discovery-filters as we might receive
              ;; messages for multiple chats
              :callback (or callback (partial receive-message nil))
              :chat-id  chat-id}) topics)))))

(fx/defn add-filter
  [{:keys [db]} chat-id filter]
  {:db (update-in db [:transport/filters chat-id] conj filter)})

(handlers/register-handler-fx
 :shh.callback/filters-added
 (fn [{:keys [db] :as cofx} [_ filters]]
   (log/debug "PERF" :shh.callback/filters-added)
   (let [{:keys [action public-key]} (:filters/after-adding-discovery-filter db)
         filters-fx-fns
         (mapcat
          (fn [{:keys [topic chat-id filter]}]
            [(add-filter chat-id filter)
             (mailserver/upsert-mailserver-topic {:topic   topic
                                                  :chat-id chat-id})])
          filters)]
     (apply fx/merge cofx
            {:db (dissoc db :filters/after-adding-discovery-filter)}
            (mailserver/reset-request-to)
            (concat
             [(when action
                (case action
                  :add-contact
                  (contact/add-contact public-key)

                  :add-contact-and-open-chat
                  (contact/add-contact-and-open-chat public-key)))]
             filters-fx-fns
             [(mailserver/process-next-messages-request)])))))

(handlers/register-handler-fx
 :shh.callback/filter-removed
 (fn [{:keys [db]} [_ chat-id]]
   {:db (update db :transport/filters dissoc chat-id)}))

(re-frame/reg-fx
 :shh/remove-filter
 (fn [filters]
   (doseq [{:keys [filter] :as params} filters]
     (when filter (remove-filter! params)))))

(re-frame/reg-fx
 :shh/remove-filters
 (fn [{:keys [filters callback]}]
   (doseq [[chat-id filter] filters]
     (when filter (remove-filter!
                   {:chat-id          chat-id
                    :filter           filter
                    :success-callback false})))
   (when callback (callback))))
