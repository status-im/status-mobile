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
        (fn [resized-image]
          (let [path (aget resized-image "path")
                path (if (string/starts-with? path "file") path (str "file://" path))]
            (cb path)))
        #(log/error "could not resize image" %))))))

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
         (fn [resized-image]
           (let [path (aget resized-image "path")
                 path (if (string/starts-with? path "file") path (str "file://" path))]
             (.saveToCameraRoll CameraRoll path)))
         #(log/error "could not resize image" %)))))))

(re-frame/reg-fx
 ::chat-open-image-picker
 (fn []
   (react/show-image-picker
    (fn [result]
      (resize-and-call
       (aget result "path")
       #(re-frame/dispatch [:chat.ui/image-selected %])))
    "photo")))

(re-frame/reg-fx
 ::image-selected
 (fn [uri]
   (resize-and-call
    uri
    #(re-frame/dispatch [:chat.ui/image-selected %]))))

(re-frame/reg-fx
 ::camera-roll-get-photos
 (fn [num]
   (permissions/request-permissions
    {:permissions [:read-external-storage]
     :on-allowed  (fn []
                    (-> (.getPhotos CameraRoll #js {:first num :assetType "Photos" :groupTypes "All"})
                        (.then #(re-frame/dispatch [:on-camera-roll-get-photos (:edges (types/js->clj %))]))
                        (.catch #(log/error "could not get cameraroll photos"))))})))

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

(fx/defn cancel-sending-image
  {:events [:chat.ui/cancel-sending-image]}
  [{:keys [db]}]
  (let [current-chat-id (:current-chat-id db)]
    {:db (update-in db [:chats current-chat-id :metadata] dissoc :sending-image)}))

(fx/defn image-selected
  {:events [:chat.ui/image-selected]}
  [{:keys [db]} uri]
  (let [current-chat-id (:current-chat-id db)]
    {:db (assoc-in db [:chats current-chat-id :metadata :sending-image :uri] uri)}))

(fx/defn chat-open-image-picker
  {:events [:chat.ui/open-image-picker]}
  [_]
  {::chat-open-image-picker nil})

(fx/defn camera-roll-pick
  {:events [:chat.ui/camera-roll-pick]}
  [_ uri]
  {::image-selected uri})

(fx/defn save-image-to-gallery
  {:events [:chat.ui/save-image-to-gallery]}
  [_ base64-uri]
  {::save-image-to-gallery base64-uri})
