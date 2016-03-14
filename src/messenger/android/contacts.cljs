(ns messenger.android.contacts
  (:require [messenger.state :as state]
            [messenger.android.utils :refer [log toast http-post]]
            [messenger.android.database :as db]))

(def fake-contacts? true)

(def react-native-contacts (js/require "react-native-contacts"))

(defn generate-contact [n]
  {:name (str "Contact " n)
   :photo-path ""
   :phone-numbers [{:label "mobile" :number (apply str (repeat 7 n))}]
   :delivery-status (if (< (rand) 0.5) :delivered :seen)
   :datetime "15:30"
   :new-messages-count (rand-int 3)
   :online (< (rand) 0.5)})

(defn generate-contacts [n]
  (map generate-contact (range 1 (inc n))))

(defn load-phone-contacts [on-success on-error]
  (if fake-contacts?
    (on-success (generate-contacts 10))
    (.getAll react-native-contacts
             (fn [error raw-contacts]
               (if (not error)
                 (let [contacts (map (fn [contact]
                                       (merge (generate-contact 1)
                                              {:name (:givenName contact)
                                               :photo-path (:thumbnailPath contact)
                                               :phone-numbers (:phoneNumbers contact)}))
                                     (js->clj raw-contacts :keywordize-keys true))]
                   (on-success contacts))
                 (when on-error
                   (on-error error)))))))

(defn load-whisper-contacts []
  (map (fn [contact]
         (merge contact
                {:delivery-status (if (< (rand) 0.5) :delivered :seen)
                 :datetime "15:30"
                 :new-messages-count (rand-int 3)
                 :online (< (rand) 0.5)}))
       (db/get-contacts)))
