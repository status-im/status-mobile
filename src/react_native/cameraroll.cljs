(ns react-native.cameraroll
  (:require ["@react-native-camera-roll/camera-roll" :as CameraRoll]
            [react-native.fs :as fs]
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

(defn save-image
  [path]
  (-> (.save CameraRoll (clj->js path))
      (.then #(fs/unlink path))
      (.catch #(fs/unlink path))))
