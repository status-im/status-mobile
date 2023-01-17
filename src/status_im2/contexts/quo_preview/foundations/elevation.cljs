(ns status-im2.contexts.quo-preview.foundations.elevation
  (:require
   [quo2.foundations.elevation :as elevation]
   [quo2.foundations.colors :as colors]
   [quo2.core :as quo]
   [react-native.core :as rn]
   [reagent.core :as reagent]
   [status-im2.contexts.quo-preview.preview :as preview]))

(defn demo-box [shadow-on? name elevation-style]
  [rn/view {:margin-left :auto
            :margin-right :auto
            :margin-top 8
            :margin-bottom 8
            :align-items      :center}
   [quo/text {} name]
   [rn/view
    {:style (merge {:width 60
                    :height 60
                    :border-radius 16
                    :background-color (colors/theme-colors colors/white colors/neutral-90)}
                   (when shadow-on? elevation-style))}]])

(def descriptor
  [{:label   "Shadow on?"
    :key     :shadow-on?
    :type    :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:shadow-on?   true})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [:<>
         [quo/text {:style {:margin-left :auto
                            :margin-right :auto
                            :align-items      :center}}
          "Normal Scales"]
         [demo-box (:shadow-on? @state) "elevation 1" (:elevation-1 elevation/normal-scale)]
         [demo-box (:shadow-on? @state) "elevation 2" (:elevation-2 elevation/normal-scale)]
         [demo-box (:shadow-on? @state) "elevation 3" (:elevation-3 elevation/normal-scale)]
         [demo-box (:shadow-on? @state) "elevation 4" (:elevation-4 elevation/normal-scale)]
         [quo/text {:style {:margin-left :auto
                            :margin-right :auto
                            :align-items      :center}}
          "Inverted Scales"]
         [demo-box (:shadow-on? @state) "elevation 1" (:elevation-1 elevation/inverted-scale)]
         [demo-box (:shadow-on? @state) "elevation 2" (:elevation-2 elevation/inverted-scale)]
         [demo-box (:shadow-on? @state) "elevation 3" (:elevation-3 elevation/inverted-scale)]
         [demo-box (:shadow-on? @state) "elevation 4" (:elevation-4 elevation/inverted-scale)]
         [quo/text {:style {:margin-left :auto
                            :margin-right :auto
                            :align-items      :center}}
          "Inverted Scales"]
         [demo-box (:shadow-on? @state) "Inner Shadow" elevation/inner-shadow]]]])))

(defn preview-elevation
  []
  [rn/view
   {:background-color (colors/theme-colors colors/neutral-30 colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
