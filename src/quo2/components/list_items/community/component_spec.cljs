(ns quo2.components.list-items.community.component-spec
  (:require [quo2.components.list-items.community.view :as component]
            [test-helpers.component :as h]))

(h/describe "Community list"
  (h/test "default render"
    (h/render [component/view {}])
    (h/is-truthy (h/get-by-label-text :community-item-title))
    (h/is-null (h/query-by-label-text :community-item-info)))

  (h/describe "type share"
    (h/test "renders"
      (h/render [component/view
                 {:type     :share
                  :title    "Title"
                  :subtitle "Subtitle"}])
      (h/is-truthy (h/get-by-text "# Title"))
      (h/is-truthy (h/get-by-text "Subtitle"))
      (h/is-null (h/query-by-label-text :community-item-info))))

  (h/describe "type engage"
    (h/test "default, no info"
      (h/render [component/view {:type :engage}])
      (h/is-null (h/query-by-label-text :community-item-info)))

    (h/test "info is :navigation"
      (h/render [component/view {:type :engage :info :navigation}])
      (h/is-truthy (h/get-by-label-text :info-navigation)))

    (h/test "info is :token-gated"
      (h/render [component/view {:type :engage :info :token-gated}])
      (h/is-truthy (h/get-by-label-text :info-token-gated)))

    (h/test "info is :muted"
      (h/render [component/view {:type :engage :info :muted}])
      (h/is-truthy (h/get-by-label-text :info-muted)))

    (h/test "info is :notification"
      (h/render [component/view {:type :engage :info :notification}])
      (h/is-truthy (h/get-by-label-text :info-notification-dot)))

    (h/test "info is :mention"
      (h/render [component/view
                 {:type         :engage
                  :info         :mention
                  :unread-count 9000}])
      (h/is-truthy (h/get-by-text "99+"))))

  (h/describe "type discover"
    (h/test "default, no info, no member stats"
      (h/render [component/view {:type :discover}])
      (h/is-null (h/query-by-label-text :stats-members-count))
      (h/is-null (h/query-by-label-text :community-item-info)))

    (h/test "info is :token-gated, show member stats"
      (let [on-press-info (h/mock-fn)]
        (h/render [component/view
                   {:type          :discover
                    :info          :token-gated
                    :locked?       true
                    :blur?         false
                    :on-press-info on-press-info
                    :members       {:members-count 9876 :active-count 1350}
                    :tokens        [{:id 1 :group [{:id 1}]}]}])
        (h/is-truthy (h/get-by-label-text :community-item-info))
        (h/is-truthy (h/get-by-label-text :permission-tag))
        (h/is-truthy (h/get-by-label-text :stats-members-count))
        (h/is-truthy (h/get-by-label-text :stats-active-count))
        (h/fire-event :press (h/get-by-label-text :permission-tag))
        (h/was-called on-press-info)))))
