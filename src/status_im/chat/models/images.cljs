(ns status-im.chat.models.images
  (:require ["react-native-blob-util" :default ReactNativeBlobUtil]
            [re-frame.core :as re-frame]
            [react-native.share :as share]
            [react-native.cameraroll :as cameraroll]
            [status-im.ui.components.react :as react]
            [status-im2.config :as config]
            [react-native.fs :as fs]
            [utils.re-frame :as rf]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]))

(def temp-image-url (str (fs/cache-dir) "/StatusIm_Image.jpeg"))

(defn download-image-http
  [base64-uri on-success]
  (-> (.config ReactNativeBlobUtil
               (clj->js {:trusty platform/ios?
                         :path   temp-image-url}))
      (.fetch "GET" base64-uri)
      (.then #(on-success (.path %)))
      (.catch #(log/error "could not download image" {:error %}))))

(defn share-image
  [uri]
  (download-image-http uri
                       (fn [downloaded-url]
                         (share/open {:url       (str (when platform/android? "file://") downloaded-url)
                                      :isNewTask true}
                                     #(fs/unlink downloaded-url)
                                     #(fs/unlink downloaded-url)))))

(defn save-image-to-gallery
  [base64-uri on-success]
  (-> (download-image-http base64-uri cameraroll/save-image)
      (.then on-success)
      (.catch #(log/error (str "could not save image to gallery" %)))))

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
