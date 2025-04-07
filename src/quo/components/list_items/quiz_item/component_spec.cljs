(ns quo.components.list-items.quiz-item.component-spec
  (:require
    [quo.components.list-items.quiz-item.view :as quiz-item]
    [test-helpers.component :as h]))

(h/describe "List Items: Token Value"
  (h/test "Number label renders"
    (h/render [quiz-item/view
               {:state  :empty
                :word   "collapse"
                :number 2}])
    (h/is-truthy (h/get-by-label-text :number-container)))
  (h/test "Success icon renders"
    (h/render [quiz-item/view
               {:state  :success
                :word   "collapse"
                :number 2}])
    (h/is-truthy (h/get-by-label-text :success-icon)))
  (h/test "Error icon renders"
    (h/render [quiz-item/view
               {:state  :error
                :word   "collapse"
                :number 2}])
    (h/is-truthy (h/get-by-label-text :error-icon))))
