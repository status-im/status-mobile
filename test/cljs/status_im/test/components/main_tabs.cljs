(ns status-im.test.components.main-tabs
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.components.main-tabs :as tabs]))

(deftest tab->index
  (is (contains? tabs/tab->index :chat-list))
  (is (= 1 (:discover tabs/tab->index))))

