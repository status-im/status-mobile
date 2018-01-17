(ns status-im.test.utils.datetime
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.datetime :as datetime]))

(def timestamp
  (.getTime (new js/Date "2017-12-28 20:17:36")))

(deftest datetime
  (testing "moment locale conversion"
    (is (= "de" (datetime/->moment-locale "de")))
    (is (= "de-CH" (datetime/->moment-locale "de-CH")))
    (is (= "zh-HK" (datetime/->moment-locale "zh-Hant-HK")))
    (is (= "en" (datetime/->moment-locale "na-NA")))
    (is (= "en" (datetime/->moment-locale nil))))

  (testing "Localized short date time format"
    (with-redefs [datetime/format-locale-datetime (datetime/get-locale-datetime-formatter "zh-Hant-HK")]
      (is (= "2017年12月28日 20:17" (datetime/to-short-str timestamp)))))

  (testing "Localized mini date time format"
    (with-redefs [datetime/format-locale-datetime (datetime/get-locale-datetime-formatter "de-CH")]
      (is (= "28 Dez." (datetime/timestamp->mini-date timestamp)))))

  (testing "Localized long date time format"
    (with-redefs [datetime/format-locale-datetime (datetime/get-locale-datetime-formatter "ru")]
      (is (= (keyword "28 дек. 2017 г. 20:17:36") (datetime/timestamp->long-date timestamp))))))
