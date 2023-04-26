(ns native-module.core-test
  (:require [cljs.test :refer [deftest is testing]]
            [native-module.core :as native-module]))

(deftest identicon-test
  (testing "check if identicon test works"
    (is
     (=
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAjklEQVR4nOzXsQmAMBQGYRV3cUAdQwd0Gm2sJIWSBI6f+0oR8XjwSKYhhCE0htAYQmMITUzI/PXF49yv0vN12cYWP1L7/ZiJGEJjCE31xvm7bXptv5iJGEJjCE31WasVz1oPQ2gMoWlyuyvpfaN8i5mIITSG0BhCYwiNIeokZiKG0BhCYwiNITR3AAAA//+A3RtWaKqXgQAAAABJRU5ErkJggg=="
      (native-module/identicon "a")))))
