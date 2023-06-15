(ns quo2.components.colors.color-picker.component-spec
  (:require [quo2.components.colors.color-picker.view :as color-picker]
            [reagent.core :as reagent]
            [test-helpers.component :as h]))

(def color-list [:blue :yellow :turquoise :copper :sky :camel :orange :army :pink :purple :magenta])

(h/describe "color-picker"
  (h/test "color picker rendered"
    (h/render [color-picker/view])
    (-> (h/expect (h/get-all-by-label-text :color-picker-item))
        (.toHaveLength 11)))
  (h/test "clicks on a color item"
    (let [event (h/mock-fn)]
      (h/render [color-picker/view {:on-change #(event)}])
      (h/fire-event :press (get (h/get-all-by-label-text :color-picker-item) 0))
      (-> (h/expect event)
          (.toHaveBeenCalled))))
  (h/test "color picker color changed"
    (let [selected (reagent/atom nil)]
      (h/render [color-picker/view {:on-change #(reset! selected %)}])
      (h/fire-event :press (get (h/get-all-by-label-text :color-picker-item) 0))
      (-> (h/expect @selected)
          (.toStrictEqual :blue))))
  (h/test "custom colors render in expected order and values"
    (h/render [color-picker/view])
    (js/Promise.all (map (fn [color]
                           (h/is-truthy (h/get-all-by-label-text color)))
                         color-list))))


