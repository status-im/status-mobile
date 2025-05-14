(ns quo.components.drawers.permission-context.component-spec
  (:require
    [quo.components.drawers.permission-context.view :as permission-context]
    [test-helpers.component :as h]))

(h/describe "permission context"
  (js/beforeEach (fn []
                   (-> (js/jest.spyOn js/console "error")
                       (.mockImplementation js/jest.fn))))

  (h/describe "action type"
    (h/test "shows the label"
      (h/render [permission-context/view
                 {:type         :action
                  :action-label "test"
                  :action-icon  :i/communities}])
      (-> (js/expect (h/get-by-text "test"))
          (.toBeTruthy)))

    (h/test "fails due to missing props"
      (-> (js/expect
           (fn []
             (h/render [permission-context/view
                        {:type :action}])))
          (.toThrow))))

  (h/describe "single-token-gating type"
    (h/test "shows the token-tag container"
      (h/render [permission-context/view
                 {:type         :single-token-gating
                  :token-value  "23"
                  :token-symbol :eth}])
      (-> (js/expect (h/get-by-label-text :permission-context-single-token))
          (.toBeTruthy)))

    (h/test "fails due to wrong props"
      (-> (js/expect
           (fn []
             (h/render [permission-context/view
                        {:type         :single-token-gating
                         :action-label "test"
                         :action-icon  :i/communities}])))
          (.toThrow))))

  (h/describe "multiple-token-gating"
    (h/test "shows the token-tag container"
      (h/render [permission-context/view
                 {:type         :multiple-token-gating
                  :token-groups [[:eth :snt] [:snt :eth :snt]]}])
      (-> (js/expect (h/get-by-label-text :permission-context-multiple-token))
          (.toBeTruthy)))

    (h/test "fails if token-groups is empty"
      (-> (js/expect
           (fn []
             (h/render [permission-context/view
                        {:type         :multiple-token-gating
                         :token-groups []}])))
          (.toThrow)))

    (h/test "fails due to wrong props"
      (-> (js/expect
           (fn []
             (h/render [permission-context/view
                        {:type         :multiple-token-gating
                         :token-value  "23"
                         :token-symbol :eth}])))
          (.toThrow)))))
