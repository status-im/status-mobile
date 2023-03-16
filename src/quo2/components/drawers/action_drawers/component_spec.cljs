(ns quo2.components.drawers.action-drawers.component-spec
  (:require ["@testing-library/react-native" :as rtl]
            [quo2.components.drawers.action-drawers.view :as action-drawer]
            [reagent.core :as reagent]))

(defn render-action-drawer
  ([options]
   (rtl/render (reagent/as-element [action-drawer/action-drawer options]))))

(js/global.test "action-drawer renders with elements label displaying"
  (fn []
    (render-action-drawer [[{:icon  :i/friend
                             :label "a sample label"}]])
    (-> (js/expect (rtl/screen.getByText "a sample label"))
        (.toBeTruthy))))

(js/global.test "action-drawer renders with elements sub-label displaying"
  (fn []
    (render-action-drawer [[{:icon      :i/friend
                             :label     "a sample label"
                             :sub-label "a sample sub label"}]])
    (-> (js/expect (rtl/screen.getByText "a sample sub label"))
        (.toBeTruthy))))

(js/global.test "action-drawer on click action works on element"
  (let [event (js/jest.fn)]
    (fn []
      (render-action-drawer [[{:icon     :i/friend
                               :label    "a sample label"
                               :on-press event}]])
      (rtl/fireEvent.press (rtl/screen.getByText "a sample label"))
      (-> (js/expect event)
          (.toHaveBeenCalled)))))

(js/global.test "action-drawer renders two icons when set"
  (fn []
    (render-action-drawer [[{:icon                :i/friend
                             :label               "a sample label"
                             :right-icon          :i/friend
                             :accessibility-label :first-element}]])
    (-> (js/expect (rtl/screen.getByLabelText "right-icon-for-action"))
        (.toBeTruthy))
    (-> (js/expect (rtl/screen.queryByLabelText "left-icon-for-action"))
        (.toBeTruthy))))

(js/global.test "action-drawer renders a divider when the add-divider? prop is true"
  (fn []
    (render-action-drawer [[{:icon                :i/friend
                             :label               "a sample label"
                             :add-divider?        true
                             :accessibility-label :first-element}]])
    (-> (js/expect (rtl/screen.getAllByLabelText "divider"))
        (.toBeTruthy))))
