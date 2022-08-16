(ns status-im.utils.security-html-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.security-html :as s]))

(deftest with-doctype
  (is (s/is-html? "<!doctype html>"))
  (is (s/is-html? "\n\n<!doctype html><html>")))

(deftest body-html-tags
  (testing "detect HTML if it has <html>, <body> or <x-*>"
    (is (s/is-html? "<html>"))
    (is (s/is-html? "<html></html>"))
    (is (s/is-html? "<html lang=\"en\"></html>"))
    (is (s/is-html? "<html><body></html>"))
    (is (s/is-html? "<html><body class=\"no-js\"></html>"))
    (is (s/is-html? "<x-unicorn>"))))

(deftest html-standard-tags
  (testing "detect HTML if it contains any of the standard HTML tags"
    (is (s/is-html? "<p>foo</p>"))
    (is (s/is-html? "<a href=\"#\">foo</a>"))))

(deftest not-matching-xml
  (is (not (s/is-html? "<cake>foo</cake>")))
  (is (not (s/is-html? "<any>rocks</any>")))
  (is (not (s/is-html? "<htmly>not</htmly>")))
  (is (not (s/is-html? "<bodyx>not</bodyx>"))))
