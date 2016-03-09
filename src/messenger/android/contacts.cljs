(ns messenger.android.contacts
  (:require [messenger.state :as state]
            [messenger.android.utils :refer [log toast http-post]]))

(def fake-contacts? false)

(def react-native-contacts (js/require "react-native-contacts"))

(defn generate-contact [n]
  {:name (str "Contact " n)
   :photo ""
   :phone-numbers [{:label "mobile" :number "121212"}]
   :delivery-status (if (< (rand) 0.5) :delivered :seen)
   :datetime "15:30"
   :new-messages-count (rand-int 3)
   :online (< (rand) 0.5)})

(defn generate-contacts [n]
  (map generate-contact (range 1 (inc n))))

(defn load-contacts [on-success on-error]
  (if fake-contacts?
    (on-success (generate-contacts 100))
    (.getAll react-native-contacts
             (fn [error raw-contacts]
               (if (not error)
                 (let [contacts (map (fn [contact]
                                       (merge (generate-contact 1)
                                              {:name (:givenName contact)
                                               :photo (:thumbnailPath contact)
                                               :phone-numbers (:phoneNumbers contact)}))
                                     (js->clj raw-contacts :keywordize-keys true))]
                   (on-success contacts))
                 (when on-error
                   (on-error error)))))))
