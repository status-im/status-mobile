(ns status-im2.contexts.communities.actions.community-options.component-spec
  (:require [re-frame.core :as re-frame]
            [test-helpers.component :as h]
            [status-im2.contexts.communities.actions.community-options.view :as options]))

(defn setup-subs
  [subscriptions]
  (doseq [keyval subscriptions]
    (re-frame/reg-sub
     (key keyval)
     (fn [_] (val keyval)))))

(h/describe "community options for bottom sheets"
  (h/test "joined options - Non token Gated"
    (setup-subs {:communities/my-pending-request-to-join nil
                 :communities/community                  {:joined true}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-translation-text :view-members))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :view-community-rules))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :mark-as-read))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :mute-community))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :notification-settings))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :invite-people-from-contacts))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :show-qr))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :share-community))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :leave-community))
        (.toBeTruthy)))

  (h/test "joined options - Token Gated"
    (setup-subs {:communities/my-pending-request-to-join nil
                 :communities/community                  {:joined            true
                                                          :token-permissions []}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-translation-text :view-members))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :view-community-rules))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :view-token-gating))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :mark-as-read))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :mute-community))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :notification-settings))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :invite-people-from-contacts))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :show-qr))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :share-community))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :leave-community))
        (.toBeTruthy)))

  (h/test "admin options - Non token Gated"
    (setup-subs {:communities/my-pending-request-to-join nil
                 :communities/community                  {:admin true}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-translation-text :view-members))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :view-community-rules))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :mark-as-read))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :mute-community))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :notification-settings))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :invite-people-from-contacts))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :show-qr))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :share-community))
        (.toBeTruthy)))

  (h/test "admin options - Token Gated"
    (setup-subs {:communities/my-pending-request-to-join nil
                 :communities/community                  {:admin             true
                                                          :token-permissions []}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-translation-text :view-members))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :view-community-rules))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :mark-as-read))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :mute-community))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :notification-settings))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :invite-people-from-contacts))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :show-qr))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :share-community))
        (.toBeTruthy)))

  (h/test "request sent options - Non token Gated"
    (setup-subs {:communities/my-pending-request-to-join "mock-id"
                 :communities/community                  {}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-translation-text :view-members))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :view-community-rules))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :invite-people-from-contacts))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :show-qr))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :share-community))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :cancel-request-to-join))
        (.toBeTruthy)))

  (h/test "request sent options - Token Gated"
    (setup-subs {:communities/my-pending-request-to-join "mock-id"
                 :communities/community                  {:token-permissions []}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-translation-text :invite-people-from-contacts))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :view-token-gating))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :show-qr))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :share-community))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :cancel-request-to-join))
        (.toBeTruthy)))

  (h/test "banned options - Non token Gated"
    (setup-subs {:communities/my-pending-request-to-join nil
                 :communities/community                  {:banList true}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-translation-text :view-members))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :view-community-rules))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :invite-people-from-contacts))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :show-qr))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :share-community))
        (.toBeTruthy)))

  (h/test "banned options - Token Gated"
    (setup-subs {:communities/my-pending-request-to-join nil
                 :communities/community                  {:banList           100
                                                          :token-permissions []}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-translation-text :invite-people-from-contacts))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :view-token-gating))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :show-qr))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :share-community))
        (.toBeTruthy)))

  (h/test "banned options - Token Gated"
    (setup-subs {:communities/my-pending-request-to-join nil
                 :communities/community                  {:banList           100
                                                          :token-permissions []}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-translation-text :invite-people-from-contacts))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :view-token-gating))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :show-qr))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-translation-text :share-community))
        (.toBeTruthy)))

  (h/test "joined and muted community"
    (setup-subs {:communities/my-pending-request-to-join nil
                 :communities/community                  {:joined            true
                                                          :muted             true
                                                          :token-permissions []}})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-translation-text :unmute-community))
        (.toBeTruthy))))
