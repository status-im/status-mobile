(ns status-im.contexts.communities.actions.community-options.component-spec
  (:require
    [status-im.contexts.communities.actions.community-options.view :as options]
    [test-helpers.component :as h]))

(h/describe "community options for bottom sheets"
  (h/setup-restorable-re-frame)

  (h/test "joined options - Non token Gated"
    (h/setup-subs {:communities/my-pending-request-to-join nil
                   :communities/community                  {:joined true}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (h/is-truthy (h/get-by-translation-text :t/view-members))
    (h/is-truthy (h/get-by-translation-text :t/view-community-rules))
    (h/is-truthy (h/get-by-translation-text :t/mark-as-read))
    (h/is-truthy (h/get-by-translation-text :t/mute-community))
    (h/is-truthy (h/get-by-translation-text :t/notification-settings))
    (h/is-truthy (h/get-by-translation-text :t/invite-people-from-contacts))
    (h/is-truthy (h/get-by-translation-text :t/show-qr))
    (h/is-truthy (h/get-by-translation-text :t/share-community))
    (h/is-truthy (h/get-by-translation-text :t/leave-community)))

  (h/test "joined options - Token Gated"
    (h/setup-subs {:communities/my-pending-request-to-join nil
                   :communities/community                  {:joined            true
                                                            :token-permissions []}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (h/is-truthy (h/get-by-translation-text :t/view-members))
    (h/is-truthy (h/get-by-translation-text :t/view-community-rules))
    (h/is-truthy (h/get-by-translation-text :t/view-token-gating))
    (h/is-truthy (h/get-by-translation-text :t/mark-as-read))
    (h/is-truthy (h/get-by-translation-text :t/mute-community))
    (h/is-truthy (h/get-by-translation-text :t/notification-settings))
    (h/is-truthy (h/get-by-translation-text :t/invite-people-from-contacts))
    (h/is-truthy (h/get-by-translation-text :t/show-qr))
    (h/is-truthy (h/get-by-translation-text :t/share-community))
    (h/is-truthy (h/get-by-translation-text :t/leave-community)))

  (h/test "admin options - Non token Gated"
    (h/setup-subs {:communities/my-pending-request-to-join nil
                   :communities/community                  {:admin true}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (h/is-truthy (h/get-by-translation-text :t/view-members))
    (h/is-truthy (h/get-by-translation-text :t/view-community-rules))
    (h/is-truthy (h/get-by-translation-text :t/mark-as-read))
    (h/is-truthy (h/get-by-translation-text :t/mute-community))
    (h/is-truthy (h/get-by-translation-text :t/notification-settings))
    (h/is-truthy (h/get-by-translation-text :t/invite-people-from-contacts))
    (h/is-truthy (h/get-by-translation-text :t/show-qr))
    (h/is-truthy (h/get-by-translation-text :t/share-community)))

  (h/test "admin options - Token Gated"
    (h/setup-subs {:communities/my-pending-request-to-join nil
                   :communities/community                  {:admin             true
                                                            :token-permissions []}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (h/is-truthy (h/get-by-translation-text :t/view-members))
    (h/is-truthy (h/get-by-translation-text :t/view-community-rules))
    (h/is-truthy (h/get-by-translation-text :t/mark-as-read))
    (h/is-truthy (h/get-by-translation-text :t/mute-community))
    (h/is-truthy (h/get-by-translation-text :t/notification-settings))
    (h/is-truthy (h/get-by-translation-text :t/invite-people-from-contacts))
    (h/is-truthy (h/get-by-translation-text :t/show-qr))
    (h/is-truthy (h/get-by-translation-text :t/share-community)))

  (h/test "request sent options - Non token Gated"
    (h/setup-subs {:communities/my-pending-request-to-join "mock-id"
                   :communities/community                  {}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (h/is-truthy (h/get-by-translation-text :t/view-members))
    (h/is-truthy (h/get-by-translation-text :t/view-community-rules))
    (h/is-truthy (h/get-by-translation-text :t/invite-people-from-contacts))
    (h/is-truthy (h/get-by-translation-text :t/show-qr))
    (h/is-truthy (h/get-by-translation-text :t/share-community))
    (h/is-truthy (h/get-by-translation-text :t/cancel-request-to-join)))

  (h/test "request sent options - Token Gated"
    (h/setup-subs {:communities/my-pending-request-to-join "mock-id"
                   :communities/community                  {:token-permissions []}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (h/is-truthy (h/get-by-translation-text :t/invite-people-from-contacts))
    (h/is-truthy (h/get-by-translation-text :t/view-token-gating))
    (h/is-truthy (h/get-by-translation-text :t/show-qr))
    (h/is-truthy (h/get-by-translation-text :t/share-community))
    (h/is-truthy (h/get-by-translation-text :t/cancel-request-to-join)))

  (h/test "banned options - Non token Gated"
    (h/setup-subs {:communities/my-pending-request-to-join nil
                   :communities/community                  {:banList true}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (h/is-truthy (h/get-by-translation-text :t/view-members))
    (h/is-truthy (h/get-by-translation-text :t/view-community-rules))
    (h/is-truthy (h/get-by-translation-text :t/invite-people-from-contacts))
    (h/is-truthy (h/get-by-translation-text :t/show-qr))
    (h/is-truthy (h/get-by-translation-text :t/share-community)))

  (h/test "banned options - Token Gated"
    (h/setup-subs {:communities/my-pending-request-to-join nil
                   :communities/community                  {:banList           100
                                                            :token-permissions []}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (h/is-truthy (h/get-by-translation-text :t/invite-people-from-contacts))
    (h/is-truthy (h/get-by-translation-text :t/view-token-gating))
    (h/is-truthy (h/get-by-translation-text :t/show-qr))
    (h/is-truthy (h/get-by-translation-text :t/share-community)))

  (h/test "banned options - Token Gated"
    (h/setup-subs {:communities/my-pending-request-to-join nil
                   :communities/community                  {:banList           100
                                                            :token-permissions []}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (h/is-truthy (h/get-by-translation-text :t/invite-people-from-contacts))
    (h/is-truthy (h/get-by-translation-text :t/view-token-gating))
    (h/is-truthy (h/get-by-translation-text :t/show-qr))
    (h/is-truthy (h/get-by-translation-text :t/share-community)))

  (h/test "joined and muted community"
    (h/setup-subs {:communities/my-pending-request-to-join nil
                   :communities/community                  {:joined            true
                                                            :muted             true
                                                            :token-permissions []}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (h/is-truthy (h/get-by-translation-text :t/unmute-community))))
