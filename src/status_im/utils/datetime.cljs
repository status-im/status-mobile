(ns status-im.utils.datetime
  (:require [re-frame.core :as re-frame]
            [cljs-time.core :as t :refer [plus minus days hours before?]]
            [cljs-time.coerce :refer [from-long from-date]]
            [cljs-time.format :refer [formatters
                                      formatter
                                      unparse]]
            [status-im.i18n.i18n :refer [label label-pluralize]]
            [status-im.native-module.core :as status]
            [clojure.string :as s]
            [status-im.goog.i18n :as goog.18n]))

(defn now []
  (t/now))

(def one-second 1000)
(def minute (* 60 one-second))
(defn minutes [m]
  (* m minute))
(def hour (* 60 minute))
(def day (* 24 hour))
(def week (* 7 day))
(def units [{:name :t/datetime-second-short :limit 60 :in-second 1}
            {:name :t/datetime-minute-short :limit 3600 :in-second 60}
            {:name :t/datetime-hour-short :limit 86400 :in-second 3600}
            {:name :t/datetime-day-short :limit nil :in-second 86400}])

(def time-zone-offset (hours (- (/ (.getTimezoneOffset ^js (js/Date.)) 60))))

;; detects if given locale symbols timeformat generates AM/PM ("a")
(defn- is24Hour-locsym [^js locsym]
  (not (s/includes?
        (nth (.-TIMEFORMATS locsym) 2)
        "a")))

;; returns is24Hour from device or from given locale symbols
;; whenever we get non-nil value use it, else calculate it from the given locale symbol
(defn- is24Hour [locsym]
  (if-some [fromdev (status/is24Hour)]
    fromdev
    (is24Hour-locsym locsym)))

;; time formats
(defn- short-time-format [locsym] (if (is24Hour locsym) "HH:mm" "h:mm a"))
(defn- time-format [locsym] (if (is24Hour locsym) "HH:mm:ss" "h:mm:ss a"))

;; date formats
(defn- short-date-format [_] "dd MMM")
(defn- medium-date-format [^js locsym] (nth (.-DATEFORMATS locsym) 2)) ; get medium format from current locale symbols

;; date-time formats
(defn- medium-date-time-format [locsym]
  (str (medium-date-format locsym) ", " (time-format locsym)))

(defn get-formatter-fn [format]
  (let [formatter (atom nil)]
    (fn []
      (or @formatter
          (reset! formatter
                  (goog.18n/mk-fmt status-im.i18n.i18n/locale format))))))

;; generate formatters for different formats
(def date-time-fmt
  (get-formatter-fn medium-date-time-format))
(def date-fmt
  (get-formatter-fn medium-date-format))
(def time-fmt
  (get-formatter-fn short-time-format))
(def short-date-fmt
  (get-formatter-fn short-date-format))

;;
;; functions which apply formats for the given timestamp
;;

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

(defn to-date [ms]
  (from-long ms))

(defn timestamp []
  (inst-ms (js/Date.)))

(defn timestamp->year-month-day-date [ms]
  (unparse (:year-month-day formatters) (to-date ms)))

(re-frame/reg-cofx
 :now
 (fn [coeffects _]
   (assoc coeffects :now (timestamp))))

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
