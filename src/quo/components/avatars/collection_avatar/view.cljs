(ns quo.components.avatars.collection-avatar.view
  (:require
    [quo.components.avatars.collection-avatar.style :as style]
    [quo.theme :as quo.theme]
    [react-native.fast-image :as fast-image]))

(defn- view-internal
  "Opts:
   
    :image - collection image
    :theme - keyword -> :light/:dark"
  [{:keys [image theme]}]
  [fast-image/fast-image
   {:accessibility-label :collection-avatar
    :source              image
    :style               (style/collection-avatar theme)}])

(def view (quo.theme/with-theme view-internal))
