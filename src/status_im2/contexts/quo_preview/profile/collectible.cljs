(ns status-im2.contexts.quo-preview.profile.collectible
  (:require
    [quo2.components.profile.collectible.view :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]
    [status-im2.common.resources :as resources]))

(defonce test-image (resources/get-mock-image :collectible))
(def test-images (repeat 10 test-image))

(def descriptor
  [{:label   "Number of images"
    :key     :num-images
    :type    :select
    :options (map (fn [n]
                    {:key n :value (str n " images")})
                  (range 1 (inc (count test-images))))}
   {:label "Random order of images"
    :key   :shuffle-images
    :type  :boolean}])

(defn preview-collectible
  []
  (let [state (reagent/atom {:num-images 1 :shuffle-images false})]
    (fn []
      (let [images-to-show (cond->> test-images
                             (:shuffle-images @state)
                             (shuffle)
                             :always
                             (take (:num-images @state)))]
        [preview/preview-container
         {:state      state
          :descriptor descriptor}
         [rn/view
          {:padding-vertical 100
           :align-items      :center}
          [quo/collectible
           {:images   images-to-show
            :on-press #(js/alert "Pressed")}]]]))))
