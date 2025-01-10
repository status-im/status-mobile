(ns status-im.contexts.chat.messenger.photo-selector.events
  (:require
    [re-frame.core :as re-frame]
    [status-im.constants :as constants]
    status-im.contexts.chat.messenger.photo-selector.effects
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/defn on-camera-roll-get-albums
  {:events [:on-camera-roll-get-albums]}
  [{:keys [db]} albums]
  {:db       (assoc db :camera-roll/albums albums)
   :dispatch [:photo-selector/camera-roll-get-ios-photo-count]})

(rf/defn get-photos-count-ios
  {:events [:on-camera-roll-get-images-count-ios]}
  [{:keys [db]} cnt]
  {:db (assoc db :camera-roll/ios-images-count cnt)})

(rf/defn camera-roll-get-albums
  {:events [:photo-selector/camera-roll-get-albums]}
  [_]
  {:effects.camera-roll/get-albums nil})

(rf/defn camera-roll-get-ios-photo-count
  {:events [:photo-selector/camera-roll-get-ios-photo-count]}
  [_]
  {:effects.camera-roll/get-photos-count-ios nil})

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
  {:effects.camera-roll/request-permissions-and-get-photos
   [21 end-cursor
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
    (when (and (< (count images) constants/max-album-photos)
               (not (some #(= (:uri image) (:uri %)) images)))
      {:effects.camera-roll/image-selected [image current-chat-id]})))

(re-frame/reg-event-fx :photo-selector/navigate-to-photo-selector
 (fn []
   {:fx [[:dispatch [:open-modal :photo-selector]]
         [:dispatch [:photo-selector/get-photos-for-selected-album]]
         [:dispatch [:photo-selector/camera-roll-get-albums]]]}))
