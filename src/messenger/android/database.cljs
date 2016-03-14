(ns messenger.android.database
  (:require [messenger.android.utils :refer [log toast http-post]]))

(set! js/Realm (js/require "realm"))

(def realm (js/Realm. (clj->js {:schema [{:name "Contact"
                                          :properties {:phone-number "string"
                                                       :whisper-identity "string"
                                                       :name "string"
                                                       :photo-path "string"}}]})))

(defn write [f]
  (.write realm f))

(defn get-contacts-objects []
  (.objects realm "Contact"))

(defn get-contacts []
  (vals (js->clj (get-contacts-objects) :keywordize-keys true)))

(defn delete-contacts []
  (write (fn []
           (.delete realm (get-contacts-objects)))))

(defn filtered [objs query]
  (.filtered objs query))

(defn get-count [objs]
  (.-length objs))

(defn create-contact [{:keys [phone-number whisper-identity name photo-path]}]
  (.create realm "Contact"
           (clj->js {:phone-number phone-number
                     :whisper-identity whisper-identity
                     :name (or name "")
                     :photo-path (or photo-path "")})))

(defn contact-exist? [contacts contact]
  (some #(= (:phone-number contact) (:phone-number %)) contacts))

(defn add-contacts [contacts]
  (write (fn []
           (let [db-contacts (get-contacts)]
             (dorun (map (fn [contact]
                           (if (not (contact-exist? db-contacts contact))
                             (create-contact contact)
                             ;; TODO else override?
                             ))
                         contacts))))))
