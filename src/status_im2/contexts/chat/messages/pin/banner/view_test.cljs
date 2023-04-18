(ns status-im2.contexts.chat.messages.pin.banner.view-test
  (:require [status-im2.contexts.chat.messages.pin.banner.view :as view]
            [cljs.test :as t]
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

(t/deftest test-resolve-message
  (with-redefs [rf/sub sub]
    (t/testing ""
      (let [text (view/resolve-message parsed-text)]
        (t/is (= text
                 "foobar i just mention you here to debug this issue https://foo.bar , no worries"))))))
