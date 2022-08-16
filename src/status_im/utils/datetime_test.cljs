(ns status-im.utils.datetime-test
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.utils.datetime :as d]
            [status-im.goog.i18n :as i18n]
            [cljs-time.core :as t]))

(defn match [name symbols]
  (is (identical? (.-dateTimeSymbols_ (i18n/mk-fmt name #'status-im.utils.datetime/medium-date-format))
                  symbols)))

(deftest date-time-formatter-test
  (match "en-US" goog.i18n.DateTimeSymbols_en_US)
  (match "en-ZZ" goog.i18n.DateTimeSymbols_en)
  (match "en" goog.i18n.DateTimeSymbols_en)
  (match "nb-NO" goog.i18n.DateTimeSymbols_nb)
  (match "nb" goog.i18n.DateTimeSymbols_nb)
  (match "whoa-WHOA" goog.i18n.DateTimeSymbols_en)
  (match "whoa" goog.i18n.DateTimeSymbols_en))

;; 1970-01-01 00:00:00 UTC
(def epoch 0)
;; 1970-01-03 00:00:00 UTC
(def epoch-plus-3d 172800000)

(deftest is24Hour-locale-en-test
  (is (= (#'status-im.utils.datetime/is24Hour-locsym (i18n/locale-symbols "en")) false)))

(deftest is24Hour-locale-it-test
  (is (= (#'status-im.utils.datetime/is24Hour-locsym (i18n/locale-symbols "it")) true)))

(deftest is24Hour-locale-nb-test
  (is (= (#'status-im.utils.datetime/is24Hour-locsym (i18n/locale-symbols "nb-NO")) true)))

(deftest to-short-str-today-test
  (with-redefs [t/*ms-fn* (constantly epoch-plus-3d)
                d/time-fmt (fn [] (i18n/mk-fmt "us" #'status-im.utils.datetime/short-time-format))
                d/time-zone-offset (t/period :hours 0)]
    (is (= (d/to-short-str epoch-plus-3d) "12:00 AM"))))

#_((deftest to-short-str-today-force-24H-test
     (with-redefs [t/*ms-fn* (constantly epoch-plus-3d)
                   d/is24Hour (constantly true)
                   d/time-fmt (i18n-module/mk-fmt "us" d/short-time-format)
                   d/time-zone-offset (t/period :hours 0)]
       (is (= (d/to-short-str epoch-plus-3d) "00:00"))))

   (deftest to-short-str-today-force-AMPM-test
     (with-redefs [t/*ms-fn* (constantly epoch-plus-3d)
                   d/is24Hour (constantly false)
                   d/time-fmt (i18n-module/mk-fmt "it" d/short-time-format)
                   d/time-zone-offset (t/period :hours 0)]
       (is (= (d/to-short-str epoch-plus-3d) "12:00 AM")))))

(deftest to-short-str-before-yesterday-us-test
  (with-redefs [t/*ms-fn* (constantly epoch-plus-3d)
                d/time-zone-offset (t/period :hours 0)
                d/date-fmt (fn [] (i18n/mk-fmt "us" #'status-im.utils.datetime/medium-date-format))]
    (is (= (d/to-short-str epoch) "Jan 1, 1970"))))

(deftest to-short-str-before-yesterday-nb-test
  (with-redefs [d/time-zone-offset (t/period :hours 0)
                d/date-fmt (fn [] (i18n/mk-fmt "nb-NO" #'status-im.utils.datetime/medium-date-format))
                t/*ms-fn* (constantly epoch-plus-3d)]
    (is (= (d/to-short-str epoch) "1. jan. 1970"))))

(deftest day-relative-before-yesterday-us-test
  (with-redefs [t/*ms-fn* (constantly epoch-plus-3d)
                d/time-zone-offset (t/period :hours 0)
                d/date-fmt (fn [] (i18n/mk-fmt "us" #'status-im.utils.datetime/medium-date-time-format))]
    (is (= (d/day-relative epoch) "Jan 1, 1970, 12:00:00 AM"))))

(deftest day-relative-before-yesterday-nb-test
  (with-redefs [t/*ms-fn* (constantly epoch-plus-3d)
                d/time-zone-offset (t/period :hours 0)
                d/date-fmt (fn [] (i18n/mk-fmt "nb-NO" #'status-im.utils.datetime/medium-date-time-format))]
    (is (= (d/day-relative epoch) "1. jan. 1970, 00:00:00"))))

#_((deftest day-relative-before-yesterday-force-24H-test
     (with-redefs [t/*ms-fn* (constantly epoch-plus-3d)
                   d/is24Hour (constantly true)
                   d/time-zone-offset (t/period :hours 0)
                   d/date-fmt (i18n-module/mk-fmt "us" #'status-im.utils.datetime/medium-date-time-format)]
       (is (= (d/day-relative epoch) "Jan 1, 1970, 00:00:00"))))

   (deftest day-relative-before-yesterday-force-AMPM-test
     (with-redefs [t/*ms-fn* (constantly epoch-plus-3d)
                   d/is24Hour (constantly false)
                   d/time-zone-offset (t/period :hours 0)
                   d/date-fmt (i18n-module/mk-fmt "it" #'status-im.utils.datetime/medium-date-time-format)]
       (is (= (d/day-relative epoch) "01 gen 1970, 12:00:00 AM")))))
