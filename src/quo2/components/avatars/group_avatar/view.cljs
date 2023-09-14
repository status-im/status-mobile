(ns quo2.components.avatars.group-avatar.view
  (:require [quo2.components.icon :as icon]
            [quo2.theme :as quo.theme]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [quo2.components.avatars.group-avatar.style :as style]))

(def sizes
  {:size-20 {:icon      12
               :container 20}
   :size-28 {:icon      16
               :container 28}
   :size-32 {:icon      16
               :container 32}
   :size-48 {:icon      20
               :container 48}
   :size-80 {:icon      32
               :container 80}})

(defn- view-internal
  [_]
  (fn [{:keys [size theme customization-color picture icon-name]
        :or   {size                :size-20
               customization-color :blue
               picture             nil
               icon-name           :i/group}}]
    (let [container-size (get-in sizes [size :container])
          icon-size      (get-in sizes [size :icon])]
      [rn/view
       {:style (style/container {:container-size      container-size
                                 :customization-color customization-color
                                 :theme               theme})}
       (if picture
         [fast-image/fast-image
          {:source picture
           :style  {:width  container-size
                    :height container-size}}]
         [icon/icon icon-name
          {:size  icon-size
           :color colors/white-opa-70}])])))

(def view (quo.theme/with-theme view-internal))
