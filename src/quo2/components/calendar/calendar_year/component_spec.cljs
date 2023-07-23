(ns quo2.components.calendar.calendar-year.component-spec
  (:require [quo2.components.calendar.calendar-year.view :as calendar-year]
            [test-helpers.component :as h]))

(h/describe "calendar-year component"
  (h/test "default render of calendar-year component"
    (h/render [calendar-year/calendar-year {} "2023"])
    (h/is-truthy (h/query-by-text "2023")))

  (h/test "should not call on-press when disabled"
    (let [on-press (h/mock-fn)]
      (h/render [calendar-year/calendar-year {:on-press on-press :disabled? true} "2023"])
      (h/fire-event :press (h/query-by-text "2023"))
      (h/was-not-called on-press))))
