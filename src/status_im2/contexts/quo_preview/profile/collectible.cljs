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

(defn cool-preview
  []
  (let [state (reagent/atom {:num-images 1 :shuffle-images false})]
    (fn []
      (let [images-to-show (cond->> test-images
                             (:shuffle-images @state)
                             (shuffle)
                             :always
                             (take (:num-images @state)))]
        [rn/view
         {:margin-bottom 50
          :padding       16}
         [preview/customizer state descriptor]
         [rn/view
          {:padding-vertical 100
           :align-items      :center}
          [quo/collectible {:images images-to-show}]]]))))

(defn preview-collectible
  []
  [rn/view {:style {:flex 1}}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
