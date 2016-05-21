(ns syng-im.handlers
  (:require
    [re-frame.core :refer [register-handler after dispatch debug]]
    [schema.core :as s :include-macros true]
    [syng-im.db :refer [app-db schema]]
    [syng-im.persistence.simple-kv-store :as kv]
    [syng-im.protocol.state.storage :as storage]
    [syng-im.models.commands :refer [set-commands]]
    [syng-im.chat.suggestions :refer [load-commands]]
    [syng-im.utils.logging :as log]
    [syng-im.utils.crypt :refer [gen-random-bytes]]
    [syng-im.utils.handlers :as u]
    syng-im.chat.handlers
    syng-im.group-settings.handlers
    syng-im.navigation.handlers
    syng-im.contacts.handlers
    syng-im.discovery.handlers
    syng-im.new-group.handlers
    syng-im.participants.handlers
    syng-im.protocol.handlers))

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

(register-handler :set
  (debug
    (fn [db [_ k v]]
      (assoc db k v))))

(register-handler :set-in
  (debug
    (fn [db [_ path v]]
      (assoc-in db path v))))

(register-handler :initialize-db
  (fn [_ _]
    (assoc app-db
      :signed-up (storage/get kv/kv-store :signed-up))))

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

(register-handler :crypt-initialized
  (u/side-effect!
    (fn [_ _]
      (log/debug "crypt initialized"))))

(register-handler :load-commands
  (u/side-effect!
    (fn [_ [action]]
      (log/debug action)
      (load-commands))))

(register-handler :set-commands
  (fn [db [action commands]]
    (log/debug action commands)
    (set-commands db commands)))

;; -- User data --------------------------------------------------------------
(register-handler :load-user-phone-number
  (fn [db [_]]
    ;; todo fetch phone number from db
    (assoc db :user-phone-number "123")))
