(ns status-im.contexts.chat.photo-selector.effects
  (:require
    [clojure.string :as string]
    [react-native.cameraroll :as cameraroll]
    [react-native.core :as rn]
    [react-native.image-resizer :as image-resizer]
    [react-native.permissions :as permissions]
    [react-native.platform :as platform]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def ^:private maximum-image-size-px 2000)

(rf/reg-fx :effects.camera-roll/request-permissions-and-get-photos
 (fn [[num end-cursor album]]
   (permissions/request-permissions
    {:permissions [(if platform/is-below-android-13? :read-external-storage :read-media-images)]
     :on-allowed
     (fn []
       (cameraroll/get-photos
        (merge {:first      num
                :assetType  "Photos"
                :groupTypes (if (= album (i18n/label :t/recent)) "All" "Albums")
                :groupName  (if (not= album (i18n/label :t/recent)) album "")
                :include    ["imageSize"]}
               (when end-cursor
                 {:after end-cursor}))
        #(rf/dispatch [:on-camera-roll-get-photos (:edges %) (:page_info %) end-cursor])))})))

(defn- resize-photo
  [uri callback]
  (rn/image-get-size
   uri
   (fn [width height]
     (let [resize? (> (max width height) maximum-image-size-px)]
       (image-resizer/resize
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

(rf/reg-fx :effects.camera-roll/image-selected
 (fn [[image chat-id]]
   (resize-photo (:uri image) #(rf/dispatch [:photo-selector/image-selected chat-id image %]))))

(defn- get-albums
  [callback]
  (let [albums (atom {:smart-album []
                      :my-albums   []})]
    ;; Get the "recent" album first
    (cameraroll/get-photos
     {:first 1 :groupTypes "All" :assetType "Photos"}
     (fn [res-recent]
       (swap! albums assoc
         :smart-album
         {:title (i18n/label :t/recent)
          :uri   (get-in (first (:edges res-recent)) [:node :image :uri])})
       ;; Get albums, then loop over albums and get each one's cover (first photo)
       (cameraroll/get-albums
        {:assetType "Photos"}
        (fn [res-albums]
          (let [response-count (count res-albums)]
            (if (pos? response-count)
              (doseq [album res-albums]
                (cameraroll/get-photos
                 {:first 1 :groupTypes "Albums" :groupName (:title album) :assetType "Photos"}
                 (fn [res]
                   (let [uri (get-in (first (:edges res)) [:node :image :uri])]
                     (swap! albums update :my-albums conj (merge album {:uri uri}))
                     (when (= (count (:my-albums @albums)) response-count)
                       (swap! albums update :my-albums #(sort-by :title %))
                       (callback @albums))))))
              (callback @albums)))))))))

(rf/reg-fx :effects.camera-roll/get-albums
 (fn []
   (get-albums #(rf/dispatch [:on-camera-roll-get-albums %]))))

(defn get-photos-count-ios-fx
  [cb]
  (cameraroll/get-photos-count-ios cb))

(rf/reg-fx :effects.camera-roll/get-photos-count-ios
 (fn []
   (when platform/ios?
     (get-photos-count-ios-fx #(rf/dispatch [:on-camera-roll-get-images-count-ios %])))))
