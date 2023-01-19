(ns status-im.chat.models.input-test
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.chat.models.input :as input]))

(deftest text->emoji
  (is (nil? (input/text->emoji nil)))
  (is (= "" (input/text->emoji "")))
  (is (= "test" (input/text->emoji "test")))
  (is (= "word1 \uD83D\uDC4D word2" (input/text->emoji "word1 :+1: word2"))))
