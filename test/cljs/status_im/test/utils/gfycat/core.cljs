(ns status-im.test.utils.gfycat.core
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.utils.gfycat.core :as gfy]))

(deftest test-generate-gfy
  (is (= (gfy/generate-gfy "57348975ff9199ca636207a396b915d6b6a675b4") "Woeful Glorious Mosquito"))
  (is (= (gfy/generate-gfy "e63d083d2d7a13a14632983b5820529445ca8109") "Midnightblue Neighboring Waterdogs"))
  (is (= (gfy/generate-gfy "57348975ff9199ca636207a396b915d6b6a675b4") "Woeful Glorious Mosquito"))
  (is (= (gfy/generate-gfy "26cf649aebb252a75aebc588e0d9ce93789dbe0b") "Faint Urban Arcticseal"))
  (is (= (gfy/generate-gfy "e63d083d2d7a13a14632983b5820529445ca8109") "Midnightblue Neighboring Waterdogs")))
