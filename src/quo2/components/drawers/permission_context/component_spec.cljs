(ns quo2.components.drawers.permission-context.component-spec
  (:require [quo2.components.drawers.permission-context.view :as permission-context]
            [react-native.core :as rn]
            [test-helpers.component :as h]))

(h/describe "permission context"
  (h/test "it tests the default render"
    (h/render [permission-context/view
               [rn/text
                {:accessibility-label :accessibility-id}
                "a sample label"]])
    (-> (js/expect (h/get-by-label-text :accessibility-id))
        (.toBeTruthy))))
