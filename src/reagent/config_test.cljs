(ns reagent.config-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [reagent.config :as sut]))

(sut/set-convert-props-in-vectors! #{:foo :bar :foo-bar-qux})

(deftest convert-prop-value-test
  ;; `test-fn` transforms the result to be easily compared during testing
  (let [test-fn (comp js->clj sut/convert-prop-value)]
    (testing "camelCase keys are kept as is"
      (let [props {:foo nil :bar nil}]
        (is (= {"foo" nil
                "bar" nil}
               (test-fn props)))))

    (testing "kebab-case keys are transformed"
      (let [props {:foo nil :bar nil}]
        (is (= {"foo" nil
                "bar" nil}
               (test-fn props)))))

    (testing "kebab-case keys are transformed when passed inside a vector"
      (let [props-in-vector [{:foo nil :bar nil}]]
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
                 (test-fn complex-props)))))

      (testing "Keep `:qux` without transformations because it isn't specified in config"
        (let [props-with-vector {:qux [{:foo-qux :bar-qux}
                                       {:baz-qux nil}]}]
          (is (= {"qux" [{"foo-qux" "bar-qux"}
                         {"baz-qux" nil}]}
                 (test-fn props-with-vector)))))

      (testing "Keep `:qux` without transformations but transforms :foo due to config"
        (let [props-with-vectors {:foo {:foo-foo [{:foo-foo [{:foo-foo-1 nil
                                                              :foo-foo-2 nil}]}
                                                  {:foo-bar [{:foo-bar-1 nil
                                                              :foo-bar-2 nil}]}]}
                                  :qux {:qux-qux [{:qux-qux [{:qux-qux-1 nil
                                                              :qux-qux-2 nil}]}
                                                  {:qux-bar [{:qux-qux-1 nil
                                                              :qux-qux-2 nil}]}]}}]
          (is (= {"foo" {"fooFoo"
                         ;; This vector is transformed
                         [{"fooFoo" [{"fooFoo1" nil "fooFoo2" nil}]}
                          {"fooBar" [{"fooBar1" nil "fooBar2" nil}]}]}
                  "qux" {"quxQux"
                         ;; This vector is not
                         [{"qux-qux" [{"qux-qux-1" nil "qux-qux-2" nil}]}
                          {"qux-bar" [{"qux-qux-1" nil "qux-qux-2" nil}]}]}}
                 (test-fn props-with-vectors))))))))
