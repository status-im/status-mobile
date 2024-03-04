(ns status-im.navigation.utils-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.navigation.utils :as utils]))

(deftest test-add-view-to-modal-stack
  (testing "Add view ID to an empty modal stack"
    (is (= (utils/add-view-to-modal-stack [] 1)
           [])))
  (testing "Add view ID to a non-empty modal stack"
    (is (= (utils/add-view-to-modal-stack [[1 2]] 3)
           [[1 2 3]])))
  (testing "Add view ID when multiple modal stacks exist"
    (is (= (utils/add-view-to-modal-stack [[1 2] [3 4]] 5)
           [[1 2] [3 4 5]]))))

(deftest test-remove-last-view-from-current-modal-stack
  (testing "Remove last view ID from a single-element stack"
    (is (= (utils/remove-last-view-from-current-modal-stack [[1]])
           [])))
  (testing "Remove last view ID from the current modal stack"
    (is (= (utils/remove-last-view-from-current-modal-stack [[1 2 3] [4 5 6]])
           [[1 2 3] [4 5]])))
  (testing "Handle empty modal stacks"
    (is (= (utils/remove-last-view-from-current-modal-stack [])
           [])))
  (testing "Ensure unchanged stack when nested stack is a single element"
    (is (= (utils/remove-last-view-from-current-modal-stack [[1 2] [3]])
           [[1 2]]))))

(deftest test-add-stack-to-modal-stacks
  (testing "Add a new stack to an empty list of modal stacks"
    (is (= (utils/add-stack-to-modal-stacks [] 1)
           [[1]])))
  (testing "Add a new stack to existing modal stacks"
    (is (= (utils/add-stack-to-modal-stacks [[1 2]] 3)
           [[1 2] [3]])))
  (testing "Add a new stack when existing stacks are varied"
    (is (= (utils/add-stack-to-modal-stacks [[1 2] [3 4]] 5)
           [[1 2] [3 4] [5]]))))

(deftest test-remove-current-modal-stack
  (testing "Remove the only modal stack"
    (is (= (utils/remove-current-modal-stack [[1 2]])
           [])))
  (testing "Remove the last modal stack from multiple"
    (is (= (utils/remove-current-modal-stack [[1 2] [3 4] [5 6]])
           [[1 2] [3 4]])))
  (testing "Handle removal when modal stacks are empty"
    (is (= (utils/remove-current-modal-stack [])
           [])))
  (testing "Ensure unchanged when nested stacks have varied lengths"
    (is (= (utils/remove-current-modal-stack [[1] [2 3] [4 5 6]])
           [[1] [2 3]]))))

(deftest test-remove-views-from-modal-stack-until-comp-id
  (testing "Remove views until comp-id, including multiple occurrences"
    (is (= (utils/remove-views-from-modal-stack-until-comp-id [[1 2 3] [4 5 5 6]] 5)
           [[1 2 3] [4 5 5]])))
  (testing "comp-id at the beginning of a stack"
    (is (= (utils/remove-views-from-modal-stack-until-comp-id [[1 2] [3 4] [5 6 7]] 5)
           [[1 2] [3 4] [5]])))
  (testing "comp-id not found, no removal"
    (is (= (utils/remove-views-from-modal-stack-until-comp-id [[1 2 3] [4 5 6]] 7)
           [[1 2 3] [4 5 6]])))
  (testing "comp-id is the last element in a stack"
    (is (= (utils/remove-views-from-modal-stack-until-comp-id [[1 2 3] [4 5 6] [7 8 9]] 9)
           [[1 2 3] [4 5 6] [7 8 9]]))))
