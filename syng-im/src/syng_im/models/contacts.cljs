(ns syng-im.models.contacts
  (:require [cljs.core.async :as async :refer [chan put! <! >!]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.utils.utils :refer [log toast]]
            [syng-im.persistence.realm :as realm]
            [syng-im.persistence.realm :as r]))

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

(comment

  (r/write #(create-contact {:phone-number     "0543072333"
                             :whisper-identity "0x0479a5ed1f38cadfad1db6cd56c4b659b0ebe052bbe9efa950f6660058519fa4ca6be2dda66afa80de96ab00eb97a2605d5267a1e8f4c2a166ab551f6826608cdd"
                             :name             "Mr. Bean"
                             :photo-path       ""}))

  (r/write #(create-contact {:phone-number     "0544828649"
                             :whisper-identity "0x042fc4ef6525be53a6358da6db9d9019184e0fd04b5bd67a6faa252c3d0f4efdf57bdafc2c03b39daa2d0deabe49bb727c272c59ade8857f93a6ef27be77517e9e"
                             :name             "Mr. Batman"
                             :photo-path       ""}))

  (contacts-list)

  (:new-group @re-frame.db/app-db)

  )