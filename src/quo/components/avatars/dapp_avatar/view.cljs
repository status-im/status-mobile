(ns quo.components.avatars.dapp-avatar.view
  (:require [quo.components.avatars.dapp-avatar.style :as style]
            [react-native.core :as rn]
            [react-native.hole-view :as hole-view]
            [react-native.platform :as platform]
            [schema.core :as schema]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:context? {:optional true} [:maybe :boolean]]
      [:size {:optional true} [:maybe [:enum :size-32 :size-64]]]
      [:image :schema.common/image-source]
      [:network-image {:optional true} [:maybe :schema.common/image-source]]
      [:container-style {:optional true} [:maybe :map]]]]]
   :any])

(defn- view-internal
  [{:keys [context? size image network-image container-style]}]
  [rn/view
   {:style               (-> (style/container size)
                             (merge container-style))
    :accessibility-label :dapp-avatar}
   [hole-view/hole-view
    (cond-> {:holes (if context?
                      [(style/context-hole size)]
                      [])
             :style (style/hole-view size)}
      platform/android? (assoc :key context?))
    [rn/image
     {:source image
      :style  (style/image size)}]]
   (when context?
     [rn/image
      {:source network-image
       :style  (style/context size)}])])

(def view (schema/instrument #'view-internal ?schema))
