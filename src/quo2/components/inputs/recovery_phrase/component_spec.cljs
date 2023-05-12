(ns quo2.components.inputs.recovery-phrase.component-spec
  (:require [clojure.string :as string]
            [oops.core :as oops]
            [quo2.components.inputs.recovery-phrase.view :as recovery-phrase]
            [test-helpers.component :as h]))

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
      (h/was-called on-change-mock)))

  (h/describe "Error text"
    (h/test "Marked when words doesn't satisfy a predicate"
      (h/render [recovery-phrase/recovery-phrase-input
                 {:mark-errors? true
                  :error-pred   #(>= (count %) 5)}
                 "Text with some error words that don't satisfy the predicate"])
      (let [children-text-nodes            (-> (h/get-by-label-text :recovery-phrase-input)
                                               (oops/oget "props" "children" "props" "children")
                                               (js->clj :keywordize-keys true))
            {:keys [ok-words error-words]} (group-by #(if (string? %) :ok-words :error-words)
                                                     children-text-nodes)]
        (h/is-equal (apply str ok-words) "Text with some   that   the ")
        (h/is-truthy (= (map #(-> % :props :argv second) error-words)
                        ["error" "words" "don't" "satisfy" "predicate"]))))

    (h/test "Marked when words exceed the limit given"
      (h/render [recovery-phrase/recovery-phrase-input
                 {:mark-errors? true
                  :word-limit   4}
                 "these are ok words, these words exceed the limit"])
      (let [children-text-nodes            (-> (h/get-by-label-text :recovery-phrase-input)
                                               (oops/oget "props" "children" "props" "children")
                                               (js->clj :keywordize-keys true))
            {:keys [ok-words error-words]} (group-by #(if (string? %) :ok-words :error-words)
                                                     children-text-nodes)]
        (h/is-equal (string/trim (apply str ok-words))
                    "these are ok words,")
        (h/is-equal (->> error-words
                         (map #(-> % :props :argv second))
                         (interpose " ")
                         (apply str))
                    "these words exceed the limit")))))
