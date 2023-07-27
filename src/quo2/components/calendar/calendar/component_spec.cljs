(ns quo2.components.calendar.calendar.component-spec
  (:require [quo2.components.calendar.calendar.view :as calendar]
            [test-helpers.component :as h]
            [cljs-time.core :as time]))

(def start-date (time/date-time (time/year (time/now)) (time/month (time/now)) 5))
(def end-date (time/date-time (time/plus start-date (time/days 2))))

(h/describe "calendar component"
  (h/test "default render of calendar component"
    (h/render
     [calendar/view
      {:start-date start-date
       :end-date   end-date}])
    (-> (h/expect (h/query-by-translation-text "Mo"))
        (h/is-truthy)))

  (h/test "should call on-change with selected date on first click"
    (let [on-change (h/mock-fn)]
      (h/render
       [calendar/view
        {:start-date nil
         :end-date   nil
         :on-change  on-change}])
      (h/fire-event :press (h/query-by-text (str (time/day start-date))))
      (h/was-called-with on-change {:start-date start-date :end-date nil})))

  (h/test "should call on-change with start and end date on second click"
    (let [on-change (h/mock-fn)]
      (h/render
       [calendar/view
        {:start-date start-date :end-date nil :on-change on-change}])
      (h/fire-event :press (h/query-by-text (str (time/day end-date))))
      (h/was-called-with on-change {:start-date start-date :end-date end-date})))

  (h/test "should reset the dates on third click"
    (let [on-change (h/mock-fn)]
      (h/render
       [calendar/view
        {:start-date start-date
         :end-date   end-date
         :on-change  on-change}])
      (h/fire-event :press (h/query-by-text (str (time/day start-date))))
      (h/was-called-with on-change {:start-date start-date :end-date nil})))

  (h/test "should reset dates when start date is clicked again"
    (let [on-change (h/mock-fn)]
      (h/render
       [calendar/view
        {:start-date start-date
         :end-date   nil
         :on-change  on-change}])
      (h/fire-event :press (h/query-by-text (str (time/day start-date))))
      (h/was-called-with on-change {:start-date nil :end-date nil})))


  (h/test "should assign start and end date correctly when upper range selected first"
    (let [on-change (h/mock-fn)]
      (h/render
       [calendar/view
        {:start-date end-date
         :end-date   nil
         :on-change  on-change}])
      (h/fire-event :press (h/query-by-text (str (time/day start-date))))
      (h/was-called-with on-change {:start-date start-date :end-date end-date}))))
