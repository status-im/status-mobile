(ns quo2.components.avatars.group-avatar.view
  (:require [quo2.components.icon :as icon]
            [quo2.theme :as theme]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [quo2.components.avatars.group-avatar.style :as style]))

(def sizes
  {20 {:icon 12}
   28 {:icon 16}
   32 {:icon 16}
   48 {:icon 20}
   80 {:icon 32}})

(defn- view-internal
  [_]
  (fn [{:keys [size theme customization-color picture]
        :or   {size                20
               customization-color :blue}}]
    (let [icon-size (get-in sizes [size :icon])]
      [rn/view
       {:style (style/container {:container-size      size
                                 :customization-color customization-color
                                 :theme               theme})}
       (if picture
         [fast-image/fast-image
          {:source picture
           :style  {:width  size
                    :height size}}]
         [icon/icon :i/members
          {:size  icon-size
           :color colors/white-opa-70}])])))

(def view (theme/with-theme view-internal))
