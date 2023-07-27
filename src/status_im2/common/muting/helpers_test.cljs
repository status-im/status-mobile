(ns status-im2.common.muting.helpers-test
  (:require [cljs-time.coerce :as time-coerce]
            [cljs-time.core :as t]
            [cljs-time.format :as t.format]
            [cljs.test :refer-macros [deftest testing is are]]
            [clojure.string :as string]
            [utils.datetime :as datetime]
            [status-im2.common.muting.helpers :as muting]))

(def mock-current-time-epoch 1655731506000)

(deftest format-mute-till-test
  (with-redefs [t/*ms-fn* (constantly mock-current-time-epoch)]
    (let [remove-msecs              #(string/replace % #"\.\w*Z" "Z")
          time-str-to-obj           #(t.format/parse (remove-msecs (time-coerce/to-string %)))
          curr-time                 (t/now)
          custom-HH-MM-formatter    (t.format/formatter "HH:mm")
          custom-DD-formatter       (t.format/formatter "DD")
          get-hh-mm                 #(t.format/unparse custom-HH-MM-formatter %)
          get-day                   #(t.format/unparse custom-DD-formatter %)
          get-week-day              #(->> %
                                          t/day-of-week
                                          (get datetime/int->weekday))
          mock-today                (t.format/unparse (t.format/formatters :date-time-no-ms) curr-time)
          in-n-days                 #(-> (time-str-to-obj mock-today)
                                         (t/plus (t/days %)))
          in-n-minutes              #(-> (time-str-to-obj mock-today)
                                         (t/plus (t/minutes %)))
          in-n-hours                #(-> (time-str-to-obj mock-today)
                                         (t/plus (t/hours %)))
          mock-tomorrow             (in-n-days 1)
          mock-in-two-days          (in-n-days 2)
          mock-in-three-days        (in-n-days 3)
          mock-in-four-days         (in-n-days 4)
          mock-in-five-days         (in-n-days 5)
          mock-in-six-days          (in-n-days 6)
          mock-in-15-minutes        (in-n-minutes 15)
          mock-in-1-hour            (in-n-hours 1)
          mock-in-8-hour            (in-n-hours 8)
          get-month-day-int         #(int (get-day %))
          today?                    (fn [mocked curr-time]
                                      (=
                                       (t.format/unparse (t.format/formatter "MM:DD") mocked)
                                       (t.format/unparse (t.format/formatter "MM:DD") curr-time)))
          tomorrow?                 (fn [mocked curr-time]
                                      (some #(= %
                                                (-
                                                 (int (get-month-day-int mocked))
                                                 (int (get-month-day-int curr-time))))
                                            [1 30 29 27]))
          form-full-date            #(str (get-hh-mm %)
                                          " " (string/capitalize (get-week-day %))
                                          " " (get-month-day-int %)
                                          " " (string/capitalize (get datetime/months (t/month %))))
          today-date                #(str (get-hh-mm %) " today")
          tomorrow-date             #(str (get-hh-mm %) " tomorrow")
          write-date                #(cond (today? % curr-time)    (today-date %)
                                           (tomorrow? % curr-time) (tomorrow-date %)
                                           :else                   (form-full-date %))
          will-unmute-in-1-hour     (remove-msecs (time-coerce/to-string mock-in-1-hour))
          will-unmute-in-8-hours    (remove-msecs (time-coerce/to-string mock-in-8-hour))
          will-unmute-in-15-mins    (remove-msecs (time-coerce/to-string mock-in-15-minutes))
          will-unmute-in-two-days   (remove-msecs (time-coerce/to-string mock-in-two-days))
          will-unmute-tomorrow      (remove-msecs (time-coerce/to-string mock-tomorrow))
          will-unmute-in-three-days (remove-msecs (time-coerce/to-string mock-in-three-days))
          will-unmute-in-four-days  (remove-msecs (time-coerce/to-string mock-in-four-days))
          will-unmute-in-five-days  (remove-msecs (time-coerce/to-string mock-in-five-days))
          will-unmute-in-six-days   (remove-msecs (time-coerce/to-string mock-in-six-days))]
      (testing "Mute for minutes and hours"
        (are [arg expected] (= (muting/format-mute-till arg) expected)
         will-unmute-in-15-mins (write-date mock-in-15-minutes)
         will-unmute-in-1-hour  (write-date mock-in-1-hour)
         will-unmute-in-8-hours (write-date mock-in-8-hour)))
      (testing "Weekdays"
        (are [arg expected] (= (muting/format-mute-till arg) expected)
         will-unmute-tomorrow      (write-date mock-tomorrow)
         will-unmute-in-two-days   (write-date mock-in-two-days)
         will-unmute-in-three-days (write-date mock-in-three-days)
         will-unmute-in-four-days  (write-date mock-in-four-days)
         will-unmute-in-five-days  (write-date mock-in-five-days)
         will-unmute-in-six-days   (write-date mock-in-six-days)))
      (testing "Until the user turns it back on"
        (is (= "you turn it back on" (muting/format-mute-till datetime/go-default-time)))))))
