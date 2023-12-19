(ns legacy.status-im.fleet.core
  (:require
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [legacy.status-im.node.core :as node]
    [re-frame.core :as re-frame]
    [status-im2.config :as config]
    [status-im2.constants :as constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn current-fleet-sub
  [multiaccount]
  (keyword (or (get multiaccount :fleet)
               config/fleet)))

(defn format-mailserver
  [mailserver address]
  {:id       mailserver
   :name     (name mailserver)
   :password constants/mailserver-password
   :address  address})

(defn format-mailservers
  [mailservers]
  (reduce (fn [acc [mailserver address]]
            (assoc acc mailserver (format-mailserver mailserver address)))
          {}
          mailservers))

(defn default-mailservers
  [db]
  (reduce (fn [acc [fleet node-by-type]]
            (assoc acc fleet (format-mailservers (:mail node-by-type))))
          {}
          (node/fleets db)))

(rf/defn show-save-confirmation
  {:events [:fleet.ui/fleet-selected]}
  [_ fleet]
  {:ui/show-confirmation
   {:title (i18n/label :t/close-app-title)
    :content (i18n/label :t/change-fleet
                         {:fleet fleet})
    :confirm-button-text (i18n/label :t/close-app-button)
    :on-accept
    #(re-frame/dispatch [:fleet.ui/save-fleet-confirmed (keyword fleet)])
    :on-cancel nil}})

(defn nodes->fleet
  [nodes]
  (letfn [(format-nodes [nodes]
            (reduce (fn [acc n]
                      (assoc acc
                             (keyword n)
                             n))
                    {}
                    nodes))]
    {:boot    (format-nodes nodes)
     :mail    (format-nodes nodes)
     :whisper (format-nodes nodes)}))

(rf/defn set-nodes
  [{:keys [db]} fleet nodes]
  {:db (-> db
           (assoc-in [:custom-fleets fleet] (nodes->fleet nodes))
           (assoc-in [:mailserver/mailservers fleet]
                     (format-mailservers
                      (reduce
                       (fn [acc e]
                         (assoc acc
                                (keyword e)
                                e))
                       {}
                       nodes))))})

(rf/defn save
  {:events [:fleet.ui/save-fleet-confirmed]}
  [{:keys [db now] :as cofx} fleet]
  (let [old-fleet (get-in db [:profile/profile :fleet])]
    (when (not= fleet old-fleet)
      (rf/merge
       cofx
       (multiaccounts.update/multiaccount-update :fleet fleet {})
       (node/prepare-new-config
        {:on-success
         #(re-frame/dispatch
           [:multiaccounts.update.callback/save-settings-success])})))))
