(ns status-im.utils.datetime
  (:require [cljs-time.core :as t :refer [date-time plus days hours before?]]
            [cljs-time.coerce :refer [from-long to-long from-date]]
            [cljs-time.format :refer [formatters
                                      formatter
                                      unparse]]
            [status-im.i18n :refer [label label-pluralize]]
            [status-im.native-module.core :as status]
            [goog.string :as gstring]
            goog.string.format
            goog.i18n.DateTimeFormat
            [clojure.string :as s]
            [goog.object :refer [get]]))

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

;; xx-YY locale, xx locale or en fallback
(defn- locale-symbols [locale-name]
  (if-let [loc (get goog.i18n (str "DateTimeSymbols_" locale-name))]
    loc
    (let [name-first (s/replace (or locale-name "") #"-.*$" "")
          loc (get goog.i18n (str "DateTimeSymbols_" name-first))]
      (or loc goog.i18n.DateTimeSymbols_en))))

;; detects if given locale symbols timeformat generates AM/PM ("a")
(defn- is24Hour-locsym [locsym]
  (not (s/includes?
        (nth (get locsym 'TIMEFORMATS) 2)
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
(defn- short-date-format [locsym] "dd MMM")
(defn- medium-date-format [locsym] (nth (get locsym 'DATEFORMATS) 2)) ; get medium format from current locale symbols

;; date-time formats
(defn- medium-date-time-format [locsym] (str (medium-date-format locsym) ", " (time-format locsym)))

;; get formatter for current locale symbols and format function
(defn- mk-fmt [locale format-fn]
  (let [locsym (locale-symbols locale)]
    (goog.i18n.DateTimeFormat. (format-fn locsym) locsym)))

;; generate formatters for different formats
(def date-time-fmt
  (mk-fmt status-im.i18n/locale medium-date-time-format))
(def date-fmt
  (mk-fmt status-im.i18n/locale medium-date-format))
(def time-fmt
  (mk-fmt status-im.i18n/locale short-time-format))
(def short-date-fmt
  (mk-fmt status-im.i18n/locale short-date-format))

;;
;; functions which apply formats for the given timestamp
;;

(defn- to-str [ms old-fmt-fn yesterday-fmt-fn today-fmt-fn]
  (let [date (from-long ms)
        local (plus date time-zone-offset) ; this is wrong, it uses the current timezone offset, regardless of DST
        today (t/today-at-midnight)
        yesterday (plus today (days -1))]
    (cond
      (before? date yesterday) (old-fmt-fn local)
      (before? date today) (yesterday-fmt-fn local)
      :else (today-fmt-fn local))))

(defn to-short-str [ms]
  (to-str ms
          #(.format date-fmt %)
          #(label :t/datetime-yesterday)
          #(.format time-fmt %)))

(defn day-relative [ms]
  (to-str ms
          #(.format date-fmt %)
          #(label :t/datetime-yesterday)
          #(label :t/datetime-today)))

(defn timestamp->mini-date [ms]
  (.format short-date-fmt (-> ms
                              from-long
                              (plus time-zone-offset))))

(defn timestamp->time [ms]
  (.format time-fmt (-> ms
                        from-long
                        (plus time-zone-offset))))

(defn timestamp->date-key [ms]
  (keyword (unparse (formatter "YYYYMMDD") (-> ms
                                               from-long
                                               (plus time-zone-offset)))))

(defn timestamp->long-date [ms]
  (.format date-time-fmt  (-> ms
                              from-long
                              (plus time-zone-offset))))

(defn format-time-ago [diff unit]
  (let [name (label-pluralize diff (:name unit))]
    (label :t/datetime-ago-format {:ago (label :t/datetime-ago)
                                   :number diff
                                   :time-intervals name})))
(defn seconds-ago [time]
  (t/in-seconds (t/interval time (t/now))))

(defn time-ago [time]
  (let [diff (seconds-ago time)]
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

(defn timestamp []
  (inst-ms (js/Date.)))

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
