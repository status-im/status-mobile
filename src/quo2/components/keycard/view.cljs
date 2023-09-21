(ns quo2.components.keycard.view
  (:require [react-native.core :as rn]
            [quo2.components.keycard.style :as style]
            [quo2.components.tags.tag :as tag]
            [quo2.foundations.colors :as colors]
            [quo2.foundations.resources :as resources]
            [utils.i18n :as i18n]
            [quo2.theme :as quo.theme]))

(defn- view-internal
  "This component based on the following properties:
  - :holder-name - Can be owner's name. Default is Empty
  - :locked? - Boolean to specify whether the keycard is locked or not
  - :theme :light/:dark
  "
  [{:keys [holder-name locked? theme]}]
  (let [label (if (boolean holder-name)
                (i18n/label :t/user-keycard {:name holder-name})
                (i18n/label :t/empty-keycard))]
    [rn/view {:style (style/card-container locked? theme)}
     [rn/image
      {:source (resources/get-image :keycard-logo)
       :style  (style/keycard-logo locked? theme)}]
     [rn/image
      {:source (resources/get-image
                (if (or locked? (= :dark theme)) :keycard-chip-dark :keycard-chip-light))
       :style  style/keycard-chip}]
     [rn/image
      {:source (resources/get-image :keycard-watermark)
       :style  (style/keycard-watermark locked? theme)}]
     [tag/tag
      {:size                32
       :type                (when locked? :icon)
       :label               label
       :labelled?           true
       :blurred?            true
       :resource            (when locked? :i/locked)
       :accessibility-label :holder-name
       :icon-color          colors/white-70-blur
       :override-theme      (when locked? :dark)}]]))

(def keycard (quo.theme/with-theme view-internal))
