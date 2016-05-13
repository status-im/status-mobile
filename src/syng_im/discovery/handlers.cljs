(ns syng-im.discovery.handlers
  (:require [re-frame.core :refer [register-handler after dispatch enrich
                                   log-ex debug]]
            [syng-im.protocol.api :as api]
            [syng-im.models.discoveries :refer [save-discoveries
                                                set-current-tag]]
            [syng-im.navigation.handlers :as nav]
            [syng-im.models.discoveries :as discoveries]
            [syng-im.utils.handlers :as u]))

(defmethod nav/preload-data! :discovery
  [{:keys [] :as db} _]
  (-> db
      (assoc :tags (discoveries/all-tags))
      (assoc :discoveries (discoveries/discovery-list))))

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
  (fn [{:keys [name] :as db} [_ status hashtags]]
    (api/broadcast-discover-status name status hashtags)
    db))

(register-handler :show-discovery-tag
  (fn [db [_ tag]]
    (let [db (set-current-tag db tag)]
      (dispatch [:navigate-to :discovery-tag])
      db)))

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
      debug
      ((after save-discovery!))
      ((enrich reload-tags!))))
