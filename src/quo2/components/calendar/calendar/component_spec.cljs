(ns quo2.components.calendar.calendar.component-spec
  (:require [quo2.components.calendar.calendar.view :as calendar]
            [test-helpers.component :as h]
            [cljs-time.core :as t]))

(def start-date (t/date-time (t/year (t/now)) (t/month (t/now)) 5))
(def end-date (t/date-time (t/plus start-date (t/days 2))))

(h/describe "calendar component"
  (h/test "default render of calendar component"
    (h/render
     [calendar/calendar
      {:start-date start-date
       :end-date   end-date}])
    (-> (h/expect (h/query-by-translation-text "Mo"))
        (h/is-truthy)))

  (h/test "should call on-change with selected date on first click"
    (let [on-change (h/mock-fn)]
      (h/render
       [calendar/calendar
        {:start-date nil
         :end-date   nil
         :on-change  on-change}])
      (h/fire-event :press (h/query-by-text (str (t/day start-date))))
      (h/was-called-with on-change {:start-date start-date :end-date nil})))

  (h/test "should call on-change with start and end date on second click"
    (let [on-change (h/mock-fn)]
      (h/render
       [calendar/calendar
        {:start-date start-date :end-date nil :on-change on-change}])
      (h/fire-event :press (h/query-by-text (str (t/day end-date))))
      (h/was-called-with on-change {:start-date start-date :end-date end-date})))

  (h/test "should reset the dates on third click"
    (let [on-change (h/mock-fn)]
      (h/render
       [calendar/calendar
        {:start-date start-date
         :end-date   end-date
         :on-change  on-change}])
      (h/fire-event :press (h/query-by-text (str (t/day start-date))))
      (h/was-called-with on-change {:start-date start-date :end-date nil})))

  (h/test "should reset dates when start date is clicked again"
    (let [on-change (h/mock-fn)]
      (h/render
       [calendar/calendar
        {:start-date start-date
         :end-date   nil
         :on-change  on-change}])
      (h/fire-event :press (h/query-by-text (str (t/day start-date))))
      (h/was-called-with on-change {:start-date nil :end-date nil})))


  (h/test "should assign start and end date correctly when upper range selected first"
    (let [on-change (h/mock-fn)]
      (h/render
       [calendar/calendar
        {:start-date end-date
         :end-date   nil
         :on-change  on-change}])
      (h/fire-event :press (h/query-by-text (str (t/day start-date))))
      (h/was-called-with on-change {:start-date start-date :end-date end-date}))))
