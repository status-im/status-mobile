(ns status-im.fleet.core
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.node.core :as node]
            [status-im.utils.config :as config]
            [status-im.utils.types :as types]
            [status-im.utils.fx :as fx]))

(defn current-fleet-sub [settings]
  (keyword (or (get settings :fleet)
               config/fleet)))

(defn format-mailserver
  [mailserver address]
  {:id mailserver
   :name (name mailserver)
   :password constants/mailserver-password
   :address address})

(defn format-mailservers
  [mailservers]
  (reduce (fn [acc [mailserver address]]
            (assoc acc mailserver (format-mailserver mailserver address)))
          {}
          mailservers))

(defn default-mailservers [db]
  (reduce (fn [acc [fleet node-by-type]]
            (assoc acc fleet (format-mailservers (:mail node-by-type))))
          {}
          (node/fleets db)))

(fx/defn show-save-confirmation
  [{:keys [db] :as cofx} fleet]
  {:ui/show-confirmation
   {:title               (i18n/label :t/close-app-title)
    :content             (i18n/label :t/change-fleet
                                     {:fleet fleet})
    :confirm-button-text (i18n/label :t/close-app-button)
    :on-accept
    #(re-frame/dispatch [:fleet.ui/save-fleet-confirmed (keyword fleet)])
    :on-cancel           nil}})

(defn nodes->fleet [nodes]
  (letfn [(format-nodes [nodes]
            (reduce (fn [acc n]
                      (assoc acc
                             (keyword n)
                             n))
                    {}
                    nodes))]
    {:boot (format-nodes nodes)
     :mail (format-nodes nodes)
     :whisper (format-nodes nodes)}))

(fx/defn set-nodes [{:keys [db]} fleet nodes]
  {:db (-> db
           (assoc-in [:custom-fleets fleet] (nodes->fleet nodes))
           (assoc-in [:mailserver/mailservers fleet] (format-mailservers
                                                      (reduce
                                                       (fn [acc e]
                                                         (assoc acc
                                                                (keyword e)
                                                                e))
                                                       {}
                                                       nodes))))})

(fx/defn save
  [{:keys [db now] :as cofx} fleet]
  (let [settings (get-in db [:multiaccount :settings])
        new-settings (if fleet
                       (assoc settings :fleet fleet)
                       (dissoc settings :fleet))]
    (fx/merge cofx
              (multiaccounts.update/update-settings new-settings {})
              (node/prepare-new-config
               {:on-success
                #(when (not= fleet
                             (:fleet settings))
                   (re-frame/dispatch
                    [:multiaccounts.update.callback/save-settings-success]))}))))
