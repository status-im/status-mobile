(ns legacy.status-im.ui.components.common.common
  (:require
    [legacy.status-im.ui.components.common.styles :as styles]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.react :as react]
    [reagent.core :as reagent]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]]))

(defn logo
  [{:keys [size]}]
  [icons/icon :icons/logo (styles/logo size)])

;;TODO DEPRECATED, use legacy.status-im.ui.components.badge
(defn counter
  ([value] (counter nil value))
  ([{:keys [size accessibility-label] :or {size 18}} value]
   (let [more-than-9 (> value 9)]
     [react/view {:style (styles/counter-container size)}
      [react/text
       (cond-> {:style (styles/counter-label size)}
         accessibility-label
         (assoc :accessibility-label accessibility-label))
       (if more-than-9 (i18n/label :t/counter-9-plus) value)]])))

(def small-screen-image-k 0.8)
(def small-screen-height 600)

(defview image-contain
  [{:keys [container-style style]} {:keys [image width height]}]
  (letsubs [content-width                               (reagent/atom 0)
            {window-width :width window-height :height} [:dimensions/window]]
    [react/view
     {:style     (merge styles/image-contain container-style)
      :on-layout #(reset! content-width (-> ^js % .-nativeEvent .-layout .-width))}
     [react/image
      {:source      image
       :resize-mode :contain
       :style       (merge style
                           (if (> window-height window-width)
                             {:width  (* @content-width
                                         (if (< window-height small-screen-height)
                                           small-screen-image-k
                                           1))
                              :height (/ (* @content-width
                                            height
                                            (if (< window-height small-screen-height)
                                              small-screen-image-k
                                              1))
                                         width)}
                             {:width  @content-width
                              :height (* window-height small-screen-image-k)}))}]]))
