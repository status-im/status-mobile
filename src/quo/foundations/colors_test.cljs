(ns quo.foundations.colors-test
  (:require
    [cljs.test :refer-macros [deftest testing is]]
    [quo.foundations.colors :as colors]))

(deftest resolve-color-test
  (testing "community color - resolves a hex string and ignores theme"
    (is (= "#fff" (colors/resolve-color "#fff" :light))))
  (testing "community color - resolves a hex string and ignores theme"
    (is (= "#fff" (colors/resolve-color "#fff" :dark))))
  (testing "user/wallet/chat colors - resolves a keyword from the customization map with light theme"
    (is (= "#2A4AF5" (colors/resolve-color :blue :light))))
  (testing "user/wallet/chat colors - resolves a keyword from the colors map with dark theme"
    (is (= "#223BC4" (colors/resolve-color :blue :dark))))
  (testing "network colors - resolves a keyword with from the networks map"
    (is (= "#758EEB" (colors/resolve-color :ethereum :light)))))

(deftest resolve-color-with-opacity-test
  (testing "community color with 10% opacity- resolves a hex string and ignores theme"
    (is (= "rgba(255,15,NaN,0.1)" (colors/resolve-color "#fff" :light 10))))
  (testing "community color with 10% opacity- resolves a hex string and ignores theme"
    (is (= "rgba(255,15,NaN,0.9)" (colors/resolve-color "#fff" :light 90))))
  (testing
    "user/wallet/chat colors with 10% opacity - resolves a keyword from the colors map with light theme"
    (is (= "rgba(42,74,245,0.1)" (colors/resolve-color :blue :light 10))))
  (testing
    "user/wallet/chat colors with 10% opacity - resolves a keyword from the colors map with dark theme"
    (is (= "rgba(42,74,245,0.1)" (colors/resolve-color :blue :dark 10))))
  (testing
    "when using opacity theme is ignored and uses the light suffix resolver"
    (is (colors/resolve-color :blue :light 10) (colors/resolve-color :blue :dark 10))))

(deftest convert-hex-of-length-6-to-rgb
  (testing "Test powderblue conversion"
    (is (= "rgb(176, 224, 230)" (colors/hex->rgba "#B0E0E6"))))
  (testing "Test slateblue conversion"
    (is (= "rgb(106, 90, 205)" (colors/hex->rgba "#6A5ACD"))))
  (testing "Test slateblue with opacity 10% conversion"
    (is (= "rgba(106, 90, 205, 0.1)" (colors/hex->rgba "#6A5ACD" 0.1))))
  (testing "Test navy with opacity 0.66 conversion"
    (is (= "rgba(0, 0, 128, 0.66)" (colors/hex->rgba "#000080" 0.66))))
  (testing "Failing conversion"
    (is (not= "rgb(137, 43, 226)" (colors/hex->rgba "#8A2BE2")))))

(deftest test-account-colors-customization
  (is (every? #(contains? colors/customization %) colors/account-colors)))
