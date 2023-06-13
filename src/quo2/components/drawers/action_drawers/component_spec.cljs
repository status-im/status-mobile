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
    (let [on-press (js/jest.fn)]
      (h/render [action-drawer/action-drawer
                 [[{:icon     :i/friend
                    :label    "a sample label"
                    :on-press on-press}]]])
      (h/fire-event :press (h/get-by-text "a sample label"))
      (h/was-called on-press)))

  (h/test "disabled state"
    (let [on-press-muted  (js/jest.fn)
          on-press-friend (js/jest.fn)
          label-mute      "Mute"
          label-friend    "View member details"]
      (h/render [action-drawer/action-drawer
                 [[{:icon     :i/muted
                    :label    label-mute
                    :on-press on-press-muted}
                   {:icon      :i/friend
                    :label     label-friend
                    :disabled? true
                    :on-press  on-press-friend}]]])
      (h/fire-event :press (h/get-by-text label-mute))
      (h/was-called on-press-muted)

      (h/fire-event :press (h/get-by-text label-friend))
      (h/was-not-called on-press-friend)))

  (h/test "renders two icons when set"
    (h/render [action-drawer/action-drawer
               [[{:icon                :i/friend
                  :label               "a sample label"
                  :right-icon          :i/friend
                  :accessibility-label :first-element}]]])
    (h/is-truthy (h/query-by-label-text :right-icon-for-action))
    (h/is-truthy (h/query-by-label-text :left-icon-for-action)))

  (h/test "renders right text besides icon when non-nil"
    (h/render [action-drawer/action-drawer
               [[{:icon       :i/friend
                  :label      "a sample label"
                  :right-text "20+"
                  :right-icon :i/friend}]]])
    (h/is-truthy (h/query-by-label-text :right-text-for-action)))

  (h/test "does not render right text when not present"
    (h/render [action-drawer/action-drawer
               [[{:icon       :i/friend
                  :label      "a sample label"
                  :right-icon :i/friend}]]])
    (h/is-null (h/query-by-label-text :right-text-for-action)))

  (h/test "renders a divider when the add-divider? prop is true"
    (h/render [action-drawer/action-drawer
               [[{:icon                :i/friend
                  :label               "a sample label"
                  :add-divider?        true
                  :accessibility-label :first-element}]]])
    (h/is-truthy (h/get-all-by-label-text "divider"))))
