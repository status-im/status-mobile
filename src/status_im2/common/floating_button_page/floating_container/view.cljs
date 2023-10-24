(ns status-im2.common.floating-button-page.floating-container.view
  (:require     [quo.foundations.colors :as colors]
                [react-native.blur :as blur]
                [react-native.core :as rn]
                [react-native.platform :as platform]
                [react-native.safe-area :as safe-area]
                [status-im2.common.floating-button-page.floating-container.style :as style]))

(def blur-container-props
  {:blur-amount      34
   :blur-type        :transparent
   :overlay-color    :transparent
   :background-color (if platform/android? colors/neutral-100 colors/neutral-80-opa-1-blur)})

"
-  on-layout will trigger to dynamically get the height of the container to screen using it.
"
(defn view [{:keys [on-layout blur? container-style  child-height  theme]} children]
  (fn [{:keys [show-background? floating?]} _]
    (let [insets (safe-area/get-insets)
          blur-active? (and blur? show-background?)
          container-view (if blur-active? blur/view rn/view)
          inline-container-style  (if  blur-active? style/blur-container style/view-container)]


      [container-view (merge
                       {:on-layout on-layout
                        :style (merge container-style (inline-container-style floating? insets child-height theme show-background?))}
                       (when blur-active? blur-container-props))
       children])))


