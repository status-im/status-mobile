(ns quo2.components.drawers.action-drawers.component-spec
  (:require [quo2.components.drawers.action-drawers.view :as action-drawer]
            [test-helpers.component :as h]))

(h/describe "action drawer"
  (h/test "default render"
    (h/render [action-drawer/action-drawer
               [[{:icon  :i/friend
                  :label "a sample label"}]]])
    (h/is-truthy (h/get-by-text "a sample label")))

  (h/test "renders with elements sub-label displaying"
    (h/render [action-drawer/action-drawer
               [[{:icon      :i/friend
                  :label     "a sample label"
                  :sub-label "a sample sub label"}]]])
    (h/is-truthy (h/get-by-text "a sample sub label")))

  (h/test "on click action works on element"
    (let [event (js/jest.fn)]
      (h/render [action-drawer/action-drawer
                 [[{:icon     :i/friend
                    :label    "a sample label"
                    :on-press event}]]])
      (h/fire-event :press (h/get-by-text "a sample label"))
      (h/was-called event)))

  (h/test "renders two icons when set"
    (h/render [action-drawer/action-drawer
               [[{:icon                :i/friend
                  :label               "a sample label"
                  :right-icon          :i/friend
                  :accessibility-label :first-element}]]])
    (h/is-truthy (h/get-by-label-text "right-icon-for-action"))
    (h/is-truthy (h/query-by-label-text "left-icon-for-action")))

  (h/test "renders a divider when the add-divider? prop is true"
    (h/render [action-drawer/action-drawer
               [[{:icon                :i/friend
                  :label               "a sample label"
                  :add-divider?        true
                  :accessibility-label :first-element}]]])
    (h/is-truthy (h/get-all-by-label-text "divider"))))
