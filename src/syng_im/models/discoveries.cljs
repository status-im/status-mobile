(ns syng-im.models.discoveries
  (:require [cljs.core.async :as async :refer [chan put! <! >!]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.utils.debug :refer [log]]
            [syng-im.persistence.realm :as realm]
            [syng-im.persistence.realm :as r]
            [syng-im.resources :as res]
            [syng-im.db :as db]))

;; TODO see https://github.com/rt2zz/react-native-contacts/issues/45
(def fake-discoveries? true)

(defn signal-discovery-updated [db]
  (update-in db db/updated-discoveries-signal-path (fn [current]
                                               (if current
                                                 (inc current)
                                                 0))))

(defn discovery-updated? [db]
  (get-in db db/updated-discoveries-signal-path))

(defn generate-discovery [n]
  {:name         (str "Contact " n)
   :status       (apply str (repeat (+ n 3) "Contact Status "))
   :whisper-id   (str "id-" n)
   :location     ""
   :photo        ""
   :tags         ["tag1" "tag2"]
   :last-updated (js/Date. "10/01/2015")
   })

(defn- generate-discoveries [n]
  (map generate-discovery (range 1 (inc n))))

(def fake-discoveries (generate-discoveries 20))

(defn- get-discoveries []
  (let [list (realm/get-list :discoveries)
        _ (log list)]
    (if (> (.-length list) 0) (.slice list 0) [])))

(defn load-syng-discoveries [db]
  (let [discoveries (map (fn [discovery]
                        (merge discovery
                               {}))
                      (get-discoveries))]
    (assoc db :discoveries discoveries)))

(defn get-tag [tag]
  (-> (r/get-by-field :tag :name tag)
      (r/single-cljs)))

(defn remove-tag [tag]
  (let [tag-object (get-tag tag)]
    (if tag-object
      (realm/create :tag
                    {:name tag
                     :count (dec (:count tag-object))
                     }))))

(defn add-tag [tag]
  (let [tag-object (get-tag tag)
        counter (if tag-object (:count tag-object) 1)]
    (realm/create :tag
                  {:name tag
                   :count (inc counter)
                   }
                  (if tag-object true false))))

(defn remove-tags [tags]
  (doseq [tag tags]
    (remove-tag tag)))

(defn add-tags [tags]
  (doseq [tag tags]
    (add-tag tag)))

(defn get-tags [whisper-id]
  (:tags (-> (r/get-by-field :discoveries :whisper-id whisper-id)
      (r/single-cljs))))

(defn- create-discovery [{:keys [name status whisper-id photo location tags last-updated]}]
  (do
    ;(add-tags tags)
    (realm/create :discoveries
                  {:name         name
                   :status       status
                   :whisper-id   whisper-id
                   :photo        photo
                   :location     location
                   :tags         (mapv (fn [tag]
                                        {:name tag}) tags)
                   :last-updated last-updated} true)
    )
  )



(defn- update-discovery [{:keys [name status whisper-id photo location tags last-updated]}]
  (let [old-tags (get-tags whisper-id)]
    (do
      ;;(remove-tags old-tags)
      ;;(add-tags tags)
      (realm/create :discoveries
                  {:name         name
                   :status       status
                   :whisper-id   whisper-id
                   :photo        photo
                   :location     location
                   :tags         (mapv (fn [tag]
                                         {:name tag}) tags)
                   :last-updated last-updated}
                  true)
    ))
  )

(defn- discovery-exist? [discoveries discovery]
  (some #(= (:whisper-id discovery) (:whisper-id %)) discoveries))

(defn- add-discoveries [discoveries]
  (realm/write (fn []
                 (let [db-discoveries (.slice (discovery-list) 0)
                       _ (log discoveries)
                       _ (log (.slice db-discoveries 0))]
                   (dorun (map (fn [discovery]
                                 (if (not (discovery-exist? db-discoveries discovery))
                                   (create-discovery discovery)
                                   (update-discovery discovery)
                                   ))
                               discoveries))))))

(defn save-discoveries [discoveries]
  (add-discoveries discoveries))

(defn discovery-list []
  (-> (r/get-all :discoveries)
      (r/sorted :last-updated :desc)))

(defn discoveries-by-tag [tag limit]
  (let [_ (log (str "discoveries by tag: " tag))]
  (-> (r/get-by-filter :discoveries (str "tags.name = '" tag "'"))
      (r/sorted :last-updated :desc)
      (r/page 0 limit))))

(defn get-discovery-recent [discoveries limit]
  (if fake-discoveries?
    (take limit fake-discoveries)
    (-> (r/sorted discoveries :last-updated :desc)
        (r/page 0 limit)))
  )

(defn get-tag-popular [limit]
  (-> (r/get-all :tag)
      (r/sorted :count :desc)
      (r/page 0 limit)))

(defn add-with-limit [data value limit]
  (if (>= (count data) limit)
    data
    (conj data value))
  )

(defn group-by-tag [discoveries tag-limit]
    (reduce (fn [result discovery]
              (let [keys (:tags discovery)]
                (reduce (fn [data key]
                          (assoc data (keyword key) (add-with-limit (get data (keyword key) []) discovery tag-limit)))
                        result
                        keys)))
            {}
            discoveries)
    )


(comment
  (group-by-tag [{:tags ["a" "b" "c"]
                  :name "test1"}
                 {:tags ["a" "c"]
                  :name "test2"}
                 {:tags ["c"]
                  :name "test3"}])
  )