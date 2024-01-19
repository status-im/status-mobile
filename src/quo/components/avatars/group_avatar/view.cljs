(ns quo.components.avatars.group-avatar.view
  (:require
    [quo.components.avatars.group-avatar.style :as style]
    [quo.components.icon :as icon]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.fast-image :as fast-image]
    [react-native.pure :as rn.pure]))

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

(defn- view-pure
  [{:keys [size customization-color picture icon-name]
    :or   {size                :size-20
           customization-color :blue
           picture             nil
           icon-name           :i/group}}]
  (let [theme          (quo.theme/use-theme)
        container-size (get-in sizes [size :container])
        icon-size      (get-in sizes [size :icon])]
    (rn.pure/view
     {:accessibility-label :group-avatar
      :style               (style/container {:container-size      container-size
                                             :customization-color customization-color
                                             :theme               theme})}
     (if picture
       (fast-image/fast-image
        {:source picture
         :style  {:width  container-size
                  :height container-size}})
       (icon/icon icon-name
                  {:size  icon-size
                   :color colors/white-opa-70})))))

(defn view [props] (rn.pure/func view-pure props))
