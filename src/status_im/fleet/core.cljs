(ns status-im.fleet.core
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.types :as types])
  (:require-macros [status-im.utils.slurp :refer [slurp]]))

(defn current-fleet
  ([db]
   (current-fleet db nil))
  ([db address]
   (keyword (or (if address
                  (get-in db [:accounts/accounts address :settings :fleet])
                  (get-in db [:account/account :settings :fleet]))
                config/fleet))))

(def fleets
  (:fleets (types/json->clj (slurp "resources/config/fleets.json"))))

(defn format-wnode
  [wnode address]
  {:id wnode
   :name (name wnode)
   :password constants/inbox-password
   :address address})

(defn format-wnodes
  [wnodes]
  (reduce (fn [acc [wnode address]]
            (assoc acc wnode (format-wnode wnode address)))
          {}
          wnodes))

(def default-wnodes
  (reduce (fn [acc [fleet node-by-type]]
            (assoc acc fleet (format-wnodes (:mail node-by-type))))
          {}
          fleets))

(defn show-save-confirmation
  [fleet {:keys [db] :as cofx}]
  {:ui/show-confirmation {:title               (i18n/label :t/close-app-title)
                          :content             (i18n/label :t/change-fleet
                                                           {:fleet fleet})
                          :confirm-button-text (i18n/label :t/close-app-button)
                          :on-accept           #(re-frame/dispatch [:fleet.ui/save-fleet-confirmed (keyword fleet)])
                          :on-cancel           nil}})

(defn save
  [fleet {:keys [db now] :as cofx}]
  (let [settings (get-in db [:account/account :settings])]
    (handlers-macro/merge-fx cofx
                             (accounts.update/update-settings
                              (if fleet
                                (assoc settings :fleet fleet)
                                (dissoc settings :fleet))
                              [:accounts.update.callback/save-settings-success]))))
