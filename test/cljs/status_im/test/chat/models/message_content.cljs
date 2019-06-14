(ns status-im.test.chat.models.message-content
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.models.message-content :as message-content]))

(deftest enrich-string-content-test
  (testing "Text content of the message is enriched correctly"
    (is (not (:metadata (message-content/enrich-content {:text "Plain message"}))))
    (is (= {:bold [[5 14]]}
           (:metadata (message-content/enrich-content {:text "Some *styling* present"}))))
    (is (= {:bold [[5 14]]
            :tag  [[28 33] [38 43]]}
           (:metadata (message-content/enrich-content {:text "Some *styling* present with #tag1 and #tag2 as well"})))))
  (testing "right to left is correctly identified"
    (is (not (:rtl? (message-content/enrich-content {:text "You are lucky today!"}))))                  ;English
    (is (not (:rtl? (message-content/enrich-content {:text "42"}))))                                    ;Numbers
    (is (not (:rtl? (message-content/enrich-content {:text "You are lucky today! أنت محظوظ اليوم!"})))) ;English + Arabic
    (is (not (:rtl? (message-content/enrich-content {:text "۱۲۳۴۵۶۷۸۹"}))))                             ;Arabic numbers (not RTL)
    (is (not (:rtl? (message-content/enrich-content {:text "۱۲۳۴۵۶۷۸۹أنت محظوظ اليوم!"}))))             ;Arabic
    (is (:rtl? (message-content/enrich-content {:text "أنت محظوظ اليوم!"})))                            ;Arabic
    (is (:rtl? (message-content/enrich-content {:text "أنت محظوظ اليوم! You are lucky today"})))        ;English + Arabic
    (is (:rtl? (message-content/enrich-content {:text "יש לך מזל היום!"})))))                           ;Hebrew
    (is (:rtl? (message-content/enrich-content {:text "من به عنوان یک (مترجم / تاجر) کار میکنم"})))))   ;Farsi
    (is (:rtl? (message-content/enrich-content {:text "Խնդրում եմ, դանդաղ խոսեք:"})))))                 ;Armenian           
    (is (:rtl? (message-content/enrich-content {:text "ئایا زمانی کوردی قسە دەکەیت؟"})))))              ;Kurdish
    (is (:rtl? (message-content/enrich-content {:text "!سیدھےجاکر داًہیں/باًہیں مڑجاًہے"})))))             ;Urdu
    (is (:rtl? (message-content/enrich-content {:text "ދިވުހިބަހުނ ވާހަކަ ދައްކަން އިނގޭތަ؟"})))))                       ;Dhivehi (Maldeves)
    

(deftest build-render-recipe-test
  (testing "Render tree is build from text"
    (is (not (:render-recipe (message-content/enrich-content {:text "Plain message"}))))
    (is (= '(["Test " :text]
             ["#status" :tag]
             [" one three " :text]
             ["#core-chat (@developer)!" :bold]
             [" By the way, " :text]
             ["nice link(https://link.com)" :italic])
           (:render-recipe (message-content/enrich-content {:text "Test #status one three *#core-chat (@developer)!* By the way, ~nice link(https://link.com)~"}))))))
