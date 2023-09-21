(ns status-im2.contexts.quo-preview.avatars.group-avatar
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key   :size/s-20
               :value "20"}
              {:key   :size/s-28
               :value "28"}
              {:key   :size/s-32
               :value "32"}
              {:key   :size/s-48
               :value "48"}
              {:key   :size/s-80
               :value "80"}]}
   {:label "Avatar:"
    :key   :picture?
    :type  :boolean}
   (preview/customization-color-option)])

(def avatar (resources/get-mock-image :photo1))

(defn view
  []
  (let [state (reagent/atom {:customization-color :blue
                             :size                :size/s-20
                             :picture?            false})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/group-avatar
        (cond-> @state
          (:picture? @state)
          (assoc :picture avatar))]])))
