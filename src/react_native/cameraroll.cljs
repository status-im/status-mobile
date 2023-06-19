(ns react-native.cameraroll
  (:require ["@react-native-community/cameraroll" :as CameraRoll]
            [utils.transforms :as transforms]
            [taoensso.timbre :as log]))

(defn get-photos
  [opts callback]
  (-> (.getPhotos CameraRoll (clj->js opts))
      (.then #(callback (transforms/js->clj %)))
      (.catch #(log/warn "could not get camera roll photos" %))))

(defn get-albums
  [opts callback]
  (-> (.getAlbums CameraRoll (clj->js opts))
      (.then #(callback (transforms/js->clj %)))
      (.catch #(log/warn "could not get camera roll albums" %))))
