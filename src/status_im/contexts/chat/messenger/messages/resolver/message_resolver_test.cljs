(ns status-im.contexts.chat.messenger.messages.resolver.message-resolver-test
  (:require
    [cljs.test :as t]
    [status-im.contexts.chat.messenger.messages.resolver.message-resolver :as resolver]
    [utils.re-frame :as rf]))

(def mentions
  {"0xsome_id" "foobar"})

(defn sub
  [[_ mention]]
  (get mentions mention mention))

(def parsed-text
  [{:type "paragraph"
    :children
    [{:literal ""}
     {:type "mention"
      :literal
      "0xsome_id"}
     {:literal " i just mention you here to debug this issue "}
     {:type "link"
      :children
      [{:literal "https://foo.bar"}]
      :literal ""
      :title ""
      :destination "https://foo.bar"}
     {:literal " , no worries"}]}])

(t/deftest resolve-message-test
  (with-redefs [rf/sub sub]
    (t/testing ""
      (let [text (resolver/resolve-message parsed-text)]
        (t/is (= text
                 "foobar i just mention you here to debug this issue https://foo.bar , no worries"))))))
