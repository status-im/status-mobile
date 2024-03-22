(ns quo.components.avatars.collection-avatar.view
  (:require
    [quo.components.avatars.collection-avatar.style :as style]
    [quo.theme :as quo.theme]
    [react-native.fast-image :as fast-image]))

(defn- view-internal
  "Opts:
   
    :image - collection image
    :theme - keyword -> :light/:dark"
  [{:keys [image theme size] :or {size :size-24}}]
  [fast-image/fast-image
   {:accessibility-label :collection-avatar
    :source              image
    :style               (style/collection-avatar theme size)}])

(def view (quo.theme/with-theme view-internal))
