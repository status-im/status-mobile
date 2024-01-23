(ns react-native.cameraroll
  (:require
    ["@react-native-camera-roll/camera-roll" :refer [CameraRoll]]
    [react-native.fs :as fs]
    [taoensso.timbre :as log]
    [utils.transforms :as transforms]))

(defn promise-get-photos
  [opts]
  (-> (.getPhotos CameraRoll (clj->js opts))
      (.then transforms/js->clj)))

(defn promise-get-albums
  [opts]
  (-> (.getAlbums CameraRoll (clj->js opts))
      (.then transforms/js->clj)))



(defn get-photos
  [opts callback]
  (-> (.getPhotos CameraRoll (clj->js opts))
      ;; With callback approach, the value isn't accessible from the returned promise.
      (.then #(callback (transforms/js->clj %)))
      (.catch #(log/warn "could not get camera roll photos" %))))

(defn get-albums
  [opts callback]
  (-> (.getAlbums CameraRoll (clj->js opts))
      ;; With callback approach, the value isn't accessible from the returned promise.
      (.then #(callback (transforms/js->clj %)))
      (.catch #(log/warn "could not get camera roll albums" %))))

(defn save-image
  [path]
  (-> (.save CameraRoll (clj->js path))
      (.then #(fs/unlink path))
      (.catch #(fs/unlink path))))

(defn get-photos-count-ios
  [cb]
  (-> (.getPhotosCountiOS CameraRoll)
      (.then #(cb %))
      (.catch #(js/console.error %))))
