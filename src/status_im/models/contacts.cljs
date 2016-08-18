(ns status-im.models.contacts
  (:require [status-im.persistence.realm.core :as r]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.persistence.realm-queries :refer [include-query
                                                       exclude-query]]))

(defn get-contacts []
  (-> (r/get-all :account :contact)
      (r/sorted :name :asc)
      r/collection->map))

(defn create-contact [{:keys [whisper-identity] :as contact}]
  (let [contact-from-db (r/get-one-by-field :account :contact
                                            :whisper-identity whisper-identity)]
    (r/create :account :contact contact (if contact-from-db true false))))

(defn save-contacts [contacts]
  (r/write :account #(mapv create-contact contacts)))


;;;;;;;;;;;;;;;;;;;;----------------------------------------------

(defn contacts-list []
  (r/sorted (r/get-all :account :contact) :name :asc))

(defn contacts-list-exclude [exclude-idents]
  (if (empty? exclude-idents)
    (contacts-list)
    (let [query (exclude-query :whisper-identity exclude-idents)]
      (-> (r/get-all :account :contact)
          (r/filtered query)
          (r/sorted :name :asc)))))

(defn contacts-list-include [include-indents]
  (if (empty? include-indents)
    ()
    (let [query (include-query :whisper-identity include-indents)]
      (-> (r/get-all :account :contact)
          (r/filtered query)
          (r/sorted :name :asc)))))

(defn contact-by-identity [identity]
  (r/single-cljs (r/get-by-field :account :contact :whisper-identity identity)))
