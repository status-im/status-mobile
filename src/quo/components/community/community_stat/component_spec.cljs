(ns quo.components.community.community-stat.component-spec
  (:require
    [quo.core :as quo]
    [test-helpers.component :as h]
    utils.money))

(h/describe "Community Stat Component"
  (h/test "default render"
    (h/render [quo/community-stat
               {:value               "5000"
                :icon                :i/active-members
                :accessibility-label :community-active-members}])
    (h/is-truthy (h/query-by-label-text :community-active-members)))
  (h/test "renders community stat with a value"
    (h/render [quo/community-stat
               {:value "5000"
                :icon  :i/members}])
    (h/is-truthy (h/query-by-text (utils.money/format-amount "5000")))))
