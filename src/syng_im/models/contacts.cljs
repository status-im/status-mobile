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

(defn contact-by-identity [identity]
  (-> (r/get-by-field :contacts :whisper-identity identity)
      (r/single-cljs)))

(comment

  (r/write #(create-contact {:phone-number     "0543072333"
                             :whisper-identity "0x043e3a8344049fb48fef030084212a9d41577a5dea18aeb4c8f285c16f783aa84e43f84c32eb8601e22827b12d5f93f14e545f9023034a0521dc18484bbbc44704"
                             :name             "Mr. Bean"
                             :photo-path       ""}))

  (r/write #(create-contact {:phone-number     "0544828649"
                             :whisper-identity "0x04e9b01298dd12c4d8f0393d7890302b25762966d825158d1fdffe124703c0efcd7f23a6cf71c466ca50b2af3d54264ea5f224a19ba7775779c1ddbcb237258c5c"
                             :name             "Mr. Batman"
                             :photo-path       ""}))

  (r/write #(create-contact {:phone-number     "0522222222"
                             :whisper-identity "0x0487954e7fa746d8cf787403c2c491aadad540b9bb1f0f7b8184792e91c33b6a394079295f5777ec6d4af9ad5ba24794b3ff1ec8be9ff6a708c85a163733192665"
                             :name             "Mr. Eagle"
                             :photo-path       ""}))

  (r/write #(create-contact {:phone-number     "0533333333"
                             :whisper-identity "0x04e43e861a6dd99ad9eee7bd58af89dcaa430188ebec8698de7b7bad54573324fff4ac5cb9bb277af317efd7abfc917b91bf48cc41e40bf70062fd79400016a1f9"
                             :name             "Mr. PiggyBear"
                             :photo-path       ""}))

  (contacts-list)

  (:new-group @re-frame.db/app-db)

  )