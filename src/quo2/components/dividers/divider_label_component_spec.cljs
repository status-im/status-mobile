(ns quo2.components.dividers.divider-label-component-spec
  (:require ["@testing-library/react-native" :as rtl]
            [quo2.components.dividers.divider-label :as divider-label]
            [reagent.core :as reagent]))

(defn render-divider-label
  ([]
   (render-divider-label {}))
  ([opts]
   (rtl/render (reagent/as-element [divider-label/divider-label opts]))))

(js/global.test "default render of divider-label component"
  (fn []
    (render-divider-label)
    (-> (js/expect (rtl/screen.getByLabelText "divider-label"))
        (.toBeTruthy))))

(js/global.test "render divider-label component with a label"
  (fn []
    (render-divider-label {:label :hello})
    (-> (js/expect (rtl/screen.getByText "hello"))
        (.toBeTruthy))))

(js/global.test "render divider-label component with a counter-value"
  (fn []
    (render-divider-label {:label         :hello
                           :counter-value "1"})
    (-> (js/expect (rtl/screen.getByText "1"))
        (.toBeTruthy))))

(js/global.test "divider-label chevron icon renders to the left when set"
  (fn []
    (render-divider-label {:label            :hello
                           :counter-value    "1"
                           :chevron-position :left})
    (-> (js/expect (rtl/screen.getByTestId "divider-label-icon-left"))
        (.toBeTruthy))))

(js/global.test "divider-label chevron icon renders to the right when set"
  (fn []
    (render-divider-label {:label            :hello
                           :counter-value    "1"
                           :chevron-position :right})
    (-> (js/expect (rtl/screen.getByTestId "divider-label-icon-right"))
        (.toBeTruthy))))
