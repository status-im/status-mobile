(ns quo2.components.selectors.--tests--.selectors-component-spec
  (:require
   ["@testing-library/react-native" :as rtl]
   [quo2.components.selectors.selectors :as selectors]
   [reagent.core :as reagent]))

(defn render-toggle
  ([]
   (render-toggle {}))
  ([opts]
   (rtl/render (reagent/as-element [selectors/toggle opts]))))

(defn render-checkbox
  ([]
   (render-checkbox {}))
  ([opts]
   (rtl/render (reagent/as-element [selectors/checkbox opts]))))

(defn render-checkbox-prefill
  ([]
   (render-checkbox-prefill {}))
  ([opts]
   (rtl/render (reagent/as-element [selectors/checkbox-prefill opts]))))

(defn render-radio
  ([]
   (render-radio {}))
  ([opts]
   (rtl/render (reagent/as-element [selectors/radio opts]))))

(js/global.test "default render of toggle component"
                (fn []
                  (render-toggle)
                  (-> (js/expect (rtl/screen.getByTestId "toggle-component"))
                      (.toBeTruthy))))

(js/global.test "toggle component on change is working"
                (let [mock-fn (js/jest.fn)]
                  (fn []
                    (render-toggle {:on-change mock-fn})
                    (rtl/fireEvent.press (rtl/screen.getByTestId "toggle-component"))
                    (-> (js/expect mock-fn)
                        (.toHaveBeenCalledTimes 1)))))

(js/global.test "default render of radio component"
                (fn []
                  (render-radio)
                  (-> (js/expect (rtl/screen.getByTestId "radio-component"))
                      (.toBeTruthy))))

(js/global.test "radio component on change is working"
                (let [mock-fn (js/jest.fn)]
                  (fn []
                    (render-radio {:on-change mock-fn})
                    (rtl/fireEvent.press (rtl/screen.getByTestId "radio-component"))
                    (-> (js/expect mock-fn)
                        (.toHaveBeenCalledTimes 1)))))

(js/global.test "default render of checkbox component"
                (fn []
                  (render-checkbox)
                  (-> (js/expect (rtl/screen.getByTestId "checkbox-component"))
                      (.toBeTruthy))))

(js/global.test "checkbox component on change is working"
                (let [mock-fn (js/jest.fn)]
                  (fn []
                    (render-checkbox {:on-change mock-fn})
                    (rtl/fireEvent.press (rtl/screen.getByTestId "checkbox-component"))
                    (-> (js/expect mock-fn)
                        (.toHaveBeenCalledTimes 1)))))

(js/global.test "default render of checkbox-prefill component"
                (fn []
                  (render-checkbox-prefill)
                  (-> (js/expect (rtl/screen.getByTestId "checkbox-prefill-component"))
                      (.toBeTruthy))))

(js/global.test "checkbox-prefill component on change is working"
                (let [mock-fn (js/jest.fn)]
                  (fn []
                    (render-checkbox-prefill {:on-change mock-fn})
                    (rtl/fireEvent.press (rtl/screen.getByTestId "checkbox-prefill-component"))
                    (-> (js/expect mock-fn)
                        (.toHaveBeenCalledTimes 1)))))