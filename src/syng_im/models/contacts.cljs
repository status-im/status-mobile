(ns syng-im.models.contacts
  (:require [cljs.core.async :as async :refer [chan put! <! >!]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.utils.utils :refer [log toast]]
            [syng-im.persistence.realm :as realm]
            [syng-im.persistence.realm :as r]
            [clojure.string :as s]))

;; TODO see https://github.com/rt2zz/react-native-contacts/issues/45
(def fake-phone-contacts? true)
(def fake-contacts? false)

(def react-native-contacts (js/require "react-native-contacts"))

(defn- generate-contact [n]
  {:name               (str "Contact " n)
   :photo-path         ""
   :phone-numbers      [{:label "mobile" :number (apply str (repeat 7 n))}]
   :delivery-status    (if (< (rand) 0.5) :delivered :seen)
   :datetime           "15:30"
   :new-messages-count (rand-int 3)
   :online             (< (rand) 0.5)})

(defn- generate-contacts [n]
  (map generate-contact (range 1 (inc n))))

(defn load-phone-contacts []
  (let [ch (chan)]
    (if fake-phone-contacts?
      (put! ch {:error nil, :contacts (generate-contacts 10)})
      (.getAll react-native-contacts
               (fn [error raw-contacts]
                 (put! ch
                       {:error error
                        :contacts
                               (when (not error)
                                 (log raw-contacts)
                                 (map (fn [contact]
                                        (merge contact
                                               (generate-contact 1)
                                               {:name          (:givenName contact)
                                                :photo-path    (:thumbnailPath contact)
                                                :phone-numbers (:phoneNumbers contact)}))
                                      (js->clj raw-contacts :keywordize-keys true)))}))))
    ch))

(defn- get-contacts []
  (if fake-contacts?
    [{:phone-number     "123"
      :whisper-identity "abc"
      :name             "fake"
      :photo-path       ""}]
    (realm/get-list :contacts)))

(defn load-syng-contacts [db]
  (let [contacts (map (fn [contact]
                        (merge contact
                               {:delivery-status    (if (< (rand) 0.5) :delivered :seen)
                                :datetime           "15:30"
                                :new-messages-count (rand-int 3)
                                :online             (< (rand) 0.5)}))
                      (get-contacts))]
    (assoc db :contacts contacts)))

(defn- create-contact [{:keys [phone-number whisper-identity name photo-path]}]
  (realm/create :contacts
                {:phone-number     phone-number
                 :whisper-identity whisper-identity
                 :name             (or name "")
                 :photo-path       (or photo-path "")}))

(defn- contact-exist? [contacts contact]
  (some #(= (:phone-number contact) (:phone-number %)) contacts))

(defn- add-contacts [contacts]
  (realm/write (fn []
                 (let [db-contacts (get-contacts)]
                   (dorun (map (fn [contact]
                                 (if (not (contact-exist? db-contacts contact))
                                   (create-contact contact)
                                   ;; TODO else override?
                                   ))
                               contacts))))))

(defn save-syng-contacts [syng-contacts]
  (add-contacts syng-contacts))


;;;;;;;;;;;;;;;;;;;;----------------------------------------------

(defn contacts-list []
  (-> (r/get-all :contacts)
      (r/sorted :name :asc)))

(defn contacts-list-exclude [exclude-idents]
  (let [exclude-query (->> exclude-idents
                           (map (fn [ident]
                                  (str "whisper-identity != '" ident "'")))
                           (s/join " && "))]
    (-> (r/get-all :contacts)
        (r/filtered exclude-query)
        (r/sorted :name :asc))))

(defn contatct-by-identity [identity]
  (-> (r/get-by-field :contacts :whisper-identity identity)
      (r/single-cljs)))

(comment

  (r/write #(create-contact {:phone-number     "0543072333"
                             :whisper-identity "0x045326d94f999c3e17d11d6a09e13cf9a34b30f62687a970053887b8f8d1fefac3f185e6ba6f02f8217b6947227c7dbfa8d4ee3a4a2864b0cac9968c819ba9c17c"
                             :name             "Mr. Bean"
                             :photo-path       ""}))

  (r/write #(create-contact {:phone-number     "0544828649"
                             :whisper-identity "0x0496152b9bb6640fddb3a156747baa961792762264ab459c8d7c9ac179ddceb2abb701357a9b6691ef858ef8ce2eb306754825b8267add96e7a01d854a576b9fda"
                             :name             "Mr. Batman"
                             :photo-path       ""}))

  (r/write #(create-contact {:phone-number     "0522222222"
                             :whisper-identity "0x04ef1f45303a14cb30af0458fabc2c7b1fd1bc9bb69fae5bb4ec63ae5b52cd53c2a194398b6674cc335899bbf6a24bfe48813a134ce9b8b85877690c1a99f0b19f"
                             :name             "Mr. Eagle"
                             :photo-path       ""}))

  (r/write #(create-contact {:phone-number     "0533333333"
                             :whisper-identity "0x04e9e31a92ab16dbac9cdce47f59545ac646175087f4eeb6e4442e36d37b09e20dcb6239ac743ddd3f1fa17377c1789dc60a1ebb8774d247f3df69ebd0082d46fe"
                             :name             "Mr. PiggyBear"
                             :photo-path       ""}))

  (contacts-list)

  (:new-group @re-frame.db/app-db)

  )