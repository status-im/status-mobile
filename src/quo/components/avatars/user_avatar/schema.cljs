(ns quo.components.avatars.user-avatar.schema
  (:require
    [quo.components.avatars.user-avatar.style :as style]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:schema.common/map {:optional true :maybe true}
      [:full-name string?]
      [:size (into [:enum] (keys style/sizes))]
      [:customization-color :schema.common/customization-color]
      [:static? boolean?]
      [:status-indicator? boolean?]
      [:online? boolean?]
      [:ring? boolean?]
      [:profile-picture :schema.quo/profile-picture-source]]]]
   :any])
