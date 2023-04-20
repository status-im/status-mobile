(ns quo2.components.inputs.recovery-phrase.component-spec
  (:require [quo2.components.inputs.recovery-phrase.view :as recovery-phrase]
            [test-helpers.component :as h]))

(h/describe "Recovery phrase input"
  (h/test "Default render"
    (h/render [recovery-phrase/recovery-phrase-input {}])
    (h/is-truthy (h/get-by-label-text :recovery-phrase-input))))
