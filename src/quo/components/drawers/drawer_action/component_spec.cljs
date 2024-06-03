(ns quo.components.drawers.drawer-action.component-spec
  (:require
    [quo.components.drawers.drawer-action.view :as drawer-action]
    [quo.foundations.colors :as colors]
    [test-helpers.component :as h]))

(h/describe "Drawers: drawer-action"
  (h/test "default render"
    (h/render-with-theme-provider [drawer-action/view {:accessibility-label :container}])
    (h/is-truthy (h/query-by-label-text :container)))

  (h/test "on-press-in changes internal state to :pressed"
    (h/render-with-theme-provider [drawer-action/view {:accessibility-label :container}])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor (colors/resolve-color :blue :light 5)})))

  (h/test "render default action with state :selected"
    (h/render-with-theme-provider [drawer-action/view
                                   {:state :selected :accessibility-label :container}])
    (h/has-style (h/query-by-label-text :container)
                 {:backgroundColor (colors/resolve-color :blue :light 5)})
    (h/is-truthy (h/query-by-label-text :check-icon)))

  (h/test "call on-press"
    (let [on-press (h/mock-fn)]
      (h/render-with-theme-provider [drawer-action/view
                                     {:on-press on-press :accessibility-label :container}])
      (h/fire-event :on-press (h/get-by-label-text :container))
      (h/was-called on-press)))


  (h/test "render :arrow action"
    (h/render-with-theme-provider [drawer-action/view {:action :arrow}])
    (h/is-truthy (h/query-by-label-text :arrow-icon)))

  (h/test "render :toggle action"
    (h/render-with-theme-provider [drawer-action/view {:action :toggle}])
    (h/is-truthy (h/query-by-label-text "toggle-off")))

  (h/test "render :toggle action with state :selected"
    (h/render-with-theme-provider [drawer-action/view
                                   {:accessibility-label :container
                                    :action              :toggle
                                    :state               :selected}])
    (h/is-truthy (h/query-by-label-text "toggle-on"))
    (h/has-style (h/query-by-label-text :container)
                 {:backgroundColor :transparent}))

  (h/test "render default action with icon, title, description"
    (h/render-with-theme-provider [drawer-action/view
                                   {:icon        :i/contact
                                    :title       "Check contact"
                                    :description "Just a small desc"}])
    (h/is-truthy (h/query-by-label-text :left-icon))
    (h/is-truthy (h/query-by-text "Check contact"))
    (h/has-style (h/query-by-text "Check contact") {:color colors/neutral-100})
    (h/is-truthy (h/query-by-text "Just a small desc"))))
