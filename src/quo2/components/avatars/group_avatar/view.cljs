(ns quo2.components.avatars.group-avatar.view
  (:require [quo2.components.icon :as icon]
            [quo2.theme :as theme]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [quo2.components.avatars.group-avatar.style :as style]))

(def sizes
  {:icon      {:x-small 12
               :small   16
               :medium  16
               :large   20
               :x-large 32}
   :container {:x-small 20
               :small   28
               :medium  32
               :large   48
               :x-large 80}})

(defn- view-internal
  [_]
  (fn [{:keys [size theme customization-color picture]
        :or   {size                :x-small
               customization-color :blue
               picture             nil}}]
    (let [container-size (get-in sizes [:container size])
          icon-size      (get-in sizes [:icon size])]
      [rn/view
       {:style (style/container {:container-size      container-size
                                 :customization-color customization-color
                                 :theme               theme})}
       (if picture
         [fast-image/fast-image
          {:source picture
           :style  {:width  container-size
                    :height container-size}}]
         [icon/icon :i/members
          {:size  icon-size
           :color colors/white-opa-70}])])))

(def view (theme/with-theme view-internal))
