(ns status-im.utils.datetime
  (:require [re-frame.core :as re-frame]
            [cljs-time.core :as t :refer [plus minus days hours before?]]
            [cljs-time.coerce :refer [from-long]]
            [cljs-time.format :refer [formatters
                                      formatter
                                      unparse]]
            [status-im.i18n.i18n :refer [label label-pluralize]]
            [status-im.native-module.core :as status]
            [clojure.string :as s]
            [status-im.goog.i18n :as goog.18n]))

;;;; Datetime constants

(defn now []
  (t/now))

(def one-second 1000)
(def minute (* 60 one-second))
(defn minutes [m]
  (* m minute))
(def hour (* 60 minute))
(def day (* 24 hour))
(def week (* 7 day))
(defn weeks [w]
  (* w week))
(def units [{:name :t/datetime-second-short :limit 60 :in-second 1}
            {:name :t/datetime-minute-short :limit 3600 :in-second 60}
            {:name :t/datetime-hour-short :limit 86400 :in-second 3600}
            {:name :t/datetime-day-short :limit nil :in-second 86400}])

(def time-zone-offset (hours (- (/ (.getTimezoneOffset ^js (js/Date.)) 60))))

;;;; Utilities

(defn- is24Hour-locsym
  "Detects if given locale symbols timeformat generates AM/PM ('a')."
  [^js locsym]
  (not (s/includes?
        (nth (.-TIMEFORMATS locsym) 2)
        "a")))

(defn- is24Hour
  "Returns is24Hour from device or from given locale symbols. Whenever we get
  non-nil value use it, else calculate it from the given locale symbol."
  [^js locsym]
  (if-some [fromdev (status/is24Hour)]
    fromdev
    (is24Hour-locsym locsym)))

;;;; Time formats

(defn- short-time-format
  [^js locsym]
  (if (is24Hour locsym)
    "HH:mm"
    "h:mm a"))

(defn- time-format
  [^js locsym]
  (if (is24Hour locsym)
    "HH:mm:ss"
    "h:mm:ss a"))

;;;; Date formats

(defn- short-date-format [_] "dd MMM")

(defn- datetime-within-one-week-format
  [^js locsym]
  (if (is24Hour locsym)
    "E HH:mm"
    "E h:mm a"))

(defn- medium-date-format
  "Get medium format from current locale symbols."
  [^js locsym]
  (nth (.-DATEFORMATS locsym) 2))

;;;; Datetime formats

(defn- medium-date-time-format [locsym]
  (str (medium-date-format locsym) ", " (time-format locsym)))

(defn get-formatter-fn [format]
  (let [formatter (atom nil)]
    (fn []
      (or @formatter
          (reset! formatter
                  (goog.18n/mk-fmt status-im.i18n.i18n/locale format))))))

(def date-time-fmt (get-formatter-fn medium-date-time-format))
(def date-fmt (get-formatter-fn medium-date-format))
(def time-fmt (get-formatter-fn short-time-format))
(def short-date-fmt (get-formatter-fn short-date-format))
(def datetime-within-one-week-fmt (get-formatter-fn datetime-within-one-week-format))

;;;; Timestamp formatters

(defn- to-str [ms old-fmt-fn yesterday-fmt-fn today-fmt-fn]
  (let [date (from-long ms)
        local (plus date time-zone-offset) ; this is wrong, it uses the current timezone offset, regardless of DST
        today (minus (t/today-at-midnight) time-zone-offset)
        yesterday (plus today (days -1))]
    (cond
      (before? date yesterday) (old-fmt-fn local)
      (before? date today) (yesterday-fmt-fn local)
      :else (today-fmt-fn local))))

(defn to-short-str [ms]
  (to-str ms
          #(.format ^js (date-fmt) %)
          #(label :t/datetime-yesterday)
          #(.format ^js (time-fmt) %)))

(defn day-relative [ms]
  (to-str ms
          #(.format ^js (date-fmt) %)
          #(label :t/datetime-yesterday)
          #(label :t/datetime-today)))

(defn timestamp->relative [ms]
  (let [datetime       (from-long ms)
        datetime-local (plus datetime time-zone-offset)
        today          (minus (t/today-at-midnight) time-zone-offset)
        yesterday      (minus today (days 1))
        six-days-ago   (minus today (days 6))]
    (cond
      ;; Previous years.
      (< (t/year datetime) (t/year today))
      (.format ^js (date-fmt) datetime-local)

      ;; Current year.
      (before? datetime six-days-ago)
      (.format ^js (short-date-fmt) datetime-local)

      ;; Within 6 days window.
      (before? datetime yesterday)
      (.format ^js (datetime-within-one-week-fmt) datetime-local)

      ;; Yesterday
      (before? datetime today)
      (str (s/capitalize (label :t/datetime-yesterday))
           " "
           (.format ^js (time-fmt) datetime-local))

      ;; Today
      :else
      (.format ^js (time-fmt) datetime-local))))

(defn timestamp->mini-date [ms]
  (.format ^js (short-date-fmt) (-> ms
                                    from-long
                                    (plus time-zone-offset))))

(defn timestamp->time [ms]
  (.format ^js (time-fmt) (-> ms
                              from-long
                              (plus time-zone-offset))))

(defn timestamp->date-key [ms]
  (keyword (unparse (formatter "YYYYMMDD") (-> ms
                                               from-long
                                               (plus time-zone-offset)))))

(defn timestamp->long-date [ms]
  (.format ^js (date-time-fmt) (-> ms
                                   from-long
                                   (plus time-zone-offset))))

(defn format-time-ago [diff unit]
  (let [name (label-pluralize diff (:name unit))]
    (if (= :t/datetime-second-short (:name unit))
      (label :t/now)
      (label :t/datetime-ago-format-short {:ago (label :t/datetime-ago)
                                           :number diff
                                           :time-intervals name}))))
(defn seconds-ago [time]
  (let [now (t/now)]
    (if (<= (.getTime ^js time) (.getTime ^js now))
      (t/in-seconds (t/interval time now))
      0)))

(defn time-ago [time]
  (let [diff (seconds-ago time)
        unit (first (drop-while #(and (>= diff (:limit %))
                                      (:limit %))
                                units))]
    (-> (/ diff (:in-second unit))
        Math/floor
        int
        (format-time-ago unit))))

(defn time-ago-long [time]
  (let [seconds-ago (seconds-ago time)
        unit (first (drop-while #(and (>= seconds-ago (:limit %))
                                      (:limit %))
                                units))
        diff  (-> (/ seconds-ago (:in-second unit))
                  Math/floor
                  int)

        name (label-pluralize diff (:name unit))]
    (label :t/datetime-ago-format {:ago (label :t/datetime-ago)
                                   :number diff
                                   :time-intervals name})))

(defn to-date [ms]
  (from-long ms))

(defn timestamp []
  (inst-ms (js/Date.)))

(defn timestamp-sec []
  (int (/ (timestamp) 1000)))

(defn timestamp->year-month-day-date [ms]
  (unparse (:year-month-day formatters) (to-date ms)))

(re-frame/reg-cofx
 :now
 (fn [coeffects _]
   (assoc coeffects :now (timestamp))))

(defn to-ms [sec]
  (* 1000 sec))
