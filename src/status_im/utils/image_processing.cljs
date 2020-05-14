(ns status-im.utils.image-processing
  (:require ["react-native-image-resizer" :default image-resizer]))

(defn resize [path max-width max-height quality on-resize on-error]
  (-> (.createResizedImage image-resizer path max-width max-height "JPEG" quality 0 nil)
      (.then on-resize)
      (.catch on-error)))
