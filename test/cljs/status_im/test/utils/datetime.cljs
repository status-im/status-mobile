(ns status-im.test.utils.datetime
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.utils.datetime :as d]
            [cljs-time.core :as t]))

(defn match [name symbols]
  (is (identical? (.-dateTimeSymbols_ (d/mk-fmt name d/medium-date-format))
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

(deftest to-short-str-today-test
  (with-redefs [t/*ms-fn* (constantly epoch-plus-3d)
                d/time-fmt (d/mk-fmt "us" d/short-time-format)
                d/time-zone-offset (t/period :hours 0)]
   (is (= (d/to-short-str epoch-plus-3d) "12:00 AM"))))

(deftest to-short-str-before-yesterday-us-test
  (with-redefs [t/*ms-fn* (constantly epoch-plus-3d)
                d/time-zone-offset (t/period :hours 0)
                d/date-fmt (d/mk-fmt "us" d/medium-date-format)]
    (is (= (d/to-short-str epoch) "Jan 1, 1970"))))

(deftest to-short-str-before-yesterday-nb-test
  (with-redefs [d/time-zone-offset (t/period :hours 0)
                d/date-fmt (d/mk-fmt "nb-NO" d/medium-date-format)
                t/*ms-fn* (constantly epoch-plus-3d)]
    (is (= (d/to-short-str epoch) "1. jan. 1970"))))

(deftest day-relative-before-yesterday-us-test
  (with-redefs [t/*ms-fn* (constantly epoch-plus-3d)
                d/time-zone-offset (t/period :hours 0)
                d/date-fmt (d/mk-fmt "us" d/medium-date-time-format)]
    (is (= (d/day-relative epoch) "Jan 1, 1970, 12:00:00 AM"))))

(deftest day-relative-before-yesterday-nb-test
  (with-redefs [t/*ms-fn* (constantly epoch-plus-3d)
                d/time-zone-offset (t/period :hours 0)
                d/date-fmt (d/mk-fmt "nb-NO" d/medium-date-time-format)]
    (is (= (d/day-relative epoch) "1. jan. 1970, 00:00:00"))))
