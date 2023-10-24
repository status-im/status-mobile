(ns status-im2.common.floating-button-page.floating-container.view
  (:require [quo.foundations.colors :as colors]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]
            [status-im2.common.floating-button-page.floating-container.style :as style]))

(def blur-container-props
  {:blur-amount      36
   :blur-radius      25
   :blur-type        :light ;:transparent
   ;:overlay-color    (colors/theme-colors colors/black colors/neutral-70 theme)

   :background-color (if platform/android? colors/neutral-100 colors/neutral-80-opa-1-blur)})


(defn- blur-container
  [props & children]
  [rn/view
   (merge props
          {:width              "100%"
           :padding-horizontal 20
           :padding-vertical   12
           :overflow           :hidden})
   ;; TODO: add theme to blur props
   (into [blur/view blur-container-props] children)])

"
-  on-layout will trigger to dynamically get the height of the container to screen using it.
"
(defn view
  [{:keys [on-layout blur? container-style child-height theme show-background?
           floating?]}
   children]
  (let [insets                 (safe-area/get-insets)
        blur-active?           (and blur? show-background?)
        container-view         (if blur-active? blur-container rn/view)
        inline-container-style (if blur-active? style/blur-container style/view-container)]

    [container-view
     {:on-layout on-layout
      :style     (merge container-style
                        (inline-container-style floating? insets child-height theme show-background?))}
     children]))


