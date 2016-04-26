(ns syng-im.handlers.contacts
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.string :as cstr]
            [cljs.core.async :as async :refer [chan put! <!]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.utils.utils :refer [log on-error http-post toast]]
            [syng-im.utils.crypt :refer [encrypt]]
            [syng-im.utils.phone-number :refer [format-phone-number]]
            [syng-im.models.contacts :as contacts-model]
            [syng-im.utils.logging :as log]))

(defn- get-contact-name [phone-contact]
  (cstr/join " "
             (remove cstr/blank?
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
          (dispatch [:load-syng-contacts])
          (handler))))))
