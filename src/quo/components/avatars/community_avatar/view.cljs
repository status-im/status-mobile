(ns quo.components.avatars.community-avatar.view
  (:require [quo.components.avatars.community-avatar.style :as style]
            [react-native.core :as rn]
            [schema.core :as schema]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:size {:optional true} [:maybe [:enum :size-32 :size-24]]]
      [:image :schema.common/image-source]
      [:container-style {:optional true} [:maybe :map]]]]]
   :any])

(defn- view-internal
  [{:keys [size image container-style]}]
  [rn/image
   {:source              image
    :accessibility-label :community-avatar
    :style               (merge (style/image size) container-style)}])

(def view (schema/instrument #'view-internal ?schema))
