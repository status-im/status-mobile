(ns quo.components.notifications.activity-logs-photos.style)

(def text {:margin-bottom 8})

(def photos-container
  {:flex           1
   :height         40
   :flex-direction :row})

(defn photo
  [index]
  {:width         40
   :height        40
   :border-radius 10
   :margin-left   (if (= index 0) 0 8)})
