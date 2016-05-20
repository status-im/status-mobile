(ns syng-im.utils.datetime
  (:require [cljs-time.core :as t :refer [date-time now plus days hours before?]]
            [cljs-time.coerce :refer [from-long to-long]]
            [cljs-time.format :as format :refer [formatters
                                                 formatter
                                                 unparse]]))

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
      (before? local today)     "Yesterday"
      :else                     (unparse (formatters :hour-minute) local))))

(defn now-ms []
  (to-long (now)))
