(ns react-native.image-resizer
  (:require
    ["react-native-image-resizer" :default image-resizer]))

(defn resize
  [{:keys [path max-width max-height quality]}]
  (.createResizedImage image-resizer path max-width max-height "JPEG" quality 0 nil false))
