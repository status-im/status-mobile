(ns messenger.services.contacts
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.string :as cstr]
            [cljs.core.async :as async :refer [chan put! <!]]
            [messenger.utils.utils :refer [log on-error http-post toast]]
            [messenger.utils.crypt :refer [encrypt]]
            [messenger.comm.intercom :as intercom :refer [save-user-phone-number]]
            [messenger.models.contacts :as contacts-model]
            [syng-im.utils.logging :as log]))

(set! js/PhoneNumber (js/require "awesome-phonenumber"))
(def country-code "US")

(defn- format-phone-number [number]
  (str (.getNumber (js/PhoneNumber. number country-code "international"))))

(defn- get-contact-name [phone-contact]
  (cstr/join " "
             (filter #(not (cstr/blank? %))
                     [(:givenName phone-contact)
                      (:middleName phone-contact)
                      (:familyName phone-contact)])))

(defn- to-syng-contacts [contacts-by-hash data]
  (map (fn [server-contact]
         (let [number-info (get contacts-by-hash
                                (:phone-number-hash server-contact))
               phone-contact (:contact number-info)]
           {:phone-number (:number number-info)
            :whisper-identity (:whisper-identity server-contact)
            :name (get-contact-name phone-contact)
            :photo-path (:photo-path phone-contact)}))
       (js->clj (:contacts data))))

(defn- get-contacts-by-hash [contacts]
  (let [numbers-info (reduce (fn [numbers contact]
                               (into numbers
                                     (map (fn [c]
                                            {:number (format-phone-number (:number c))
                                             :contact contact})
                                          (:phone-numbers contact))))
                             '()
                             contacts)]
    (reduce (fn [m number-info]
              (let [number (:number number-info)
                    hash (encrypt number)]
                (assoc m hash number-info)))
            {}
            numbers-info)))

(defn- request-syng-contacts [contacts]
  (let [contacts-by-hash (get-contacts-by-hash contacts)
        data (keys contacts-by-hash)
        ch (chan)]
    (http-post "get-contacts" {:phone-number-hashes data}
               (fn [data]
                 (put! ch
                       (to-syng-contacts contacts-by-hash data))))
    ch))

(defn sync-contacts [handler]
  (go
    (let [result (<! (contacts-model/load-phone-contacts))]
      (if-let [error (:error result)]
        (on-error error)
        (let [syng-contacts (<! (request-syng-contacts (:contacts result)))]
          (contacts-model/save-syng-contacts syng-contacts)
          (handler))))))

(defn- load-syng-contacts []
  (contacts-model/load-syng-contacts))


(defmulti contacts (fn [state id args]
                     id))

(defmethod contacts :contacts/load-syng-contacts
  [state id args]
  (log/info "handling " id " args = " args)
  (load-syng-contacts))

(defmethod contacts :contacts/sync-contacts
  [state id args]
  (log/info "handling " id " args = " args)
  (sync-contacts args))

(defn contacts-handler [state [id args]]
  (log/info "user notification: " args)
  (contacts state id args))
