(ns quo2.components.info.information-box.component-spec
  (:require [quo2.components.info.information-box.view :as view]
            [test-helpers.component :as h]))

(h/describe "Info - Information Box"
  (h/test "default render"
    (h/render [view/view {:icon :i/placeholder}
               "Lorem ipsum"])
    (h/is-null (h/query-by-label-text :information-box-action-button))
    (h/is-null (h/query-by-label-text :information-box-close-button)))

  (h/test "with close button"
    (let [on-close (h/mock-fn)]
      (h/render [view/view
                 {:icon     :i/placeholder
                  :on-close on-close}
                 "Lorem ipsum"])
      (h/is-null (h/query-by-label-text :information-box-action-button))
      (h/fire-event :on-press (h/get-by-label-text :information-box-close-button))
      (h/was-called on-close)))

  (h/test "with button"
    (let [on-press (h/mock-fn)]
      (h/render [view/view
                 {:icon            :i/placeholder
                  :button-label    "Press me"
                  :on-button-press on-press}
                 "Lorem ipsum"])
      (h/fire-event :on-press (h/get-by-label-text :information-box-action-button))
      (h/was-called on-press))))
