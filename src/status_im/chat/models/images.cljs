(ns status-im.chat.models.images
  (:require ["@react-native-community/cameraroll" :as CameraRoll]
            ["react-native-blob-util" :default ReactNativeBlobUtil]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [react-native.permissions :as permissions]
            [status-im.ui.components.react :as react]
            [status-im2.config :as config]
            [react-native.fs :as fs]
            [utils.re-frame :as rf]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

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

(rf/defn image-captured
  {:events [:chat.ui/image-captured]}
  [{:keys [db]} chat-id uri]
  (let [current-chat-id (or chat-id (:current-chat-id db))
        images          (get-in db [:chat/inputs current-chat-id :metadata :sending-image])]
    (when (and (< (count images) config/max-images-batch)
               (not (get images uri)))
      {::image-selected [uri current-chat-id]})))

(rf/defn clear-sending-images
  {:events [:chat.ui/clear-sending-images]}
  [{:keys [db]}]
  {:db (update-in db [:chat/inputs (:current-chat-id db) :metadata] assoc :sending-image {})})

(rf/defn image-unselected
  {:events [:chat.ui/image-unselected]}
  [{:keys [db]} original]
  (let [current-chat-id (:current-chat-id db)]
    {:db (update-in db [:chat/inputs current-chat-id :metadata :sending-image] dissoc (:uri original))}))

(rf/defn chat-show-image-picker-camera
  {:events [:chat.ui/show-image-picker-camera]}
  [{:keys [db]} chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))
        images          (get-in db [:chat/inputs current-chat-id :metadata :sending-image])]
    (when (< (count images) config/max-images-batch)
      {::chat-open-image-picker-camera current-chat-id})))
