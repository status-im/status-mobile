(ns quo2.components.selectors.--tests--.selectors-component-spec
  (:require ["@testing-library/react-native" :as rtl]
            [quo2.components.selectors.selectors :as selectors]
            [reagent.core :as reagent]))

(defn render-toggle
  ([]
   (rtl/render (reagent/as-element  [selectors/toggle {:disabled? (:disabled? false)}]))))

(js/global.test "default render of toggle component"
                (fn []
                  (render-toggle)
                  (-> (js/expect  (rtl/screen.getByTestId "toggle-component"))
                      (.toBeTruthy))))