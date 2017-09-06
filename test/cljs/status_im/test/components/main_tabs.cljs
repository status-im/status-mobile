(ns status-im.test.components.main-tabs
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ui.screens.main-tabs.views :as tabs]))

(deftest tab->index
  (is (contains? tabs/tab->index :chat-list))
  (is (= 2 (:discover tabs/tab->index))))

