(ns quo.components.keycard.component-spec
  (:require
    [quo.components.keycard.view :as keycard]
    [test-helpers.component :as h]))

(h/describe "keycard component"
  (let [holder-name "Alisha"]
    (h/test "Render of keycard component when: holder-name prop is set"
      (h/render [keycard/keycard {:holder-name holder-name}])
      (h/is-truthy (h/get-by-translation-text :t/user-keycard {:name holder-name})))))
