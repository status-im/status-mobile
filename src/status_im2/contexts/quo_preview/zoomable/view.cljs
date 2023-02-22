(ns status-im2.contexts.quo-preview.zoomable.view
  (:require
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.lightbox.zoomable-image.view :as zoomable-image]))


(defn cool-preview
  []
  [:f>
   (fn []
     [zoomable-image/zoomable-image
      {:image-width  1000
       :image-height 1000
       :message-id   "xyz"
       :content      {:image "https://cdn.pixabay.com/photo/2017/11/14/13/06/kitty-2948404__340.jpg"}}
      0 (reanimated/use-shared-value 0) #(println "on-tap")])])

(defn preview-zoomable
  []
  [rn/view
   {:background-color colors/neutral-100
    :flex             1}
   [rn/flat-list
    {:keyboardShouldPersistTaps :always
     :style {:flex 1
             :height "100%"}
     :content-container-style {:justify-content :center
                               :align-items :center}
     :header                    [cool-preview]
     :key-fn                    str}]])
