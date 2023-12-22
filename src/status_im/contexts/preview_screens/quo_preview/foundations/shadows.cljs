(ns status-im.contexts.preview-screens.quo-preview.foundations.shadows
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.foundations.shadows :as shadows]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(defn demo-box
  [shadow? description shadow-style]
  [rn/view
   {:style {:margin-left   :auto
            :margin-right  :auto
            :margin-top    8
            :margin-bottom 8
            :align-items   :center}}
   [quo/text {} description]
   [rn/view
    {:style (merge {:width            60
                    :height           60
                    :border-radius    16
                    :background-color (colors/theme-colors colors/white colors/neutral-90)}
                   (when shadow? shadow-style))}]])

(def descriptor
  [{:key  :shadow?
    :type :boolean}])

(defn view
  []
  (let [state   (reagent/atom {:shadow? true})
        shadow? (reagent/cursor state [:shadow?])]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/text
        {:style {:margin-left  :auto
                 :margin-right :auto
                 :align-items  :center}}
        "Normal Scales"]
       [demo-box @shadow? "Shadow 1" (shadows/get 1)]
       [demo-box @shadow? "Shadow 2" (shadows/get 2)]
       [demo-box @shadow? "Shadow 3" (shadows/get 3)]
       [demo-box @shadow? "Shadow 4" (shadows/get 4)]
       [quo/text
        {:style {:margin-left  :auto
                 :margin-right :auto
                 :align-items  :center}}
        "Inverted Scales"]
       [demo-box @shadow? "Shadow 1" (shadows/get 1 (quo.theme/get-theme) :inverted)]
       [demo-box @shadow? "Shadow 2" (shadows/get 2 (quo.theme/get-theme) :inverted)]
       [demo-box @shadow? "Shadow 3" (shadows/get 3 (quo.theme/get-theme) :inverted)]
       [demo-box @shadow? "Shadow 4" (shadows/get 4 (quo.theme/get-theme) :inverted)]
       [quo/text
        {:style {:margin-left  :auto
                 :margin-right :auto
                 :align-items  :center}}
        "Inverted Scales"]
       [demo-box @shadow? "Inner Shadow" shadows/inner-shadow]])))
