(ns quo2.components.counter.--tests--.counter-component-spec
  (:require ["@testing-library/react-native" :as rtl]
            [quo2.components.counter.counter :as counter]
            [reagent.core :as reagent]))

(defn render-counter
  ([]
   (render-counter {} nil))
  ([opts value]
   (rtl/render (reagent/as-element  [counter/counter opts value]))))

(js/global.test "default render of counter component"
                (fn []
                  (render-counter)
                  (-> (js/expect  (rtl/screen.getByTestId "counter-component"))
                      (.toBeTruthy))))

(js/global.test "renders counter with a string value"
                (fn []
                  (render-counter {} "1")
                  (-> (js/expect  (rtl/screen.getByText "1"))
                      (.toBeTruthy))))

(js/global.test "renders counter with an integer value"
                (fn []
                  (render-counter {} 1)
                  (-> (js/expect  (rtl/screen.getByText "1"))
                      (.toBeTruthy))))

(js/global.test "renders counter with value 99+ when the value is greater than 99"
                (fn []
                  (render-counter {} "100")
                  (-> (js/expect  (rtl/screen.getByText "99+"))
                      (.toBeTruthy))))