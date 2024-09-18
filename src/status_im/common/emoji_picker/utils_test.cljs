(ns status-im.common.emoji-picker.utils-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [status-im.common.emoji-picker.utils :as utils]))

(deftest emoji-search-test
  (testing "search for emojis with name"
    (let [search-input "dolphin"
          expected     `(({:group   3
                           :hexcode "1f42c"
                           :label   "dolphin"
                           :tags    ["flipper"]
                           :unicode "🐬"}))]
      (is (= expected (utils/search-emoji search-input)))))

  (testing "search for emojis with emoticon"
    (let [search-input "<3"
          expected     `(({:group    0
                           :hexcode  "2764"
                           :label    "red heart"
                           :tags     ["heart"]
                           :unicode  "❤️"
                           :emoticon "<3"}))]
      (is (= expected (utils/search-emoji search-input)))))

  (testing "search for emojis with tag"
    (let [search-input "tada"
          expected     `(({:group   6
                           :hexcode "1f389"
                           :label   "party popper"
                           :tags    ["celebration" "party" "popper" "tada"]
                           :unicode "🎉"}))]
      (is (= expected (utils/search-emoji search-input)))))

  (testing "search for emojis with tag"
    (let [search-input "raven"
          expected     `(({:group   3
                           :hexcode "1f426-200d-2b1b"
                           :label   "black bird"
                           :tags    ["bird" "black" "crow" "raven" "rook"]
                           :unicode "🐦‍⬛"}))]
      (is (= expected (utils/search-emoji search-input))))))
