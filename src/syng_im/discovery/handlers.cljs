(ns syng-im.discovery.handlers
  (:require [re-frame.core :refer [register-handler after dispatch enrich]]
            [syng-im.protocol.api :as api]
            [syng-im.navigation.handlers :as nav]
            [syng-im.discovery.model :as discoveries]
            [syng-im.utils.handlers :as u]))

(defmethod nav/preload-data! :discovery
  [{:keys [discoveries] :as db} _]
  (if-not (seq discoveries)
    (-> db
        (assoc :tags (discoveries/all-tags))
        ;; todo add limit
        ;; todo hash-map with whisper-id as key and sorted by last-update
        ;; may be more efficient here
        (assoc :discoveries (discoveries/discovery-list)))
    db))

(register-handler :discovery-response-received
  (u/side-effect!
    (fn [_ [_ from payload]]
      (let [{:keys [name status hashtags location]} payload
            location  (or location "")
            discovery [{:name         name
                        :status       status
                        :whisper-id   from
                        :photo        ""
                        :location     location
                        :tags         (map #(hash-map :name %) hashtags)
                        :last-updated (js/Date.)}]]
        (dispatch [:add-discovery discovery])))))

(register-handler :broadcast-status
  (u/side-effect!
    (fn [{:keys [name]} [_ status hashtags]]
      (api/broadcast-discover-status name status hashtags))))

(register-handler :show-discovery-tag
  (fn [db [_ tag]]
    (dispatch [:navigate-to :discovery-tag])
    (assoc db :current-tag tag)))

;; todo remove this
(register-handler :create-fake-discovery!
  (u/side-effect!
    (fn [_ _]
      (let [number    (rand-int 999)
            discovery {:name         (str "Name " number)
                       :status       (str "Status This is some longer status to get the second line " number)
                       :whisper-id   (str number)
                       :photo        ""
                       :location     ""
                       :tags         [{:name "tag1"}
                                      {:name "tag2"}
                                      {:name "tag3"}]
                       :last-updated (new js/Date)}]
        (dispatch [:add-discovery discovery])))))

(defn add-discovery
  [db [_ discovery]]
  (-> db
      (assoc :new-discovery discovery)
      (update :discoveries conj discovery)))

(defn save-discovery!
  [{:keys [new-discovery]} _]
  (discoveries/save-discoveries [new-discovery]))

(defn reload-tags!
  [db _]
  (assoc db :tags (discoveries/all-tags)))

(register-handler :add-discovery
  (-> add-discovery
      ((after save-discovery!))
      ((enrich reload-tags!))))
