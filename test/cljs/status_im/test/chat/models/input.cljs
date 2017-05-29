(ns status-im.test.chat.models.input
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.chat.models.input :as in]))

(deftest test-split-command-args
  (is (= [""] (in/split-command-args nil)))
  (is (= ["@browse" "google.com"] (in/split-command-args "@browse google.com")))
  (is (= ["@browse" "google.com"] (in/split-command-args "  @browse   google.com  "))))
