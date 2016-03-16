(ns messenger.models.contacts
  (:require-macros [natal-shell.data-source :refer [data-source clone-with-rows]])
  (:require [cljs.core.async :as async :refer [chan put! <! >!]]
            [messenger.state :as state]
            [messenger.utils.utils :refer [log toast http-post]]
            [messenger.utils.database :as db]))

(def fake-contacts? true)

(def react-native-contacts (js/require "react-native-contacts"))

(defn- generate-contact [n]
  {:name (str "Contact " n)
   :photo-path ""
   :phone-numbers [{:label "mobile" :number (apply str (repeat 7 n))}]
   :delivery-status (if (< (rand) 0.5) :delivered :seen)
   :datetime "15:30"
   :new-messages-count (rand-int 3)
   :online (< (rand) 0.5)})

(defn- generate-contacts [n]
  (map generate-contact (range 1 (inc n))))

(defn load-phone-contacts []
  (let [ch (chan)]
    (if fake-contacts?
      (put! ch {:error nil, :contacts (generate-contacts 10)})
      (.getAll react-native-contacts
               (fn [error raw-contacts]
                 (put! ch
                       {:error error
                        :contacts
                        (when (not error)
                          (map (fn [contact]
                                 (merge (generate-contact 1)
                                        {:name (:givenName contact)
                                         :photo-path (:thumbnailPath contact)
                                         :phone-numbers (:phoneNumbers contact)}))
                               (js->clj raw-contacts :keywordize-keys true)))}))))
    ch))

(defn load-syng-contacts []
  (let [contacts (map (fn [contact]
                        (merge contact
                               {:delivery-status (if (< (rand) 0.5) :delivered :seen)
                                :datetime "15:30"
                                :new-messages-count (rand-int 3)
                                :online (< (rand) 0.5)}))
                      (db/get-contacts))]
    (swap! state/app-state update :contacts-ds
           #(clone-with-rows % contacts))))

(defn save-syng-contacts [syng-contacts]
  (db/add-contacts syng-contacts))
