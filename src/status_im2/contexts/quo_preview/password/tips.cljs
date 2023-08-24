(ns status-im2.contexts.quo-preview.password.tips
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Text"
    :key   :text
    :type  :text}
   {:label "Completed"
    :key   :completed?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state      (reagent/atom {:text       "Lower case"
                                  :completed? false})
        text       (reagent/cursor state [:text])
        completed? (reagent/cursor state [:completed?])]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding 60 :background-color colors/neutral-95}
         [quo/tips {:completed? @completed?} @text]]]])))

(defn preview-tips
  []
  [rn/view
   {:background-color (colors/theme-colors
                       colors/white
                       colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])

