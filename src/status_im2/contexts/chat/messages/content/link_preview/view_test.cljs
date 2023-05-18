(ns status-im2.contexts.chat.messages.content.link-preview.view-test
  (:require [status-im2.contexts.chat.messages.content.link-preview.view :as view]
            [cljs.test :refer [is deftest are]]))

(deftest nearly-square?-test
  (are [pred width height] (is (pred (view/nearly-square? {:width width :height height})))
   false? 0   0
   true?  1   1
   false? 100 89
   false? 100 90
   true?  100 91
   true?  100 92
   true?  100 101
   true?  100 109
   false? 100 110
   false? 100 111))
