(ns quo2.components.avatars.group-avatar.view
  (:require [quo2.components.icon :as icon]
            [quo2.theme :as theme]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [quo2.components.avatars.group-avatar.style :as style]))

(def sizes
  {:icon      {:size/x-small 12
               :size/small   16
               :size/medium  16
               :size/large   20
               :size/x-large 32}
   :container {:size/x-small 20
               :size/small   28
               :size/medium  32
               :size/large   48
               :size/x-large 80}})

(defn- view-internal
  [_]
  (fn [{:keys [size theme customization-color picture]
        :or   {size                :size/x-small
               customization-color :blue}}]
    (println size)
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
