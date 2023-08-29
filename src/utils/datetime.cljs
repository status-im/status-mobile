(ns utils.datetime
  (:require [cljs-time.coerce :as t.coerce]
            [cljs-time.core :as t]
            [goog.string :as gstring]
            [cljs-time.format :as t.format]
            [clojure.string :as string]
            [utils.i18n :as i18n]
            [utils.i18n-goog :as i18n-goog]))

(defn now [] (t/now))

(def weekday-names ["su" "mo" "tu" "we" "th" "fr" "sa"])

(def ^:const int->weekday
  "Maps the corresponding string representation of a weekday
   By it's numeric index as in cljs-time"
  {1 "mon"
   2 "tue"
   3 "wed"
   4 "thu"
   5 "fri"
   6 "sat"
   7 "sun"})

(def ^:const months
  "Maps the corresponding string representation of a weekday
   By it's numeric index as in cljs-time"
  {1  "jan"
   2  "feb"
   3  "mar"
   4  "apr"
   5  "may"
   6  "jun"
   7  "jul"
   8  "aug"
   9  "sep"
   10 "oct"
   11 "nov"
   12 "dec"})

(def one-second 1000)
(def minute (* 60 one-second))
(defn minutes [m] (* m minute))
(def hour (* 60 minute))
(def day (* 24 hour))
(def week (* 7 day))
(defn weeks [w] (* w week))
(def units
  [{:name :t/datetime-second-short :limit 60 :in-second 1}
   {:name :t/datetime-minute-short :limit 3600 :in-second 60}
   {:name :t/datetime-hour-short :limit 86400 :in-second 3600}
   {:name :t/datetime-day-short :limit nil :in-second 86400}])

(def time-zone-offset (t/hours (- (/ (.getTimezoneOffset ^js (js/Date.)) 60))))

;;;; Utilities
(defn- is-24-hour-locsym
  "Detects if given locale symbols timeformat generates AM/PM ('a')."
  [^js locsym]
  (not (string/includes?
        (nth (.-TIMEFORMATS locsym) 2)
        "a")))

(defn- is-24-hour
  "Returns is24Hour from device or from given locale symbols. Whenever we get
  non-nil value use it, else calculate it from the given locale symbol."
  [^js locsym]
  (is-24-hour-locsym locsym))

;;;; Time formats
(defn- short-time-format
  [^js locsym]
  (if (is-24-hour locsym)
    "HH:mm"
    "h:mm a"))

(defn- time-format
  [^js locsym]
  (if (is-24-hour locsym)
    "HH:mm:ss"
    "h:mm:ss a"))

;;;; Date formats
(defn- short-date-format [_] "dd MMM")
(defn- short-date-format-with-time [_] "dd MMM h:mm a")

(defn- datetime-within-one-week-format
  [^js locsym]
  (if (is-24-hour locsym)
    "E HH:mm"
    "E h:mm a"))

(defn- medium-date-format
  "Get medium format from current locale symbols."
  [^js locsym]
  (nth (.-DATEFORMATS locsym) 2))

(defn format-date
  [date]
  (if date
    (t.format/unparse (t.format/formatter "dd/MM/yyyy") date)
    ""))

;;;; Datetime formats
(defn- medium-date-time-format
  [locsym]
  (str (medium-date-format locsym) ", " (time-format locsym)))

(defn get-formatter-fn
  [format]
  (let [formatter (atom nil)]
    (fn []
      (or @formatter
          (reset! formatter
            (i18n-goog/mk-fmt i18n/locale format))))))

(def date-time-fmt (get-formatter-fn medium-date-time-format))
(def date-fmt (get-formatter-fn medium-date-format))
(def time-fmt (get-formatter-fn short-time-format))
(def short-date-fmt (get-formatter-fn short-date-format))
(def short-date-with-time-fmt (get-formatter-fn short-date-format-with-time))
(def datetime-within-one-week-fmt (get-formatter-fn datetime-within-one-week-format))

(def format-long-month
  (memoize (fn [month]
             (.format ^js ((get-formatter-fn (constantly "MMMM")))
                      (t/date-time 1970 month)))))
;;;; Utilities
(defn previous-years?
  [datetime]
  (< (t/year datetime) (t/year (t/now))))

(defn current-year?
  [datetime]
  (= (t/year datetime) (t/year (t/now))))

(defn today?
  [datetime]
  (let [time-now (t/now)]
    (and (= (t/year time-now) (t/year datetime))
         (= (t/month time-now) (t/month datetime))
         (= (t/day time-now) (t/day datetime)))))

(defn tomorrow?
  [datetime]
  (= (-> (t/now)
         (t/plus (t/days 1))
         t/day)
     (t/day datetime)))

(defn within-last-n-days?
  "Returns true if `datetime` is within last `n` days (inclusive on both ends)."
  [datetime n]
  (let [time-now (t/now)
        start    (t/at-midnight (t/minus time-now (t/days n)))
        end      (t/plus time-now (t/millis 1))]
    (t/within? start end datetime)))

