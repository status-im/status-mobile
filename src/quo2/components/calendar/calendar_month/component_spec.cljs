(ns quo2.components.calendar.calendar-month.component-spec
  (:require [quo2.components.calendar.calendar-month.view :as calendar-month]
            [test-helpers.component :as h]))

(h/describe "calendar-month component"
  (h/test "default render of calendar-month component"
    (h/render
     [calendar-month/calendar-month
      {:year "2023" :month "7"}])
    (-> (h/expect (h/query-by-translation-text "July 2023"))
        (h/is-truthy)))

  (h/test "should call on-change with next month when right button pressed"
    (let [on-change (h/mock-fn)]
      (h/render
       [calendar-month/calendar-month {:year "2023" :month "7" :on-change on-change}])
      (h/fire-event :press (h/query-by-label-text :next-month-button))
      (h/was-called on-change)))

  (h/test "should call on-change with previous month when left button pressed"
    (let [on-change (h/mock-fn)]
      (h/render
       [calendar-month/calendar-month {:year "2023" :month "1" :on-change on-change}])
      (h/fire-event :press (h/query-by-label-text :previous-month-button))
      (h/was-called on-change))))
