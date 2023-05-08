(ns quo2.components.inputs.recovery-phrase.component-spec
  (:require [quo2.components.inputs.recovery-phrase.view :as recovery-phrase]
            [test-helpers.component :as h]
            [oops.core :as oops]))

(h/describe "Recovery phrase input"
  (h/test "Default render"
    (h/render [recovery-phrase/recovery-phrase-input {}])
    (h/is-truthy (h/get-by-label-text :recovery-phrase-input)))

  (h/test "Renders specified text"
    (let [text-expected "My custom text"]
      (h/render [recovery-phrase/recovery-phrase-input {} text-expected])
      (h/is-equal (oops/oget (h/get-by-label-text :recovery-phrase-input) "props" "children")
                  text-expected)))

  (h/test "Text changes and dispatches on-change-text"
    (let [new-text       "New text"
          on-change-mock (h/mock-fn)
          get-new-text   #(-> % (oops/oget "props" "onChangeText" "mock" "calls") (aget 0 0))]
      (h/render [recovery-phrase/recovery-phrase-input {:on-change-text on-change-mock}
                 "Old text"])
      (h/fire-event :change-text (h/get-by-label-text :recovery-phrase-input) new-text)
      (h/is-equal (get-new-text (h/get-by-label-text :recovery-phrase-input)) new-text)
      (h/was-called on-change-mock))))

#_(h/test "Renders specified text"
    (let [text-expected "Custom text"]
      (h/render [recovery-phrase/recovery-phrase-input {} text-expected])
      ;; TODO: investigate how to get the children
      (prn (js-keys (h/get-by-label-text :recovery-phrase-input)))))

;(h/test "Text changes and dispatches on-change"
;  (let [new-text       "New text"
;        on-change-mock (h/mock-fn)]
;    (h/render [recovery-phrase/recovery-phrase-input {:on-change-text on-change-mock}
;               ""])
;    (h/fire-event :change-text (h/get-by-label-text :recovery-phrase-input) new-text)
;    #_(h/debug (h/get-by-label-text :recovery-phrase-input))
;    #_(h/was-called on-change-mock)
;
;    #_(h/is-truthy (h/get-by-text new-text))))

;(h/describe "Error text"
;  (h/test "Marked when a word doesn't satisfy a predicate"
;    (h/render [recovery-phrase/recovery-phrase-input {:mark-errors? true
;                                                      :error-pred   #(>= (count %) 5)}
;               "Text with some words satisfying the predicate"])
;    ;; TODO: filter result to only return the wrong-marked workds
;    (prn (.children (.props (h/get-by-label-text :recovery-phrase-input)
;                            (fn [node]
;                              (string? (.-type node)))))))
;
;  #_(h/test "Not marked when `mark-errors?` false"
;      (h/render [recovery-phrase/recovery-phrase-input {:mark-errors? false
;                                                        :error-pred   #(>= 4 count)}
;                 "Text with some words satisfying the predicate"])
;      ;; TODO: filter result and see that all ar regular words
;      (h/get-by-label-text :recovery-phrase-input))
;
;  #_(h/test "Marked when words exceed the limit given"
;      (let [ok-words    "Words within the limit"
;            error-words "Words out of the limit"
;            words       (str ok-words error-words)]
;        (h/render [recovery-phrase/recovery-phrase-input {:mark-errors? true
;                                                          :word-limit   4}
;                   words]))
;      ;; TODO: filter result to only return the wrong-marked workds
;      (h/get-by-label-text :recovery-phrase-input))
