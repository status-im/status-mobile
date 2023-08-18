(ns quo2.components.wallet.network-link.view
  (:require
    [quo2.theme :as quo.theme]
    [quo2.foundations.resources :as resources]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [react-native.platform :as platform]))

(def height
  {:linear 10
   :1x     66
   :2x     122})

(defn- view-internal
  [{:keys [shape theme preview?]}]
  ;; Updating image height on the fly (in preview screen) on Android does not work with fast-image
  ;; so we are using rn/image for preview screen
  (let [image-component (if (and platform/android? preview?) rn/image fast-image/fast-image)
        resource-key             (str "network-link-" (name shape) "-" (name theme))]
    [image-component
     {:source      (resources/get-image (keyword resource-key))
      :style       {:width  73
                    :height (get height shape)}
      :resize-mode :contain}]))

(def view (quo.theme/with-theme view-internal))
