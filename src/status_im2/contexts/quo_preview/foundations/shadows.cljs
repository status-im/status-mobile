(ns status-im2.contexts.quo-preview.foundations.shadows
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [quo2.foundations.shadows :as shadows]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(defn demo-box
  [shadow-on? name shadow-style]
  [rn/view
   {:margin-left   :auto
    :margin-right  :auto
    :margin-top    8
    :margin-bottom 8
    :align-items   :center}
   [quo/text {} name]
   [rn/view
    {:style (merge {:width            60
                    :height           60
                    :border-radius    16
                    :background-color (colors/theme-colors colors/white colors/neutral-90)}
                   (when shadow-on? shadow-style))}]])

(def descriptor
  [{:label "Shadow on?"
    :key   :shadow-on?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:shadow-on? true})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [:<>
         [quo/text
          {:style {:margin-left  :auto
                   :margin-right :auto
                   :align-items  :center}}
          "Normal Scales"]
         [demo-box (:shadow-on? @state) "shadow 1" (shadows/get 1)]
         [demo-box (:shadow-on? @state) "shadow 2" (shadows/get 2)]
         [demo-box (:shadow-on? @state) "shadow 3" (shadows/get 3)]
         [demo-box (:shadow-on? @state) "shadow 4" (shadows/get 4)]
         [quo/text
          {:style {:margin-left  :auto
                   :margin-right :auto
                   :align-items  :center}}
          "Inverted Scales"]
         [demo-box (:shadow-on? @state) "shadow 1" (shadows/get 1 (quo.theme/get-theme) true)]
         [demo-box (:shadow-on? @state) "shadow 2" (shadows/get 2 (quo.theme/get-theme) true)]
         [demo-box (:shadow-on? @state) "shadow 3" (shadows/get 3 (quo.theme/get-theme) true)]
         [demo-box (:shadow-on? @state) "shadow 4" (shadows/get 4 (quo.theme/get-theme) true)]
         [quo/text
          {:style {:margin-left  :auto
                   :margin-right :auto
                   :align-items  :center}}
          "Inverted Scales"]
         [demo-box (:shadow-on? @state) "Inner Shadow" shadows/inner-shadow]]]])))

(defn preview-shadows
  []
  [rn/view
   {:background-color (colors/theme-colors colors/neutral-30 colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
