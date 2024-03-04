(ns status-im.navigation.utils-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.navigation.utils :as utils]))

(deftest test-add-view-to-modal-stack
  (testing "Add view ID to an existing modal stack"
    (is (= (utils/add-view-to-modal-stack [[1 2]] 3)
           [[1 2 3]])))
  (testing "Add view ID when there are no modal stacks"
    (is (= (utils/add-view-to-modal-stack [] 1)
           []))))

(deftest test-remove-last-view-from-current-modal-stack
  (testing "Remove last view ID from the current modal stack"
    (is (= (utils/remove-last-view-from-current-modal-stack [[1 2 3] [4 5 6]])
           [[1 2 3] [4 5]])))
  (testing "Handle removal when modal stacks are empty"
    (is (= (utils/remove-last-view-from-current-modal-stack [])
           []))))

(deftest test-add-stack-to-modal-stacks
  (testing "Add a new stack to existing modal stacks"
    (is (= (utils/add-stack-to-modal-stacks [[1 2]] 3)
           [[1 2] [3]])))
  (testing "Add a new stack when there are no modal stacks"
    (is (= (utils/add-stack-to-modal-stacks [] 1)
           [[1]]))))

(deftest test-remove-current-modal-stack
  (testing "Remove the current (last) modal stack"
    (is (= (utils/remove-current-modal-stack [[1 2] [3 4]])
           [[1 2]])))
  (testing "Handle removal when there is only one modal stack"
    (is (= (utils/remove-current-modal-stack [[1 2]])
           [])))
  (testing "Handle removal when there are no modal stacks"
    (is (= (utils/remove-current-modal-stack [])
           []))))

(deftest test-remove-views-from-modal-stack-until-comp-id
  (testing "Remove views from modal stack until comp-id is found"
    (is (= (utils/remove-views-from-modal-stack-until-comp-id [[1 2 3] [4 5 6 7]] 5)
           [[1 2 3] [4 5]])))
  (testing "Do nothing if comp-id is not found"
    (is (= (utils/remove-views-from-modal-stack-until-comp-id [[1 2 3] [4 5 6]] 8)
           [[1 2 3] [4 5 6]]))))
