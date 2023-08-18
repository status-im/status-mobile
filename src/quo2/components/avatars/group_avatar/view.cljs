(ns quo2.components.avatars.group-avatar.view
  (:require [quo2.components.icon :as icon]
            [quo2.theme :as theme]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [quo2.components.avatars.group-avatar.style :as style]))

(def sizes
  {:icon      {:size/xs-20 12
               :size/sm-28   16
               :size/md-32  16
               :size/lg-48   20
               :size/xl-80 32}
   :container {:size/xs-20 20
               :size/sm-28   28
               :size/md-32  32
               :size/lg-48   48
               :size/xl-80 80}})

(defn- view-internal
  [_]
  (fn [{:keys [size theme customization-color picture]
        :or   {size                :size/xs-20
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
