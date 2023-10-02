(ns status-im2.contexts.quo-preview.avatars.collection-avatar

  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]))

(def descriptor
  [{:key     :image
    :type    :select
    :options [{:key   (resources/get-mock-image :bored-ape)
               :value "Bored ape"}
              {:key   (resources/get-mock-image :ring)
               :value "Circle"}]}])

(defn view
  []
  (let [state (reagent/atom {:image (resources/get-mock-image :bored-ape)})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:align-items :center}}
       [quo/collection-avatar @state]])))
