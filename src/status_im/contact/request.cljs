(ns status-im.contact.request
  (:require [status-im.contact.device-info :as device-info]
            [status-im.utils.fx :as fx]
            [status-im.transport.message.contact :as message.contact]))

(defn- own-info [db]
  (let [{:keys [name photo-path address]} (:account/account db)
        fcm-token (get-in db [:notifications :fcm-token])]
    {:name          name
     :profile-image photo-path
     :address       address
     :device-info   (device-info/all {:db db})
     :fcm-token     fcm-token}))

(fx/defn build
  [{:keys [db] :as cofx} {:keys [public-key pending? dapp?] :as contact} message]
  (if pending?
    (message.contact/map->ContactRequestConfirmed (own-info db))
    (message.contact/map->ContactRequest (assoc (own-info db)
                                                :message message))))
