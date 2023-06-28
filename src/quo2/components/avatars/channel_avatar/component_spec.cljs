(ns quo2.components.avatars.channel-avatar.component-spec
  (:require [quo2.components.avatars.channel-avatar.view :as component]
            [test-helpers.component :as h]))

(h/describe "Channel Avatar"
  (h/test "default render"
    (h/render [component/view])
    (h/is-truthy (h/query-by-label-text :initials))
    (h/is-null (h/query-by-label-text :emoji))
    (h/is-null (h/query-by-label-text :lock)))

  (h/test "with emoji, no lock set, big size"
    (let [emoji "🍓"]
      (h/render [component/view {:emoji emoji :big? true}])
      (h/is-null (h/query-by-label-text :initials))
      (h/is-truthy (h/query-by-text emoji))
      (h/is-null (h/query-by-label-text :lock))))

  (h/test "locked"
    (h/render [component/view {:locked? true}])
    (h/is-truthy (h/query-by-label-text :lock)))

  (h/test "unlocked"
    (h/render [component/view {:locked? false}])
    (h/is-truthy (h/query-by-label-text :lock)))

  (h/test "no emoji, smaller size"
    (h/render [component/view {:full-name "Status Mobile"}])
    (h/is-truthy (h/query-by-text "S")))

  (h/test "no emoji, big size"
    (h/render [component/view
               {:full-name "Status Mobile"
                :big?      true}])
    (h/is-truthy (h/query-by-text "SM"))))
