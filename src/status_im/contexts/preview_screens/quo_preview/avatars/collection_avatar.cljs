(ns status-im.contexts.preview-screens.quo-preview.avatars.collection-avatar

  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

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
