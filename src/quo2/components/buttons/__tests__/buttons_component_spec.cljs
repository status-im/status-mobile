(ns quo2.components.buttons.--tests--.buttons-component-spec
  (:require ["@testing-library/react-native" :as rtl]
            [quo2.components.buttons.button :as button]
            [reagent.core :as reagent]))

(defn render-button
  ([options label]
   (rtl/render (reagent/as-element  [button/button options label]))))

(js/global.test "default render of button component"
                (fn []
                  (render-button {:accessibility-label "test-button"} "")
                  (-> (js/expect  (rtl/screen.getByLabelText  "test-button"))
                      (.toBeTruthy))))

(js/global.test "button renders with a label"
                (fn []
                  (render-button {} "test-label")
                  (-> (js/expect  (rtl/screen.getByText  "test-label"))
                      (.toBeTruthy))))

(js/global.test "button on-press works"
                (let [event (js/jest.fn)]
                  (fn []
                    (render-button {:on-press event} "test-label")
                    (rtl/fireEvent.press (rtl/screen.getByText  "test-label"))
                    (-> (js/expect  event)
                        (.toHaveBeenCalledTimes 1)))))
