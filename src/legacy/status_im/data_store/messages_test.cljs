(ns legacy.status-im.data-store.messages-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [legacy.status-im.data-store.messages :as m]))

(def message-id "0xfe96d03da2159e632a6653d04028b0de8b55f78f03521b26ce10dc5f48a16aee")
(def chat-id "chat-id")
(def from
  "0x0424a68f89ba5fcd5e0640c1e1f591d561fa4125ca4e2a43592bc4123eca10ce064e522c254bb83079ba404327f6eafc01ec90a1444331fe769d3f3a7f90b0dde1")

(deftest message<-rpc
  (testing "message from RPC"
    (let [expected {:message-id                 message-id
                    :content                    {:chat-id     chat-id
                                                 :sticker     {:hash "hash" :pack 1}
                                                 :text        "hta"
                                                 :line-count  1
                                                 :ens-name    "ens-name"
                                                 :parsed-text "parsed-text"
                                                 :rtl?        false
                                                 :image       nil
                                                 :response-to "a"
                                                 :links       nil}
                    :whisper-timestamp          1
                    :contact-verification-state 1
                    :contact-request-state      2
                    :outgoing-status            :sending
                    :command-parameters         nil
                    :outgoing                   true
                    :message-type               0
                    :clock-value                2
                    :from                       from
                    :chat-id                    chat-id
                    :quoted-message             {:from "from"
                                                 :text "reply"}
                    :content-type               1
                    :compressed-key             "c"
                    :timestamp                  3
                    :link-previews              [{:thumbnail {:url      "http://localhost"
                                                              :data-uri "data:image/png"}}]}
          message  {:id                       message-id
                    :whisperTimestamp         1
                    :parsedText               "parsed-text"
                    :ensName                  "ens-name"
                    :contactVerificationState 1
                    :contactRequestState      2
                    :localChatId              chat-id
                    :from                     from
                    :text                     "hta"
                    :rtl                      false
                    :chatId                   chat-id
                    :lineCount                1
                    :sticker                  {:hash "hash" :pack 1}
                    :contentType              1
                    :messageType              0
                    :clock                    2
                    :responseTo               "a"
                    :compressedKey            "c"
                    :quotedMessage            {:from "from"
                                               :text "reply"}
                    :timestamp                3
                    :outgoingStatus           "sending"
                    :linkPreviews             [{:thumbnail {:url     "http://localhost"
                                                            :dataUri "data:image/png"}}]}]
      (is (= expected (m/<-rpc message))))))
