(ns quo.components.avatars.collection-avatar.view
  (:require
    [quo.components.avatars.collection-avatar.style :as style]
    [quo.context :as quo.context]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]))

(defn view
  "Opts:

    :image - collection image
    :theme - keyword -> :light/:dark"
  [{:keys [image size on-load-start on-load-end on-error] :or {size :size-24}}]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style (style/collection-avatar-container theme size)}
     [fast-image/fast-image
      {:accessibility-label :collection-avatar
       :source              image
       :on-load-start       on-load-start
       :on-load-end         on-load-end
       :on-error            on-error
       :style               (style/collection-avatar size)}]]))
