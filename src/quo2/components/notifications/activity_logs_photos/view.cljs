(ns quo2.components.notifications.activity-logs-photos.view
  (:require
    [clojure.string :as string]
    [quo2.components.markdown.text :as text]
    [quo2.components.notifications.activity-logs-photos.style :as style]
    [react-native.core :as rn]))

(defn view
  [{:keys [photos message-text]}]
  [:<>
   (when (not (string/blank? message-text))
     [text/text
      {:size                :paragraph-1
       :weight              :regular
       :style               style/text
       :number-of-lines     2
       :accessibility-label :activity-log-title}
      message-text])
   [rn/view {:style style/photos-container}
    (map-indexed
     (fn [index photo]
       ^{:key index}
       [rn/image
        {:source photo
         :style  (style/photo index)}])
     photos)]])
