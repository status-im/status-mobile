(ns utils.datetime-test
  (:require [cljs-time.coerce :as time-coerce]
            [cljs-time.core :as t]
            [cljs.test :refer-macros [deftest testing is]]
            [status-im.goog.i18n :as i18n]
            status-im2.setup.datetime
            [utils.datetime :as datetime]))

(defn match
  [name symbols]
  (is (identical? (.-dateTimeSymbols_ (i18n/mk-fmt name #'utils.datetime/medium-date-format))
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

(deftest is-24-hour-locale-en-test
  (is (= (#'utils.datetime/is-24-hour-locsym (i18n/locale-symbols "en")) false)))

(deftest is-24-hour-locale-it-test
  (is (= (#'utils.datetime/is-24-hour-locsym (i18n/locale-symbols "it")) true)))

(deftest is-24-hour-locale-nb-test
  (is (= (#'utils.datetime/is-24-hour-locsym (i18n/locale-symbols "nb-NO")) true)))

(deftest to-short-str-today-test
  (with-redefs [t/*ms-fn*          (constantly epoch-plus-3d)
                datetime/time-fmt         (fn []
                                     (i18n/mk-fmt "us" #'utils.datetime/short-time-format))
                datetime/time-zone-offset (t/period :hours 0)]
    (is (= (datetime/to-short-str epoch-plus-3d) "12:00 AM"))))

#_((deftest to-short-str-today-force-24H-test
     (with-redefs [t/*ms-fn*          (constantly epoch-plus-3d)
                   datetime/is-24-hour       (constantly true)
                   datetime/time-fmt         (i18n-module/mk-fmt "us" datetime/short-time-format)
                   datetime/time-zone-offset (t/period :hours 0)]
       (is (= (datetime/to-short-str epoch-plus-3d) "00:00"))))

   (deftest to-short-str-today-force-AMPM-test
     (with-redefs [t/*ms-fn*          (constantly epoch-plus-3d)
                   datetime/is-24-hour       (constantly false)
                   datetime/time-fmt         (i18n-module/mk-fmt "it" datetime/short-time-format)
                   datetime/time-zone-offset (t/period :hours 0)]
       (is (= (datetime/to-short-str epoch-plus-3d) "12:00 AM")))))

(deftest to-short-str-before-yesterday-us-test
  (with-redefs [t/*ms-fn*          (constantly epoch-plus-3d)
                datetime/time-zone-offset (t/period :hours 0)
                datetime/date-fmt         (fn []
                                     (i18n/mk-fmt "us" #'utils.datetime/medium-date-format))]
    (is (= (datetime/to-short-str epoch) "Jan 1, 1970"))))

(deftest to-short-str-before-yesterday-nb-test
  (with-redefs [datetime/time-zone-offset (t/period :hours 0)
                datetime/date-fmt         (fn []
                                     (i18n/mk-fmt "nb-NO" #'utils.datetime/medium-date-format))
                t/*ms-fn*          (constantly epoch-plus-3d)]
    (is (= (datetime/to-short-str epoch) "1. jan. 1970"))))

(deftest day-relative-before-yesterday-us-test
  (with-redefs [t/*ms-fn*          (constantly epoch-plus-3d)
                datetime/time-zone-offset (t/period :hours 0)
                datetime/date-fmt         (fn []
                                     (i18n/mk-fmt "us"
                                                  #'utils.datetime/medium-date-time-format))]
    (is (= (datetime/day-relative epoch) "Jan 1, 1970, 12:00:00 AM"))))

(deftest day-relative-before-yesterday-nb-test
  (with-redefs [t/*ms-fn*          (constantly epoch-plus-3d)
                datetime/time-zone-offset (t/period :hours 0)
                datetime/date-fmt         (fn []
                                     (i18n/mk-fmt "nb-NO"
                                                  #'utils.datetime/medium-date-time-format))]
    (is (= (datetime/day-relative epoch) "1. jan. 1970, 00:00:00"))))

(deftest current-year?-test
  ;; Today is Monday, 1975-03-10 15:15:45Z
  (with-redefs [t/*ms-fn*          (constantly 163696545000)
                datetime/time-zone-offset (t/period :hours 0)]
    (is (datetime/current-year? (t/now)))

    (testing "returns false for future years"
      (is (not (datetime/current-year? (t/plus (t/now) (t/years 1))))))

    (testing "returns true at 1975-01-01 00:00:00"
      (is (datetime/current-year? (time-coerce/from-long 157766400000))))

    (testing "returns false at 1974-12-31 23:59:59"
      (is (not (datetime/current-year? (time-coerce/from-long 157766399000)))))))

(deftest previous-years?-test
  ;; Today is Monday, 1975-03-10 15:15:45Z
  (with-redefs [t/*ms-fn*          (constantly 163696545000)
                datetime/time-zone-offset (t/period :hours 0)]
    (is (not (datetime/previous-years? (t/now))))

    (testing "returns false for future years"
      (is (not (datetime/current-year? (t/plus (t/now) (t/years 1))))))

    (testing "returns false at 1975-01-01 00:00:00"
      (is (not (datetime/previous-years? (time-coerce/from-long 1640995200000)))))

    (testing "returns true at 1974-12-31 23:59:59"
      (is (not (datetime/previous-years? (time-coerce/from-long 1640995199000)))))))

(deftest within-last-n-days?-test
  ;; Today is Monday, 1975-03-10 15:15:45Z
  (let [now 163696545000]
    (with-redefs [t/*ms-fn*          (constantly now)
                  datetime/time-zone-offset (t/period :hours 0)]
      (testing "start of the period, 6 days ago (inclusive)"
        ;; Tuesday, 1975-03-03 23:59:59Z
        (is (not (datetime/within-last-n-days? (time-coerce/from-long 163123199000) 6)))

        ;; Tuesday, 1975-03-04 00:00:00Z
        (is (datetime/within-last-n-days? (time-coerce/from-long 163123200000) 6))

        ;; Tuesday, 1975-03-04 00:00:01Z
        (is (datetime/within-last-n-days? (time-coerce/from-long 163123201000) 6)))

      (testing "end of the period (inclusive)"
        ;; Monday, 1975-03-10 15:15:44Z
        (is (datetime/within-last-n-days? (time-coerce/from-long 163696544000) 6))

        ;; Monday, 1975-03-10 15:15:45Z
        (is (datetime/within-last-n-days? (time-coerce/from-long now) 6))

        ;; Monday, 1975-03-10 15:15:46Z
        (is (not (datetime/within-last-n-days? (time-coerce/from-long 163696546000) 6)))))))

(deftest timestamp->relative-test
  ;; Today is Monday, 1975-03-10 15:15:45Z
  (with-redefs [t/*ms-fn*          (constantly 163696545000)
                datetime/time-zone-offset (t/period :hours 0)
                datetime/is-24-hour       (constantly false)]
    (testing "formats previous years"
      ;; 1974-12-31 23:59:59Z
      (is (= "Dec 31, 1974" (datetime/timestamp->relative 157766399000)))
      ;; 1973-01-01 00:00:00Z
      (is (= "Jan 1, 1973" (datetime/timestamp->relative 94694400000))))

    (testing "formats 7 days ago or older, but in the current year"
      (is (= "03 Mar" (datetime/timestamp->relative 163091745000)))
      (is (= "02 Mar" (datetime/timestamp->relative 163004400000)))
      (is (= "01 Jan" (datetime/timestamp->relative 157820400000))))

    (testing "formats dates within the last 6 days"
      (is (= "Sat 3:15 PM" (datetime/timestamp->relative 163523745000)))
      (is (= "Fri 3:15 PM" (datetime/timestamp->relative 163437345000)))
      (is (= "Thu 3:15 PM" (datetime/timestamp->relative 163350945000)))
      (is (= "Wed 3:15 PM" (datetime/timestamp->relative 163264545000)))
      (is (= "Tue 3:15 PM" (datetime/timestamp->relative 163178145000))))

    (testing "formats within yesterday window"
      (is (= "Yesterday 3:15 PM" (datetime/timestamp->relative 163610145000)))
      (is (= "Yesterday 11:59 PM" (datetime/timestamp->relative 163641599000))))

    (testing "formats today, at various timestamps"
      (is (= "3:15 PM" (datetime/timestamp->relative 163696545000)))
      (is (= "12:00 PM" (datetime/timestamp->relative 163684800000)))
      (is (= "12:00 AM" (datetime/timestamp->relative 163641600000))))))

#_((deftest day-relative-before-yesterday-force-24H-test
     (with-redefs [t/*ms-fn*          (constantly epoch-plus-3d)
                   datetime/is-24-hour       (constantly true)
                   datetime/time-zone-offset (t/period :hours 0)
                   datetime/date-fmt         (i18n-module/mk-fmt
                                       "us"
                                       #'utils.datetime/medium-date-time-format)]
       (is (= (datetime/day-relative epoch) "Jan 1, 1970, 00:00:00"))))

   (deftest day-relative-before-yesterday-force-AMPM-test
     (with-redefs [t/*ms-fn*          (constantly epoch-plus-3d)
                   datetime/is-24-hour       (constantly false)
                   datetime/time-zone-offset (t/period :hours 0)
                   datetime/date-fmt         (i18n-module/mk-fmt
                                       "it"
                                       #'utils.datetime/medium-date-time-format)]
       (is (= (datetime/day-relative epoch) "01 gen 1970, 12:00:00 AM")))))
