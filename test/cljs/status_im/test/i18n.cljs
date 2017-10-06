(ns status-im.test.i18n
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.i18n :as i18n]))

(deftest label-options
  (is (not (nil? (:key (i18n/label-options {:key nil}))))))
