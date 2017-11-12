(ns status-im.test.utils.gfycat.core
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.utils.gfycat.core :as gfy]))

(def message "Names are only deterministic as long as word lists do not change!\nIf you change the lists, please change the tests as well.")

(deftest test-generate-gfy
  (is (= (gfy/generate-gfy "57348975ff9199ca636207a396b915d6b6a675b4") "Winged Fitting Mosquito") message)
  (is (= (gfy/generate-gfy "e63d083d2d7a13a14632983b5820529445ca8109") "Mediumvioletred Melodic Waterdogs") message)
  (is (= (gfy/generate-gfy "57348975ff9199ca636207a396b915d6b6a675b4") "Winged Fitting Mosquito") message)
  (is (= (gfy/generate-gfy "26cf649aebb252a75aebc588e0d9ce93789dbe0b") "Educated Upright Arcticseal") message)
  (is (= (gfy/generate-gfy "e63d083d2d7a13a14632983b5820529445ca8109") "Mediumvioletred Melodic Waterdogs") message)
  (is (= (gfy/generate-gfy nil) gfy/unknown-gfy) message)
  (is (= (gfy/generate-gfy "0") gfy/unknown-gfy) message))
