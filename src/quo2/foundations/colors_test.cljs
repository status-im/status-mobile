(ns quo2.foundations.colors-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [quo2.foundations.colors :as colors]))

(deftest test-color-resolver
  (testing "community color - resolves a hex string and ignores suffix of 50"
    (is (= "#fff" (colors/custom-color "#fff" 50))))
  (testing "community color - resolves a hex string and ignores suffix of 60"
    (is (= "#fff" (colors/custom-color "#fff" 60))))
  (testing "user/wallet/chat colors - resolves a keyword from the customization map with suffix 50"
    (is (= (get-in colors/customization [:blue 50]) (colors/custom-color :blue 50))))
  (testing "user/wallet/chat colors - resolves a keyword from the colors map with suffix 60"
    (is (= (get-in colors/customization [:blue 60]) (colors/custom-color :blue 60))))
  (testing "network colors - resolves a keyword with from the networks map which has no suffix"
    (is (= (:ethereum colors/networks) (colors/custom-color :ethereum nil)))))

(deftest test-color-resolver-with-opacity
  (testing "community color with 10% opacity- resolves a hex string and ignores suffix of 50"
    (is (= "rgba(255,15,NaN,0.1)" (colors/custom-color "#fff" 50 10))))
  (testing "community color with 10% opacity- resolves a hex string and ignores suffix of 50"
    (is (= "rgba(255,15,NaN,0.9)" (colors/custom-color "#fff" 50 90))))
  (testing
    "user/wallet/chat colors with 10% opacity - resolves a keyword from the colors map with suffix 50"
    (is (= "rgba(42,74,245,0.1)" (colors/custom-color :blue 50 10))))
  (testing
    "network colors with 10% opacity - resolves a keyword with from the networks map which has no suffix"
    (is (= "rgba(117,142,235,0.1)" (colors/custom-color :ethereum nil 10)))))