;;;; Timestamp formatters
(defn- to-str
  [ms old-fmt-fn yesterday-fmt-fn today-fmt-fn]
  (let [date      (t.coerce/from-long ms)
        ;; NOTE(edge-case): this is wrong, it uses the current timezone offset, regardless of DST.
        local     (t/plus date time-zone-offset)
        today     (t/minus (t/today-at-midnight) time-zone-offset)
        yesterday (t/plus today (t/days -1))]
    (cond
      (t/before? date yesterday) (old-fmt-fn local)
      (t/before? date today)     (yesterday-fmt-fn local)
      :else                      (today-fmt-fn local))))

(defn to-short-str
  [ms]
  (to-str ms
          #(.format ^js (date-fmt) %)
          #(i18n/label :t/datetime-yesterday)
          #(.format ^js (time-fmt) %)))

(defn day-relative
  [ms]
  (to-str ms
          #(.format ^js (date-fmt) %)
          #(i18n/label :t/datetime-yesterday)
          #(i18n/label :t/datetime-today)))

(defn timestamp->relative
  [ms]
  (let [datetime (-> ms
                     t.coerce/from-long
                     (t/plus time-zone-offset))]
    (cond
      (today? datetime)
      (str (string/capitalize (i18n/label :t/datetime-today))
           " "
           (.format ^js (time-fmt) datetime))

      (within-last-n-days? datetime 1)
      (str (string/capitalize (i18n/label :t/datetime-yesterday))
           " "
           (.format ^js (time-fmt) datetime))

      (within-last-n-days? datetime 6)
      (.format ^js (datetime-within-one-week-fmt) datetime)

      (current-year? datetime)
      (.format ^js (short-date-with-time-fmt) datetime)

      (previous-years? datetime)
      (.format ^js (date-fmt) datetime))))

(defn timestamp->mini-date
  [ms]
  (.format ^js (short-date-fmt)
           (-> ms
               t.coerce/from-long
               (t/plus time-zone-offset))))

(defn timestamp->time
  [ms]
  (.format ^js (time-fmt)
           (-> ms
               t.coerce/from-long
               (t/plus time-zone-offset))))

(defn timestamp->date-key
  [ms]
  (keyword (t.format/unparse (t.format/formatter "YYYYMMDD")
                             (-> ms
                                 t.coerce/from-long
                                 (t/plus time-zone-offset)))))

(defn timestamp->long-date
  [ms]
  (.format ^js (date-time-fmt)
           (-> ms
               t.coerce/from-long
               (t/plus time-zone-offset))))

(defn format-time-ago
  [diff unit]
  (let [name (i18n/label-pluralize diff (:name unit))]
    (if (= :t/datetime-second-short (:name unit))
      (i18n/label :t/now)
      (i18n/label :t/datetime-ago-format-short
                  {:ago            (i18n/label :t/datetime-ago)
                   :number         diff
                   :time-intervals name}))))
(defn seconds-ago
  [date-time]
  (let [time-now (t/now)]
    (if (<= (.getTime ^js date-time) (.getTime ^js time-now))
      (t/in-seconds (t/interval date-time time-now))
      0)))

(defn time-ago
  [date-time]
  (let [diff (seconds-ago date-time)
        unit (first (drop-while #(and (>= diff (:limit %))
                                      (:limit %))
                                units))]
    (-> (/ diff (:in-second unit))
        Math/floor
        int
        (format-time-ago unit))))

(defn time-ago-long
  [date-time]
  (let [time-ago-seconds (seconds-ago date-time)
        unit             (first (drop-while #(and (>= time-ago-seconds (:limit %))
                                                  (:limit %))
                                            units))
        diff             (-> (/ time-ago-seconds (:in-second unit))
                             Math/floor
                             int)

        name             (i18n/label-pluralize diff (:name unit))]
    (i18n/label :t/datetime-ago-format
                {:ago            (i18n/label :t/datetime-ago)
                 :number         diff
                 :time-intervals name})))

(defn to-date
  [ms]
  (t.coerce/from-long ms))

(defn timestamp
  []
  (inst-ms (js/Date.)))

(defn timestamp-sec
  []
  (int (/ (timestamp) 1000)))

(defn timestamp->year-month-day-date
  [ms]
  (t.format/unparse (:year-month-day t.format/formatters) (to-date ms)))

(defn to-ms
  [sec]
  (* 1000 sec))

(defn ms-to-duration
  "miliseconds to mm:ss format"
  [ms]
  (let [sec (quot ms 1000)]
    (gstring/format "%02d:%02d" (quot sec 60) (mod sec 60))))

(def ^:const go-default-time
  "Zero value for golang's time var"
  "0001-01-01T00:00:00Z")
