(ns messenger.comm.intercom
  (:require [cljs.core.async :as async :refer [put!]]
            [messenger.state :refer [state
                                     pub-sub-publisher]]
            [syng-im.utils.logging :as log]))

(defn publish! [topic message]
  (let [publisher (->> (state)
                       (pub-sub-publisher))]
    (put! publisher [topic message])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; user data

(defn set-user-phone-number [phone-number]
  (publish! :service [:user-data :user-data/set-phone-number phone-number]))

(defn save-user-phone-number [phone-number]
  (publish! :service [:user-data :user-data/save-phone-number phone-number]))

(defn load-user-phone-number []
  ;; :service [service_name action_id args_map]
  (publish! :service [:user-data :user-data/load-phone-number nil]))

(defn set-confirmation-code [confirmation-code]
  (publish! :service [:user-data :user-data/set-confirmation-code confirmation-code]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; server

(defn sign-up [phone-number whisper-identity handler]
  (publish! :service [:server :server/sign-up {:phone-number     phone-number
                                               :whisper-identity whisper-identity
                                               :handler          handler}]))

(defn sign-up-confirm [confirmation-code handler]
  (publish! :service [:server :server/sign-up-confirm
                      {:confirmation-code confirmation-code
                       :handler           handler}]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; contacts

(defn load-syng-contacts []
  (publish! :service [:contacts :contacts/load-syng-contacts nil]))

(defn sync-contacts [handler]
  (publish! :service [:contacts :contacts/sync-contacts handler]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; protocol

(defn protocol-initialized [identity]
  (publish! :service [:protocol :protocol/initialized {:identity identity}]))

(defn save-new-msg [from payload]
  (publish! :service [:protocol :protocol/save-new-msg {:from    from
                                                        :payload payload}]))
