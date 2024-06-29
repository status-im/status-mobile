(ns quo.components.avatars.user-avatar.schema
  (:require
    [quo.components.avatars.user-avatar.style :as style]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:full-name {:optional true} [:maybe string?]]
      [:size {:optional true} [:maybe (into [:enum] (keys style/sizes))]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:static? {:optional true} [:maybe boolean?]]
      [:status-indicator? {:optional true} [:maybe boolean?]]
      [:online? {:optional true} [:maybe boolean?]]
      [:ring? {:optional true} [:maybe boolean?]]
      [:profile-picture
       {:optional true}
       [:maybe :schema.quo/profile-picture-source]]]]]
   :any])
