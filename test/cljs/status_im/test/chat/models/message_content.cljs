(ns status-im.test.chat.models.message-content
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.platform :as platform]
            [status-im.chat.models.message-content :as message-content]))

(deftest enrich-string-content-test
  (if platform/desktop?
    (testing "Text content of the message is enriched correctly"
      (is (not (:metadata (message-content/enrich-content {:text "Plain message"}))))
      (is (= {:bold [[5 14]]}
             (:metadata (message-content/enrich-content {:text "Some *styling* present"}))))
      (is (= {:bold [[5 14]]
              :tag  [[28 33] [38 43]]}
             (:metadata (message-content/enrich-content {:text "Some *styling* present with #tag1 and #tag2 as well"}))))))

  (testing "right to left is correctly identified"
    (is (not (:rtl? (message-content/enrich-content {:text "You are lucky today!"}))))
    (is (not (:rtl? (message-content/enrich-content {:text "42"}))))
    (is (not (:rtl? (message-content/enrich-content {:text "You are lucky today! أنت محظوظ اليوم!"}))))
    (is (not (:rtl? (message-content/enrich-content {:text "۱۲۳۴۵۶۷۸۹"}))))
    (is (not (:rtl? (message-content/enrich-content {:text "۱۲۳۴۵۶۷۸۹أنت محظوظ اليوم!"}))))
    (is (:rtl? (message-content/enrich-content {:text "أنت محظوظ اليوم!"})))
    (is (:rtl? (message-content/enrich-content {:text "أنت محظوظ اليوم! You are lucky today"})))
    (is (:rtl? (message-content/enrich-content {:text "יש לך מזל היום!"})))))

(deftest build-render-recipe-test
  (testing "Render tree is build from text"
    (is (not (:render-recipe (message-content/enrich-content {:text "Plain message"}))))
    (is (= (if platform/desktop?
             '(["Test " :text]
               ["#status" :tag]
               [" one three " :text]
               ["#core-chat (@developer)!" :bold]
               [" By the way, " :text]
               ["nice link(https://link.com)" :italic])
             '(["Test " :text]
               ["#status" :tag]
               [" one three *" :text]
               ["#core-chat" :tag]
               [" (" :text]
               ["@developer" :mention]
               [")!* By the way, ~nice link(" :text]
               ["https://link.com" :link]
               [")~" :text]))
           (:render-recipe (message-content/enrich-content {:text "Test #status one three *#core-chat (@developer)!* By the way, ~nice link(https://link.com)~"}))))))
