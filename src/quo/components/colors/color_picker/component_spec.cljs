(ns quo.components.colors.color-picker.component-spec
  (:require
    [quo.components.colors.color-picker.view :as color-picker]
    [reagent.core :as reagent]
    [test-helpers.component :as h]))

(h/describe "color-picker"
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
          (.toStrictEqual :blue)))))
