(ns status-im.chat.models.images
  (:require ["@react-native-community/cameraroll" :as CameraRoll]
            ["react-native-blob-util" :default ReactNativeBlobUtil]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [status-im.ui.components.permissions :as permissions]
            [status-im.ui.components.react :as react]
            [status-im2.config :as config]
            [status-im.utils.fs :as fs]
            [utils.re-frame :as rf]
            [status-im.utils.image-processing :as image-processing]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

(def maximum-image-size-px 2000)

(defn- resize-and-call
  [uri cb]
  (react/image-get-size
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
            (cb path)))
        #(log/error "could not resize image" %))))))

(defn result->id
  [^js result]
  (if platform/ios?
    (.-localIdentifier result)
    (.-path result)))

(def temp-image-url (str (fs/cache-dir) "/StatusIm_Image.jpeg"))

(defn download-image-http
  [base64-uri on-success]
  (-> (.config ReactNativeBlobUtil
               (clj->js {:trusty platform/ios?
                         :path   temp-image-url}))
      (.fetch "GET" base64-uri)
      (.then #(on-success (.path %)))
      (.catch #(log/error "could not save image"))))

(defn save-to-gallery [path] (.save CameraRoll path))

(re-frame/reg-fx
 ::save-image-to-gallery
 (fn [base64-uri]
   (if platform/ios?
     (-> (download-image-http base64-uri save-to-gallery)
         (.catch #(utils/show-popup (i18n/label :t/error)
                                    (i18n/label :t/external-storage-denied))))
     (permissions/request-permissions
      {:permissions [:write-external-storage]
       :on-allowed  #(download-image-http base64-uri save-to-gallery)
       :on-denied   (fn []
                      (utils/set-timeout
                       #(utils/show-popup (i18n/label :t/error)
                                          (i18n/label :t/external-storage-denied))
                       50))}))))

(re-frame/reg-fx
 ::chat-open-image-picker-camera
 (fn [current-chat-id]
   (react/show-image-picker-camera
    #(re-frame/dispatch [:chat.ui/image-captured current-chat-id (.-path %)])
    {})))

(re-frame/reg-fx
 ::chat-open-image-picker
 (fn [chat-id]
   (react/show-image-picker
    (fn [^js images]
      ;; NOTE(Ferossgp): Because we can't highlight the already selected images inside
      ;; gallery, we just clean previous state and set all newly picked images
      (when (and platform/ios? (pos? (count images)))
        (re-frame/dispatch [:chat.ui/clear-sending-images chat-id]))
      (doseq [^js result (if platform/ios?
                           (take config/max-images-batch images)
                           [images])]
        (resize-and-call (.-path result)
                         #(re-frame/dispatch [:chat.ui/image-selected chat-id (result->id result) %]))))
    ;; NOTE(Ferossgp): On android you cannot set max limit on images, when a user
    ;; selects too many images the app crashes.
    {:media-type "photo"
     :multiple   platform/ios?})))

(re-frame/reg-fx
 ::image-selected
 (fn [[image chat-id]]
   (resize-and-call
    (:uri image)
    #(re-frame/dispatch [:chat.ui/image-selected chat-id image %]))))

(re-frame/reg-fx
 ::camera-roll-get-photos
 (fn [[num end-cursor album]]
   (permissions/request-permissions
    {:permissions [:read-external-storage]
     :on-allowed  (fn []
                    (-> (if end-cursor
                            (.getPhotos
                             CameraRoll
                             #js
                              {:first      num
                               :after      end-cursor
                               :assetType  "Photos"
                               :groupTypes (if (= album (i18n/label :t/recent)) "All" "Albums")
                               :groupName  (when (not= album (i18n/label :t/recent)) album)
                               :include    (clj->js ["imageSize"])})
                            (.getPhotos
                             CameraRoll
                             #js
                              {:first      num
                               :assetType  "Photos"
                               :groupTypes (if (= album (i18n/label :t/recent)) "All" "Albums")
                               :groupName  (when (not= album (i18n/label :t/recent)) album)
                               :include    (clj->js ["imageSize"])}))
                        (.then #(let [response (types/js->clj %)]
                                  (re-frame/dispatch [:on-camera-roll-get-photos (:edges response)
                                                      (:page_info response) end-cursor])))
                        (.catch #(log/warn "could not get camera roll photos"))))})))

(re-frame/reg-fx
 :chat.ui/camera-roll-get-albums
 (fn []
   (let [albums (atom [{:title :smart-albums :data []}
                       {:title (i18n/label :t/my-albums) :data []}])]
     ;; Get the "recent" album first
     (->
       (.getPhotos CameraRoll
                   #js
                    {:first      1
                     :groupTypes "All"})
       (.then
        (fn [res]
          (let [recent-album {:title (i18n/label :t/recent)
                              :count "--"
                              :uri   (get-in (first (:edges (types/js->clj res))) [:node :image :uri])}]
            (swap! albums update-in [0 :data] conj recent-album)
            ;; Get albums, then loop over albums and get each one's cover (first photo)
            (->
              (.getAlbums CameraRoll #js {:assetType "All"})
              (.then
               (fn [response]
                 (let [response (types/js->clj response)]
                   (reduce
                    (fn [_ album]
                      (->
                        (.getPhotos
                         CameraRoll
                         #js
                          {:first      1
                           :groupTypes "Albums"
                           :groupName  (:title album)})
                        (.then (fn [res]
                                 (let [uri (get-in (first (:edges (types/js->clj res)))
                                                   [:node :image :uri])]
                                   (swap! albums update-in [1 :data] conj (merge album {:uri uri}))
                                   (when (= (count (get-in @albums [1 :data])) (count response))
                                     (swap! albums update-in
                                       [1 :data]
                                       #(->> %
                                             (sort-by :title)))
                                     (re-frame/dispatch [:on-camera-roll-get-albums @albums])))))))
                    nil
                    response))))
              (.catch #(log/warn "could not get camera roll albums"))))))))))


(rf/defn image-captured
  {:events [:chat.ui/image-captured]}
  [{:keys [db]} chat-id uri]
  (let [current-chat-id (or chat-id (:current-chat-id db))
        images          (get-in db [:chat/inputs current-chat-id :metadata :sending-image])]
    (when (and (< (count images) config/max-images-batch)
               (not (get images uri)))
      {::image-selected [uri current-chat-id]})))

(rf/defn on-end-reached
  {:events [:camera-roll/on-end-reached]}
  [_ end-cursor selected-album loading? has-next-page?]
  (when (and (not loading?) has-next-page?)
    (re-frame/dispatch [:chat.ui/camera-roll-loading-more true])
    (re-frame/dispatch [:chat.ui/camera-roll-get-photos 20 end-cursor selected-album])))

(rf/defn camera-roll-get-photos
  {:events [:chat.ui/camera-roll-get-photos]}
  [_ num end-cursor selected-album]
  {::camera-roll-get-photos [num end-cursor selected-album]})

(rf/defn camera-roll-get-albums
  {:events [:chat.ui/camera-roll-get-albums]}
  [_]
  {:chat.ui/camera-roll-get-albums []})

(rf/defn camera-roll-loading-more
  {:events [:chat.ui/camera-roll-loading-more]}
  [{:keys [db]} is-loading]
  {:db (assoc db :camera-roll/loading-more is-loading)})

(rf/defn on-camera-roll-get-photos
  {:events [:on-camera-roll-get-photos]}
  [{:keys [db] :as cofx} photos page-info end-cursor]
  (let [photos_x (when end-cursor (:camera-roll/photos db))]
    {:db (-> db
             (assoc :camera-roll/photos (concat photos_x (map #(get-in % [:node :image]) photos)))
             (assoc :camera-roll/end-cursor (:end_cursor page-info))
             (assoc :camera-roll/has-next-page (:has_next_page page-info))
             (assoc :camera-roll/loading-more false))}))

(rf/defn on-camera-roll-get-albums
  {:events [:on-camera-roll-get-albums]}
  [{:keys [db]} albums]
  {:db (-> db
           (assoc :camera-roll/albums albums)
           (assoc :camera-roll/loading-albums false))})

(rf/defn clear-sending-images
  {:events [:chat.ui/clear-sending-images]}
  [{:keys [db]}]
  {:db (update-in db [:chat/inputs (:current-chat-id db) :metadata] assoc :sending-image {})})

(rf/defn cancel-sending-image
  {:events [:chat.ui/cancel-sending-image]}
  [{:keys [db] :as cofx} chat-id]
  (clear-sending-images cofx))

(rf/defn image-selected
  {:events [:chat.ui/image-selected]}
  [{:keys [db]} current-chat-id original uri]
  {:db
   (update-in db
              [:chat/inputs current-chat-id :metadata :sending-image (:uri original)]
              merge
              original
              {:resized-uri uri})})

(rf/defn image-unselected
  {:events [:chat.ui/image-unselected]}
  [{:keys [db]} original]
  (let [current-chat-id (:current-chat-id db)]
    {:db (update-in db [:chat/inputs current-chat-id :metadata :sending-image] dissoc (:uri original))}))

(rf/defn chat-open-image-picker
  {:events [:chat.ui/open-image-picker]}
  [{:keys [db]} chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))
        images          (get-in db [:chat/inputs current-chat-id :metadata :sending-image])]
    (when (< (count images) config/max-images-batch)
      {::chat-open-image-picker current-chat-id})))

(rf/defn chat-show-image-picker-camera
  {:events [:chat.ui/show-image-picker-camera]}
  [{:keys [db]} chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))
        images          (get-in db [:chat/inputs current-chat-id :metadata :sending-image])]
    (when (< (count images) config/max-images-batch)
      {::chat-open-image-picker-camera current-chat-id})))

(rf/defn camera-roll-pick
  {:events [:chat.ui/camera-roll-pick]}
  [{:keys [db]} image chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))
        images          (get-in db [:chat/inputs current-chat-id :metadata :sending-image])]
    (if (get-in db [:chats current-chat-id :timeline?])
      {:db              (assoc-in db [:chat/inputs current-chat-id :metadata :sending-image] {})
       ::image-selected [image current-chat-id]}
      (when (and (< (count images) config/max-images-batch)
                 (not (some #(= (:uri image) (:uri %)) images)))
        {::image-selected [image current-chat-id]}))))

(rf/defn camera-roll-select-album
  {:events [:chat.ui/camera-roll-select-album]}
  [{:keys [db]} album]
  {:db (assoc db :camera-roll/selected-album album)})

(rf/defn save-image-to-gallery
  {:events [:chat.ui/save-image-to-gallery]}
  [_ base64-uri]
  {::save-image-to-gallery base64-uri})
