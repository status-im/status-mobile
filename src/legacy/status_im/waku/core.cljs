(ns legacy.status-im.waku.core
  (:require
    [clojure.string :as string]
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [legacy.status-im.node.core :as node]
    [re-frame.core :as re-frame]
    [status-im2.navigation.events :as navigation]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/defn switch-waku-bloom-filter-mode
  {:events [:multiaccounts.ui/waku-bloom-filter-mode-switched]}
  [cofx enabled?]
  (rf/merge cofx
            (multiaccounts.update/multiaccount-update
             :waku-bloom-filter-mode
             enabled?
             {})
            (node/prepare-new-config
             {:on-success #(re-frame/dispatch [:logout])})))

(def address-regex #"/ip4/\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}/tcp/\d{1,5}/p2p/[a-zA-Z0-9]+")

(defn valid-address?
  [address]
  (re-matches address-regex address))

(rf/defn set-input
  {:events [:wakuv2.ui/input-changed]}
  [{:keys [db] :as cofx} input-key value]
  {:db (assoc-in db
        [:wakuv2-nodes/manage input-key]
        {:value value
         :error (case input-key
                  :name    (string/blank? value)
                  :address (when value (not (valid-address? value))))})})

(rf/defn enter-settings
  {:events       [:wakuv2.ui/enter-settings-pressed]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)]}
  [{:keys [db random-guid-generator] :as cofx}]
  (let [custom-nodes (into {}
                           (map #(vector (random-guid-generator)
                                         {:name (name (first %1)) :address (second %1)})
                                (get-in db [:profile/profile :wakuv2-config :CustomNodes])))]
    (rf/merge cofx
              {:db       (assoc db :wakuv2-nodes/list custom-nodes)
               :dispatch [:navigate-to :wakuv2-settings]})))

(rf/defn edit
  {:events       [:wakuv2.ui/add-node-pressed]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)]}
  [{:keys [db random-guid-generator] :as cofx} id]
  (let [{:keys [name address]} (get-in db [:wakuv2-nodes/list id])
        id                     (or id (random-guid-generator))
        fxs                    (rf/merge cofx
                                         {:db (update db
                                                      :wakuv2-nodes/manage
                                                      assoc
                                                      :new? (nil? name)
                                                      :id id)}
                                         (set-input :name name)
                                         (set-input :address address))]
    (assoc fxs :dispatch [:navigate-to :edit-wakuv2-node])))

(rf/defn delete
  [{:keys [db] :as cofx} id]
  (rf/merge cofx
            {:db (-> db
                     (update :wakuv2-nodes/list dissoc id)
                     (dissoc :wakuv2-nodes/manage))}))

(rf/defn save-node
  {:events [:wakuv2.ui/save-node-pressed]}
  [{:keys [db] :as cofx}]
  (let [manage (:wakuv2-nodes/manage db)
        id     (:id manage)]
    (rf/merge cofx
              {:db       (-> db
                             (assoc-in [:wakuv2-nodes/list id]
                                       {:name    (get-in manage [:name :value])
                                        :address (get-in manage [:address :value])})
                             (dissoc :wakuv2-nodes/manage))
               :dispatch [:navigate-back]})))

(rf/defn discard-all
  {:events [:wakuv2.ui/discard-all-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db       (dissoc db :wakuv2-nodes/manage :wakuv2-nodes/list)
             :dispatch [:navigate-back]}))

(rf/defn save-all-pressed
  {:events [:wakuv2.ui/save-all-pressed]}
  [{:keys [db] :as cofx}]
  {:ui/show-confirmation
   {:title (i18n/label :t/close-app-title)
    :content (i18n/label :t/wakuv2-change-nodes)
    :confirm-button-text (i18n/label :t/close-app-button)
    :on-accept
    #(re-frame/dispatch [:wakuv2.ui/save-all-confirmed])
    :on-cancel nil}})

(rf/defn save-all
  {:events [:wakuv2.ui/save-all-confirmed]}
  [{:keys [db] :as cofx}]
  (let [new-nodes (->> (:wakuv2-nodes/list db)
                       vals
                       (map #(vector (:name %1) (:address %1)))
                       (into {}))]
    (rf/merge cofx
              {:db       (-> db
                             (assoc-in [:profile/profile :wakuv2-config :CustomNodes] new-nodes)
                             (dissoc :wakuv2-nodes/manage :wakuv2-nodes/list))
               :dispatch [:navigate-back]}
              (node/prepare-new-config
               {:on-success #(re-frame/dispatch [:logout])}))))

(rf/defn show-delete-node-confirmation
  {:events [:wakuv2.ui/delete-pressed]}
  [_ node-name]
  {:ui/show-confirmation {:title               (i18n/label :t/delete-node-title)
                          :content             (i18n/label :t/delete-node-are-you-sure)
                          :confirm-button-text (i18n/label :t/delete-node)
                          :on-accept           #(re-frame/dispatch [:wakuv2.ui/delete-confirmed
                                                                    node-name])}})

(rf/defn delete-node
  {:events [:wakuv2.ui/delete-confirmed]}
  [cofx id]
  (rf/merge cofx
            (delete id)
            (navigation/navigate-back)))
