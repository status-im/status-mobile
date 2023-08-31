(ns quo2.components.navigation.top-nav.component-spec
  (:require [quo2.components.navigation.top-nav.view :as top-nav]
            [test-helpers.component :as h]))

(h/describe "Top Nav component"
  (h/test "Renders default"
    (h/render [top-nav/view])
    (h/is-truthy (h/get-by-label-text :open-scanner-button))
    (h/is-truthy (h/get-by-label-text :open-activity-center-button))
    (h/is-truthy (h/get-by-label-text :show-qr-button))
    (h/is-truthy (h/get-by-label-text :open-profile)))

  (h/test "On press works for all buttons and avatar"
    (let [avatar-on-press          (h/mock-fn)
          scan-on-press            (h/mock-fn)
          activity-center-on-press (h/mock-fn)
          qr-code-on-press         (h/mock-fn)]

      (h/render [top-nav/view
                 {:avatar-on-press          avatar-on-press
                  :scan-on-press            scan-on-press
                  :activity-center-on-press activity-center-on-press
                  :qr-code-on-press         qr-code-on-press}])

      (h/fire-event :press (h/get-by-label-text :open-scanner-button))
      (h/was-called scan-on-press)

      (h/fire-event :press (h/get-by-label-text :open-activity-center-button))
      (h/was-called activity-center-on-press)

      (h/fire-event :press (h/get-by-label-text :show-qr-button))
      (h/was-called qr-code-on-press)

      (h/fire-event :press (h/get-by-label-text :open-profile))
      (h/was-called avatar-on-press))))


