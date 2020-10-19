(ns status-im.chat.models.images
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            ["@react-native-community/cameraroll" :as CameraRoll]
            [status-im.utils.types :as types]
            [status-im.ui.components.permissions :as permissions]
            [status-im.ui.components.react :as react]
            [status-im.utils.image-processing :as image-processing]
            [taoensso.timbre :as log]
            [clojure.string :as string]
            [status-im.utils.platform :as platform]))

(def maximum-image-size-px 2000)
(def max-images-batch 5)

(defn- resize-and-call [uri cb]
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

(defn result->id [^js result]
  (if platform/ios?
    (.-localIdentifier result)
    (.-path result)))

(re-frame/reg-fx
 ::save-image-to-gallery
 (fn [base64-uri]
   (if platform/ios?
     (.saveToCameraRoll CameraRoll base64-uri)
     (react/image-get-size
      base64-uri
      (fn [width height]
        (image-processing/resize
         base64-uri
         width
         height
         100
         (fn [^js resized-image]
           (let [path (.-path resized-image)
                 path (if (string/starts-with? path "file") path (str "file://" path))]
             (.saveToCameraRoll CameraRoll path)))
         #(log/error "could not resize image" %)))))))

(re-frame/reg-fx
 ::chat-open-image-picker
 (fn []
   (react/show-image-picker
    (fn [^js images]
      ;; NOTE(Ferossgp): Because we can't highlight the already selected images inside
      ;; gallery, we just clean previous state and set all newly picked images
      (when (and platform/ios? (pos? (count images)))
        (re-frame/dispatch [:chat.ui/clear-sending-images]))
      (doseq [^js result (if platform/ios?
                           (take max-images-batch images)
                           [images])]
        (resize-and-call (.-path result)
                         #(re-frame/dispatch [:chat.ui/image-selected (result->id result) %]))))
    ;; NOTE(Ferossgp): On android you cannot set max limit on images, when a user
    ;; selects too many images the app crashes.
    {:media-type "photo"
     :multiple   platform/ios?})))

(re-frame/reg-fx
 ::image-selected
 (fn [uri]
   (resize-and-call
    uri
    #(re-frame/dispatch [:chat.ui/image-selected uri %]))))

(re-frame/reg-fx
 ::camera-roll-get-photos
 (fn [num]
   (permissions/request-permissions
    {:permissions [:read-external-storage]
     :on-allowed  (fn []
                    (-> (.getPhotos CameraRoll #js {:first num :assetType "Photos" :groupTypes "All"})
                        (.then #(re-frame/dispatch [:on-camera-roll-get-photos (:edges (types/js->clj %))]))
                        (.catch #(log/warn "could not get cameraroll photos"))))})))

(fx/defn image-captured
  {:events [:chat.ui/image-captured]}
  [_ uri]
  {::image-selected uri})

(fx/defn camera-roll-get-photos
  {:events [:chat.ui/camera-roll-get-photos]}
  [_ num]
  {::camera-roll-get-photos num})

(fx/defn on-camera-roll-get-photos
  {:events [:on-camera-roll-get-photos]}
  [{db :db} photos]
  {:db (assoc db :camera-roll-photos (mapv #(get-in % [:node :image :uri]) photos))})

(fx/defn clear-sending-images
  {:events [:chat.ui/clear-sending-images]}
  [{:keys [db]}]
  (let [current-chat-id (:current-chat-id db)]
    {:db (update-in db [:chats current-chat-id :metadata] assoc :sending-image {})}))

(fx/defn cancel-sending-image
  {:events [:chat.ui/cancel-sending-image]}
  [cofx]
  (clear-sending-images cofx))

(fx/defn image-selected
  {:events [:chat.ui/image-selected]}
  [{:keys [db]} original uri]
  (let [current-chat-id (:current-chat-id db)]
    {:db (update-in db [:chats current-chat-id :metadata :sending-image original] merge {:uri uri})}))

(fx/defn image-unselected
  {:events [:chat.ui/image-unselected]}
  [{:keys [db]} original]
  (let [current-chat-id (:current-chat-id db)]
    {:db (update-in db [:chats current-chat-id :metadata :sending-image] dissoc original)}))

(fx/defn chat-open-image-picker
  {:events [:chat.ui/open-image-picker]}
  [{:keys [db]}]
  (let [current-chat-id (:current-chat-id db)
        images          (get-in db [:chats current-chat-id :metadata :sending-image])]
    (when (< (count images) max-images-batch)
      {::chat-open-image-picker nil})))

(fx/defn camera-roll-pick
  {:events [:chat.ui/camera-roll-pick]}
  [{:keys [db]} uri]
  (let [current-chat-id (:current-chat-id db)
        images          (get-in db [:chats current-chat-id :metadata :sending-image])]
    (when (and (< (count images) max-images-batch)
               (not (get images uri)))
      {:db              (update-in db [:chats current-chat-id :metadata :sending-image] assoc uri {:uri uri})
       ::image-selected uri})))

(fx/defn save-image-to-gallery
  {:events [:chat.ui/save-image-to-gallery]}
  [_ base64-uri]
  {::save-image-to-gallery base64-uri})
