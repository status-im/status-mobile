(ns quo2.components.calendar.calendar-day.component-spec
  (:require [quo2.components.calendar.calendar-day.view :as calendar-day]
            [test-helpers.component :as h]))

(h/describe "calendar-day component"
  (h/test "default render of calendar-day component"
    (h/render [calendar-day/calendar-day {} "25"])
    (h/is-truthy (h/query-by-text "25")))

  (h/test "should not call on-press when state is :disabled"
    (let [on-press (h/mock-fn)]
      (h/render [calendar-day/calendar-day {:on-press on-press :state :disabled} "25"])
      (h/fire-event :press (h/query-by-text "25"))
      (h/was-not-called on-press))))
