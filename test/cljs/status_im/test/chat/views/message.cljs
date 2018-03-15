(ns status-im.test.chat.views.message
  (:require [cljs.test :refer [deftest is]]
            [status-im.chat.views.message.message :as message]))

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
         (message/parse-url "Browse, chat and make payments securely on the decentralized web."))))
