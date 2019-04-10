(ns status-im.fleet.core
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.utils.types :as types]
            [status-im.utils.fx :as fx])
  (:require-macros [status-im.utils.slurp :refer [slurp]]))

(defn current-fleet
  ([db]
   (current-fleet db nil))
  ([db address]
   (keyword (or (if address
                  (get-in db [:accounts/accounts address :settings :fleet])
                  (get-in db [:account/account :settings :fleet]))
                config/fleet))))

(def fleets-with-les
  [:les.dev.ropsten :les.dev.mainnet])

(defn fleet-supports-les? [fleet]
  (not (nil? (some #(= fleet %) fleets-with-les))))

(def default-fleets (slurp "resources/config/fleets.json"))
(def default-les-fleets (slurp "resources/config/fleets-les.json"))

(defn fleets [{:keys [custom-fleets]}]
  (as-> [default-fleets
         default-les-fleets] $
    (mapv #(:fleets (types/json->clj %)) $)
    (conj $ custom-fleets)
    (reduce merge $)))

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
          (fleets db)))

(fx/defn show-save-confirmation
  [{:keys [db] :as cofx} fleet]
  {:ui/show-confirmation {:title               (i18n/label :t/close-app-title)
                          :content             (i18n/label :t/change-fleet
                                                           {:fleet fleet})
                          :confirm-button-text (i18n/label :t/close-app-button)
                          :on-accept           #(re-frame/dispatch [:fleet.ui/save-fleet-confirmed (keyword fleet)])
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
  (let [settings (get-in db [:account/account :settings])]
    (accounts.update/update-settings cofx
                                     (if fleet
                                       (assoc settings :fleet fleet)
                                       (dissoc settings :fleet))
                                     {:success-event [:accounts.update.callback/save-settings-success]})))
