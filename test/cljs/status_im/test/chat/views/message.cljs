(ns status-im.test.chat.views.message
  (:require [cljs.test :refer [deftest is]]
            [status-im.ui.screens.chat.message.message :as message]))

(deftest parse-url
  (is (= (lazy-seq [{:text "" :url? false}
                    {:text "www.google.com" :url? true}])
         (message/parse-url "www.google.com")))
  (is (= (lazy-seq [{:text "" :url? false}
                    {:text "status.im" :url? true}])
         (message/parse-url "status.im")))
  (is (= (lazy-seq [{:text "$33.90" :url? false} nil])
         (message/parse-url "$33.90")))
  (is (= (lazy-seq [{:text "" :url? false}
                    {:text "https://www.google.com/?gfe_rd=cr&dcr=0&ei=P9-CWuyBGaro8AeqkYGQDQ&gws_rd=cr&fg=1" :url? true}])
         (message/parse-url "https://www.google.com/?gfe_rd=cr&dcr=0&ei=P9-CWuyBGaro8AeqkYGQDQ&gws_rd=cr&fg=1")))
  (is (= (lazy-seq [{:text "Status - " :url? false}
                    {:text "https://github.com/status-im/status-react" :url? true}
                    {:text " a Mobile Ethereum Operating System" :url? false}
                    nil])
         (message/parse-url "Status - https://github.com/status-im/status-react a Mobile Ethereum Operating System")))
  (is (= (lazy-seq [{:text "Browse, chat and make payments securely on the decentralized web." :url? false} nil])
         (message/parse-url "Browse, chat and make payments securely on the decentralized web.")))
  (is (= (lazy-seq [{:text "test...test..." :url? false} nil])
         (message/parse-url "test...test...")))
  (is (= (lazy-seq [{:text "test..test.." :url? false} nil])
         (message/parse-url "test..test..")))
  (is (= (lazy-seq [{:text "...test" :url? false} nil])
         (message/parse-url "...test"))))

(deftest right-to-left-text?
  (is (not (message/right-to-left-text? "You are lucky today!")))
  (is (not (message/right-to-left-text? "42")))
  (is (not (message/right-to-left-text? "You are lucky today! أنت محظوظ اليوم!")))
  (is (not (message/right-to-left-text? "۱۲۳۴۵۶۷۸۹")))
  (is (not (message/right-to-left-text? "۱۲۳۴۵۶۷۸۹أنت محظوظ اليوم!")))
  (is (message/right-to-left-text? "أنت محظوظ اليوم!"))
  (is (message/right-to-left-text? "أنت محظوظ اليوم! You are lucky today"))
  (is (message/right-to-left-text? "יש לך מזל היום!")))
