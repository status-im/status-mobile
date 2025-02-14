(ns react-native.utils-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [utils.reagent :as sut]))

(sut/set-convert-props-in-vectors!)

(deftest convert-prop-value-test
  ;; `test-fn` transforms the result to be easily compared during testing
  (let [test-fn (comp js->clj sut/convert-prop-value)]
    (testing "camelCase keys are kept as is"
      (let [props {:foo nil
                   :bar nil}]
        (is (= (update-keys props name)
               (test-fn props)))))

    (testing "kebab-case keys are transformed"
      (let [props {:foo nil
                   :bar nil}]
        (is (= {"foo" nil
                "bar" nil}
               (test-fn props)))))

    (testing "kebab-case keys are transformed when passed inside a vector"
      (let [props-in-vector [{:foo nil
                              :bar nil}]]
        (is (= [{"foo" nil
                 "bar" nil}]
               (test-fn props-in-vector)))))

    (testing "kebab-case keys are transformed recursively when the structure has vectors"
      (let [props-with-vectors {:foo [{:foo-bar nil
                                       :foo-baz nil}]
                                :bar [{:bar-baz nil}
                                      {:bar-qux nil}]}]
        (is (= {"foo" [{"fooBar" nil
                        "fooBaz" nil}]
                "bar" [{"barBaz" nil} {"barQux" nil}]}
               (test-fn props-with-vectors))))
      (testing "Complex example"
        (let [complex-props {:foo         [{:foo-bar nil :foo-baz nil}]
                             :bar         [{:bar-baz nil} {:bar-qux nil}]
                             :qux         {:foo-qux :bar-qux}
                             :foo-bar-qux {:foo         [{:bar :qux}
                                                         {:bar-qux nil}]
                                           :foo-bar     :qux
                                           :foo-bar-qux [:foo :bar :qux {:foo-bar :foo-bar}]}}]
          (is (= {"foo"       [{"fooBar" nil "fooBaz" nil}]
                  "bar"       [{"barBaz" nil} {"barQux" nil}]
                  "qux"       {"fooQux" "bar-qux"}
                  "fooBarQux" {"foo"       [{"bar" "qux"} {"barQux" nil}]
                               "fooBar"    "qux"
                               "fooBarQux" ["foo" "bar" "qux" {"fooBar" "foo-bar"}]}}
                 (test-fn complex-props))))))))
