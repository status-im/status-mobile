(ns status-im.utils.datetime
  (:require [cljs-time.core :as t :refer [date-time now plus days hours before?]]
            [cljs-time.coerce :refer [from-long to-long]]
            [cljs-time.format :refer [formatters
                                      formatter
                                      unparse]]
            [status-im.i18n :refer [label label-pluralize]]))

(def hour (* 1000 60 60))
(def day (* hour 24))
(def week (* 7 day))
(def units [{:name (label :t/datetime-second) :limit 60 :in-second 1}
            {:name (label :t/datetime-minute) :limit 3600 :in-second 60}
            {:name (label :t/datetime-hour) :limit 86400 :in-second 3600}
            {:name (label :t/datetime-day) :limit nil :in-second 86400}])

(def time-zone-offset (hours (- (/ (.getTimezoneOffset (js/Date.)) 60))))

(defn to-short-str [ms]
  (let [date       (from-long ms)
        local      (plus date time-zone-offset)
        today-date (t/today)
        today      (date-time (t/year today-date)
                              (t/month today-date)
                              (t/day today-date))
        yesterday  (plus today (days -1))]
    (cond
      (before? local yesterday) (unparse (formatter "dd MMM") local)
      (before? local today)     (label :t/datetime-yesterday)
      :else                     (unparse (formatters :hour-minute) local))))

(defn time-ago [time]
  (let [diff (t/in-seconds (t/interval time (t/now)))]
    (if (< diff 60)
      (label :t/active-online)
      (let [unit (first (drop-while #(or (>= diff (:limit %))
                                         (not (:limit %)))
                                    units))]
        (-> (/ diff (:in-second unit))
            Math/floor
            int
            (#(str % " " (label-pluralize % (:name unit)) " " (label :t/datetime-ago))))))))

(defn to-date [ms]
  (from-long ms))

(defn now-ms []
  (to-long (now)))
