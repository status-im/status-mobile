(ns status-im.test.chat.styles.message
  (:require [cljs.test :refer [deftest is]]
            [status-im.ui.screens.chat.styles.message.message :as style]))

(deftest anim-initial-translateX
  (is (= -134.625
         (style/anim-initial-translateX 375 0.25 {:align-items :flex-start :padding-left 8})))
  (is (= 134.625
         (style/anim-initial-translateX 375 0.25 {:align-items :flex-end :padding-right 8})))
  (is (= 187.5
         (style/anim-initial-translateX 375 0 {:align-items :flex-end})))
  (is (= 0
         (style/anim-initial-translateX 375 0.25 {}))))

(deftest anim-initial-translateY
  (is (= 70
         (style/anim-initial-translateY 140 0 {})))
  (is (= 40.5
         (style/anim-initial-translateY 140 0.25 {:padding-bottom 16}))))
