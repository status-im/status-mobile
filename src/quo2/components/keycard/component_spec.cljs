(ns quo2.components.keycard.component-spec
  (:require [quo2.components.keycard.view :as keycard]
            [test-helpers.component :as h]))

(h/describe "keycard component"
  (h/test "Render of keycard component when: holder-name prop is not set"
    (h/render [keycard/keycard])
    (h/is-truthy (h/get-by-translation-text :empty-keycard)))
  (h/test "Render of keycard component when: holder-name prop is set"
    (h/render [keycard/keycard {:holder-name "Alisha"}])
    (h/is-truthy (h/get-by-translation-text :user-keycard))))
