(ns status-im.utils.contact-code.model
  (:require
   [status-im.utils.config :as config]
   [status-im.utils.ethereum.stateofus :as stateofus]
   [status-im.ui.screens.add-new.new-chat.db :as db]
   [status-im.utils.ethereum.core :as ethereum]
   [status-im.utils.ethereum.ens :as ens]
   [status-im.native-module.core :as status]))

(defn process-stateofus [username callback {{:keys [web3 network network-status] :as db} :db}]
  (let [network (get-in db [:account/account :networks network])
        chain   (ethereum/network->chain-keyword network)]
    {:db (dissoc db :contacts/new-identity-error)
     :resolve-whisper-identity {:web3 web3
                                :registry (get ens/ens-registries
                                               chain)
                                :ens-name username
                                :cb callback}}))

(defn create! [callback]
  (when config/encryption-enabled?
    (status/create-contact-code callback)))

(defn valid-public-key? [s]
  (boolean (re-matches #"0x04[0-9a-f]{128}" s)))

(defn contact-code? [s]
  (not (or
        (valid-public-key? s)
        (stateofus/is-valid-name? s))))

(defn validate [contact-code callback {:keys [db] :as cofx}]
  (cond
    (stateofus/is-valid-name? contact-code)
    (process-stateofus contact-code callback cofx)

    (and config/encryption-enabled? (contact-code? contact-code))
    (status/extract-identity-from-contact-code contact-code callback)

    :else
    {:db (assoc db
                :contacts/new-identity
                contact-code

                :contacts/new-identity-error
                (db/validate-pub-key (:account/account db) contact-code))}))

(defn add [contact-code {:keys [db]}]
  (when config/encryption-enabled?
    {:db (assoc db :contact-code/contact-code contact-code)}))

(defn fetch [cofx]
  (if config/encryption-enabled?
    (get-in cofx [:db :contact-code/contact-code])
    (get-in cofx [:db :current-public-key])))
