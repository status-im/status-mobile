(ns quo.components.banners.alert-banner.component-spec
  (:require
    [quo.components.banners.alert-banner.view :as alert-banner]
    [test-helpers.component :as h]))

(h/describe "Alert Banner"
  (h/test "Render without props is not throwing any error"
    (h/render [alert-banner/view {}])
    (h/is-truthy (h/query-by-label-text :alert-banner)))

  (h/test "Button is not displayed when :action? prop is false"
    (h/render [alert-banner/view {}])
    (h/is-falsy (h/query-by-label-text :button)))

  (h/test "Button is displayed when :action? prop is true"
    (h/render [alert-banner/view {:action? true}])
    (h/is-truthy (h/query-by-label-text :button)))

  (h/test "Button text is displayed when :action? prop is true and :button-text prop is set"
    (h/render [alert-banner/view
               {:action?     true
                :button-text "button"}])
    (h/is-truthy (h/get-by-text "button")))

  (h/test "Button is called when it's pressed and :action? prop is true"
    (let [event (h/mock-fn)]
      (h/render [alert-banner/view
                 {:action?         true
                  :on-button-press event}])
      (h/fire-event :press (h/query-by-label-text :button))
      (h/was-called event)))

  (h/test "Text message is displayed :text prop is passed"
    (h/render [alert-banner/view {:text "message"}])
    (h/is-truthy (h/get-by-text "message"))))
