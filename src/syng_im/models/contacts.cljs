(ns syng-im.models.contacts
  (:require [syng-im.persistence.realm :as r]
            [syng-im.persistence.realm-queries :refer [include-query
                                                       exclude-query]]))

(defn get-contacts []
  (-> (r/get-all :contacts)
      (r/sorted :name :asc)
      r/collection->map))

(defn create-contact [{:keys [name photo-path] :as contact}]
  (->> {:name       (or name "")
        :photo-path (or photo-path "")}
       (merge contact)
       (r/create :contacts)))

(defn save-contacts [contacts]
  (r/write #(mapv create-contact contacts)))


;;;;;;;;;;;;;;;;;;;;----------------------------------------------

(defn contacts-list []
  (r/sorted (r/get-all :contacts) :name :asc))

(defn contacts-list-exclude [exclude-idents]
  (let [query (exclude-query :whisper-identity exclude-idents)]
    (-> (r/get-all :contacts)
        (r/filtered query)
        (r/sorted :name :asc))))

(defn contacts-list-include [include-indents]
  (let [query (include-query :whisper-identity include-indents)]
    (-> (r/get-all :contacts)
        (r/filtered query)
        (r/sorted :name :asc))))

(defn contact-by-identity [identity]
  (r/single-cljs (r/get-by-field :contacts :whisper-identity identity)))
