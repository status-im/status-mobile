(ns ^{:doc "Mailserver events and API"} legacy.status-im.mailserver.core
  (:require
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/defn handle-mailserver-changed
  [{:keys [db]} ms]
  (when (seq ms)
    {:db (assoc db :mailserver/current-id (keyword ms))}))

(rf/defn handle-mailserver-available
  [{:keys [db]} ms]
  {:db (assoc db :mailserver/current-id (keyword ms))})

(rf/defn toggle-use-mailservers
  [_ value]
  {:json-rpc/call
   [{:method     "wakuext_toggleUseMailservers"
     :params     [value]
     :on-success #(log/info "successfully toggled use-mailservers" value)
     :on-error   #(log/error "failed to toggle use-mailserver" value %)}]})

(rf/defn update-use-mailservers
  {:events [:mailserver.ui/use-history-switch-pressed]}
  [cofx use-mailservers?]
  (rf/merge cofx
            (multiaccounts.update/optimistic :use-mailservers? use-mailservers?)
            (toggle-use-mailservers use-mailservers?)))

(defn add-mailservers
  [db mailservers]
  (reduce (fn [db {:keys [fleet id name] :as mailserver}]
            (let [updated-mailserver
                  (-> mailserver
                      (update :id keyword)
                      (assoc :name (if (seq name) name id))
                      (dissoc :fleet))]
              (assoc-in db
               [:mailserver/mailservers (keyword fleet) (keyword id)]
               updated-mailserver)))
          db
          mailservers))
