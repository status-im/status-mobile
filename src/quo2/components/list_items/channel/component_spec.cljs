(ns quo2.components.list-items.channel.component-spec
  (:require [test-helpers.component :as h]
            [quo2.components.list-items.channel.view :as channel]))

(h/describe "list-items/channel Component"
  (h/test "default render"
    (h/render [channel/view {:name "general"}])
    (h/is-truthy (h/query-by-label-text :channel-list-item)))

  (h/test "with name & emoji"
    (h/render [channel/view
               {:name  "general"
                :emoji "👋"}])
    (h/is-truthy (h/query-by-text "# general"))
    (h/is-truthy (h/query-by-text "👋")))

  (h/test "notification & mentions count"
    (h/render [channel/view
               {:name           "general"
                :mentions-count 10
                :notification   :mention}])
    (h/is-truthy (h/query-by-text "10")))

  (h/test "unread indicator"
    (h/render [channel/view
               {:name         "general"
                :notification :notification}])
    (h/is-truthy (h/query-by-label-text :unviewed-messages-public)))

  (h/test "on-press event"
    (let [on-press (h/mock-fn)]
      (h/render [channel/view
                 {:name     "general"
                  :on-press on-press}])
      (h/fire-event :press (h/query-by-label-text :channel-list-item))
      (h/was-called on-press)))

  (h/test "on-long-press event"
    (let [on-long-press (h/mock-fn)]
      (h/render [channel/view
                 {:name          "general"
                  :on-long-press on-long-press}])
      (h/fire-event :long-press (h/query-by-label-text :channel-list-item))
      (h/was-called on-long-press))))
