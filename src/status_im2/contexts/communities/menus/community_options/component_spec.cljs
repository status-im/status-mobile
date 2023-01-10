
(ns status-im2.contexts.communities.menus.community-options.component-spec
  (:require [re-frame.core :as re-frame]
            [test-helpers.component :as h]
            [i18n.i18n :as i18n]
            [status-im2.setup.i18n-resources :as i18n-resources]
            [status-im2.contexts.communities.menus.community-options.view :as options]))

(defn init
  []
  (i18n/set-language "en")
  (i18n-resources/load-language "en"))

(defn setup-sub
  [opts]
  (re-frame/reg-sub
   :communities/community
   (fn [_] opts)))

(h/describe "community options for bottom sheets"
  (h/test "joined options - Non token Gated"
    (setup-sub {:joined true})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-text "View members"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "View Community Rules"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Mark as read"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Mute community"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Notification settings"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Invite people from contact list"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Show QR code"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Share community"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Leave community"))
        (.toBeTruthy)))

  (h/test "joined options - Token Gated"
    (setup-sub {:joined       true
                :token-gated? true})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-text "View members"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "View Community Rules"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "View token gating"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Mark as read"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Mute community"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Notification settings"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Invite people from contact list"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Show QR code"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Share community"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Leave community"))
        (.toBeTruthy)))

  (h/test "admin options - Non token Gated"
    (setup-sub {:admin true})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-text "View members"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "View Community Rules"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Edit community"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Mark as read"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Mute community"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Notification settings"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Invite people from contact list"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Show QR code"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Share community"))
        (.toBeTruthy)))

  (h/test "admin options - Token Gated"
    (setup-sub {:admin        true
                :token-gated? true})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-text "View members"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "View Community Rules"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Edit community"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Mark as read"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Mute community"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Notification settings"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Invite people from contact list"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Show QR code"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Share community"))
        (.toBeTruthy)))

  (h/test "request sent options - Non token Gated"
    (setup-sub {:requested-to-join-at true})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-text "View members"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "View Community Rules"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Invite people from contact list"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Show QR code"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Share community"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Cancel request to join"))
        (.toBeTruthy)))

  (h/test "request sent options - Token Gated"
    (setup-sub {:requested-to-join-at 100
                :token-gated?         true})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-text "Invite people from contact list"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "View token gating"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Show QR code"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Share community"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Cancel request to join"))
        (.toBeTruthy)))

  (h/test "banned options - Non token Gated"
    (setup-sub {:banList true})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-text "View members"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "View Community Rules"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Invite people from contact list"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Show QR code"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Share community"))
        (.toBeTruthy)))

  (h/test "banned options - Token Gated"
    (setup-sub {:banList      100
                :token-gated? true})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-text "Invite people from contact list"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "View token gating"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Show QR code"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Share community"))
        (.toBeTruthy)))

  (h/test "banned options - Token Gated"
    (setup-sub {:banList      100
                :token-gated? true})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-text "Invite people from contact list"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "View token gating"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Show QR code"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Share community"))
        (.toBeTruthy)))

  (h/test "joined and muted community"
    (setup-sub {:joined       true
                :muted        true
                :token-gated? true})
    (h/render [options/community-options-bottom-sheet {:id "test"}])
    (-> (h/expect (h/get-by-text "Unmute community"))
        (.toBeTruthy))))


