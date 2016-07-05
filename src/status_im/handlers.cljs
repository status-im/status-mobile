(ns status-im.handlers
  (:require
    [re-frame.core :refer [after dispatch debug]]
    [schema.core :as s :include-macros true]
    [status-im.db :refer [app-db schema]]
    [status-im.persistence.simple-kv-store :as kv]
    [status-im.protocol.state.storage :as storage]
    [status-im.utils.logging :as log]
    [status-im.utils.crypt :refer [gen-random-bytes]]
    [status-im.components.react :refer [geth]]
    [status-im.utils.handlers :refer [register-handler] :as u]
    [status-im.models.protocol :as protocol]
    status-im.chat.handlers
    status-im.chat.handlers.animation
    status-im.group-settings.handlers
    status-im.navigation.handlers
    status-im.contacts.handlers
    status-im.discovery.handlers
    status-im.new-group.handlers
    status-im.participants.handlers
    status-im.commands.handlers.loading
    status-im.commands.handlers.jail
    status-im.qr-scanner.handlers
    status-im.accounts.handlers
    status-im.protocol.handlers
    status-im.chat.handlers.requests))

;; -- Middleware ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/wiki/Using-Handler-Middleware
;;
(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (if-let [problems (s/check a-schema db)]
    (throw (js/Error. (str "schema check failed: " problems)))))

(def validate-schema-mw
  (after (partial check-and-throw schema)))


;; -- Common --------------------------------------------------------------

(defn set-el [db [_ k v]]
  (assoc db k v))

(register-handler :set set-el)

(defn set-in [db [_ path v]]
  (assoc-in db path v))

(register-handler :set-in set-in)

(register-handler :set-animation
  (fn [db [_ k v]]
    (assoc-in db [:animations k] v)))

(register-handler :initialize-db
  (fn [_ _]
    (assoc app-db
      :signed-up (storage/get kv/kv-store :signed-up)
      :user-identity (protocol/stored-identity nil)
      :password (storage/get kv/kv-store :password))))

(register-handler :initialize-crypt
  (u/side-effect!
    (fn [_ _]
      (log/debug "initializing crypt")
      (gen-random-bytes 1024 (fn [{:keys [error buffer]}]
                               (if error
                                 (do
                                   (log/error "Failed to generate random bytes to initialize sjcl crypto")
                                   (dispatch [:notify-user {:type  :error
                                                            :error error}]))
                                 (do
                                   (->> (.toString buffer "hex")
                                        (.toBits (.. js/ecc -sjcl -codec -hex))
                                        (.addEntropy (.. js/ecc -sjcl -random)))
                                   (dispatch [:crypt-initialized]))))))))

(defn node-started [db result]
  (log/debug "Started Node: " result))

(register-handler :initialize-geth
  (u/side-effect!
   (fn [db _]
     (log/debug "Starting node")
     (.startNode geth (fn [result] (node-started db result))))))

(register-handler :crypt-initialized
  (u/side-effect!
    (fn [_ _]
      (log/debug "crypt initialized"))))

;; -- User data --------------------------------------------------------------
(register-handler :load-user-phone-number
  (fn [db [_]]
    ;; todo fetch phone number from db
    (assoc db :user-phone-number "123")))
