(ns status-im.contexts.preview-screens.quo-preview.profile.collectible
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(defonce test-image (resources/get-mock-image :collectible))
(def test-images (repeat 10 test-image))

(def descriptor
  [{:key     :num-images
    :type    :select
    :options (map (fn [n]
                    {:key n :value (str n " images")})
                  (range 1 (inc (count test-images))))}])

(defn view
  []
  (let [state (reagent/atom {:num-images 1})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 20
                                    :align-items      :center}}
       [quo/collectible
        {:images   (repeat (:num-images @state) test-image)
         :on-press #(js/alert "Pressed")}]])))
