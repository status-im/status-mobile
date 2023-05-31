(ns status-im2.contexts.chat.photo-selector.events
  (:require [react-native.cameraroll :as cameraroll]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [react-native.permissions :as permissions]
            [status-im2.config :as config]
            [utils.re-frame :as rf]
            [status-im.utils.image-processing :as image-processing]
            [taoensso.timbre :as log]
            [react-native.core :as rn]))

(def maximum-image-size-px 2000)

(defn- resize-photo
  [uri callback]
  (rn/image-get-size
   uri
   (fn [width height]
     (let [resize? (> (max width height) maximum-image-size-px)]
       (image-processing/resize
        uri
        (if resize? maximum-image-size-px width)
        (if resize? maximum-image-size-px height)
        60
        (fn [^js resized-image]
          (let [path (.-path resized-image)
                path (if (string/starts-with? path "file") path (str "file://" path))]
            (callback {:resized-uri path
                       :width       width
                       :height      height})))
        #(log/error "could not resize image" %))))))

(re-frame/reg-fx
 :camera-roll-request-permissions-and-get-photos
 (fn [[num end-cursor album]]
   (permissions/request-permissions
    {:permissions [:read-external-storage]
     :on-allowed
     (fn []
       (cameraroll/get-photos
        {:first      num
         :after      end-cursor
         :assetType  "Photos"
         :groupTypes (if (= album (i18n/label :t/recent)) "All" "Albums")
         :groupName  (when (not= album (i18n/label :t/recent)) album)
         :include    (clj->js ["imageSize"])}
        #(re-frame/dispatch [:on-camera-roll-get-photos (:edges %) (:page_info %) end-cursor])))})))

(re-frame/reg-fx
 :camera-roll-image-selected
 (fn [[image chat-id]]
   (resize-photo (:uri image) #(re-frame/dispatch [:photo-selector/image-selected chat-id image %]))))

(defn get-albums
  [callback]
  (let [albums (atom {:smart-albums []
                      :my-albums    []})]
    ;; Get the "recent" album first
    (cameraroll/get-photos
     {:first 1 :groupTypes "All"}
     (fn [res-recent]
       (swap! albums assoc
         :smart-albums
         [{:title (i18n/label :t/recent)
           :uri   (get-in (first (:edges res-recent)) [:node :image :uri])}])
       ;; Get albums, then loop over albums and get each one's cover (first photo)
       (cameraroll/get-albums
        {:assetType "All"}
        (fn [res-albums]
          (let [response-count (count res-albums)]
            (if (pos? response-count)
              (doseq [album res-albums]
                (cameraroll/get-photos
                 {:first 1 :groupTypes "Albums" :groupName (:title album)}
                 (fn [res]
                   (let [uri (get-in (first (:edges res)) [:node :image :uri])]
                     (swap! albums update :my-albums conj (merge album {:uri uri}))
                     (when (= (count (:my-albums @albums)) response-count)
                       (swap! albums update :my-albums #(sort-by :title %))
                       (callback @albums))))))
              (callback @albums)))))))))

(re-frame/reg-fx
 :camera-roll-get-albums
 (fn []
   (get-albums #(re-frame/dispatch [:on-camera-roll-get-albums %]))))

(rf/defn on-camera-roll-get-albums
  {:events [:on-camera-roll-get-albums]}
  [{:keys [db]} albums]
  {:db (assoc db :camera-roll/albums albums)})

(rf/defn camera-roll-get-albums
  {:events [:photo-selector/camera-roll-get-albums]}
  [_]
  {:camera-roll-get-albums nil})

(rf/defn camera-roll-select-album
  {:events [:chat.ui/camera-roll-select-album]}
  [{:keys [db]} album]
  {:db (assoc db :camera-roll/selected-album album)})

(rf/defn image-selected
  {:events [:photo-selector/image-selected]}
  [{:keys [db]} current-chat-id original {:keys [resized-uri width height]}]
  {:db
   (update-in db
              [:chat/inputs current-chat-id :metadata :sending-image (:uri original)]
              merge
              original
              {:resized-uri resized-uri
               :width       width
               :height      height})})

(rf/defn on-camera-roll-get-photos
  {:events [:on-camera-roll-get-photos]}
  [{:keys [db]} photos page-info end-cursor]
  (let [photos_x (when end-cursor (:camera-roll/photos db))]
    {:db (-> db
             (assoc :camera-roll/photos (concat photos_x (map #(get-in % [:node :image]) photos)))
             (assoc :camera-roll/end-cursor (:end_cursor page-info))
             (assoc :camera-roll/has-next-page (:has_next_page page-info))
             (assoc :camera-roll/loading-more false))}))

(rf/defn get-photos-for-selected-album
  {:events [:photo-selector/get-photos-for-selected-album]}
  [{:keys [db]} end-cursor]
  {:camera-roll-request-permissions-and-get-photos [21 end-cursor
                                                    (or (:camera-roll/selected-album db)
                                                        (i18n/label :t/recent))]})

(rf/defn camera-roll-loading-more
  {:events [:photo-selector/camera-roll-loading-more]}
  [{:keys [db]} is-loading]
  {:db (assoc db :camera-roll/loading-more is-loading)})

(rf/defn camera-roll-pick
  {:events [:photo-selector/camera-roll-pick]}
  [{:keys [db]} image chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))
        images          (get-in db [:chat/inputs current-chat-id :metadata :sending-image])]
    (when (and (< (count images) config/max-images-batch)
               (not (some #(= (:uri image) (:uri %)) images)))
      {:camera-roll-image-selected [image current-chat-id]})))
