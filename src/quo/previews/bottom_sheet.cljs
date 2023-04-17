(ns quo.previews.bottom-sheet
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.previews.preview :as preview]
            [quo.react-native :as rn]
            [reagent.core :as reagent]))

(def descriptor
  [{:label "Show handle:"
    :key   :show-handle?
    :type  :boolean}
   {:label "Backdrop dismiss:"
    :key   :backdrop-dismiss?
    :type  :boolean}
   {:label "Disable drag:"
    :key   :disable-drag?
    :type  :boolean}
   {:label "Android back cancel:"
    :key   :back-button-cancel
    :type  :boolean}
   {:label "Scrollable:"
    :key   :scrollable
    :type  :boolean}])

(defn cool-preview
  []
  (let [state      (reagent/atom {:show-handle?       true
                                  :backdrop-dismiss?  true
                                  :disable-drag?      false
                                  :back-button-cancel true})
        visible    (reagent/atom false)
        scrollable (reagent/cursor state [:scrollable])]
    (fn []
      [rn/view
       {:margin-bottom 50
        :padding       16}
       [preview/customizer state descriptor]
       [:<>
        [rn/view
         {:style {:align-items :center
                  :padding     16}}
         [rn/touchable-opacity {:on-press #(reset! visible true)}
          [rn/view
           {:style {:padding-horizontal 16
                    :padding-vertical   8
                    :border-radius      4
                    :background-color   (:interactive-01 @colors/theme)}}
           [quo/text {:color :secondary-inverse}
            (str "Open sheet: " @visible)]]]]

        [quo/bottom-sheet
         (merge @state
                {:visible?  @visible
                 :on-cancel #(reset! visible false)})
         [rn/view
          {:style {:height          (if @scrollable 1200 400)
                   :justify-content :center
                   :align-items     :center}}
          [rn/touchable-opacity {:on-press #(reset! visible false)}
           [quo/text {:color :link} "Close"]]
          [rn/touchable-opacity
           {:on-press #(swap! scrollable not)
            :style    {:padding-vertical 16}}
           [quo/text {:color :link} "Toggle size"]]
          [quo/text "Hello world!"]]]]])))

(defn preview
  []
  (fn []
    [rn/view
     {:background-color (:ui-background @colors/theme)
      :flex             1}
     [rn/flat-list
      {:flex                         1
       :keyboard-should-persist-taps :always
       :header                       [cool-preview]
       :key-fn                       str}]]))
