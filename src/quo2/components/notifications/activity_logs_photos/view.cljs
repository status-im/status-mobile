(ns quo2.components.notifications.activity-logs-photos.view
  (:require [react-native.core :as rn]
            [quo2.components.notifications.activity-logs-photos.style :as style]))

(defn view
  [{:keys [photos]}]
  [rn/view {:style style/photos-container}
   (map-indexed
    (fn [index photo]
      ^{:key index}
      [rn/image
       {:source photo
        :style  (style/photo index)}])
    photos)])
