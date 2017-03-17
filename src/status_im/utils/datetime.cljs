(ns status-im.utils.datetime
  (:require [cljs-time.core :as t :refer [date-time plus days hours before?]]
            [cljs-time.coerce :refer [from-long to-long from-date]]
            [cljs-time.format :refer [formatters
                                      formatter
                                      unparse]]
            [status-im.i18n :refer [label label-pluralize]]
            [goog.string :as gstring]
            goog.string.format))

(defn now []
  (t/now))

(def hour (* 1000 60 60))
(def day (* hour 24))
(def week (* 7 day))
(def units [{:name :t/datetime-second :limit 60 :in-second 1}
            {:name :t/datetime-minute :limit 3600 :in-second 60}
            {:name :t/datetime-hour :limit 86400 :in-second 3600}
            {:name :t/datetime-day :limit nil :in-second 86400}])

(def time-zone-offset (hours (- (/ (.getTimezoneOffset (js/Date.)) 60))))

(defn to-short-str
  ([ms]
   (to-short-str ms #(unparse (formatters :hour-minute) %)))
  ([ms today-format-fn]
   (let [date       (from-long ms)
         local      (plus date time-zone-offset)
         today-date (t/today)
         today      (date-time (t/year today-date)
                               (t/month today-date)
                               (t/day today-date))
         yesterday  (plus today (days -1))]
     (cond
       (before? local yesterday) (unparse (formatter "dd MMM hh:mm") local)
       (before? local today) (label :t/datetime-yesterday)
       :else (today-format-fn local)))))

(defn day-relative [ms]
  (when (pos? ms)
    (to-short-str ms #(label :t/datetime-today))))

(defn format-time-ago [diff unit]
  (let [name (label-pluralize diff (:name unit))]
    (gstring/format "%s %s %s" diff name (label :t/datetime-ago))))

(defn time-ago [time]
  (let [diff (t/in-seconds (t/interval time (t/now)))]
    (if (< diff 60)
      (label :t/active-online)
      (let [unit (first (drop-while #(and (>= diff (:limit %))
                                          (:limit %))
                                    units))]
        (-> (/ diff (:in-second unit))
            Math/floor
            int
            (format-time-ago unit))))))

(defn to-date [ms]
  (from-long ms))

(defn now-ms []
  (to-long (now)))

(defn format-date [format date]
  (let [local (plus (from-date date) time-zone-offset)]
    (unparse (formatter format) local)))

(defn get-ordinal-date [date]
  (let [local (plus (from-date date) time-zone-offset)
        day   (js/parseInt (unparse (formatter "d") local))
        s     {0 "th"
               1 "st"
               2 "nd"
               3 "rd"}
        m     (mod day 100)]
    (str day (or (s (mod (- m 20) 10))
                 (s m)
                 (s 0)))))
