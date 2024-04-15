(ns status-im.contexts.communities.actions.home-plus.view
  (:require
    [quo.core :as quo]
    [utils.re-frame :as rf]))

(defn view
  []
  [quo/action-drawer
   [[{:icon                :i/communities
      :accessibility-label :create-open-community
      :label               "Create Open community (only for testing)"
      :on-press            #(rf/dispatch [:e2e/create-open-community])}
     {:icon                :i/communities
      :accessibility-label :create-closed-community
      :label               "Create Closed community (only for testing)"
      :on-press            #(rf/dispatch [:e2e/create-closed-community])}
     {:icon                :i/communities
      :accessibility-label :create-admin-and-member-community
      :label               "Create Admin and Member community (only for testing)"
      :on-press            #(rf/dispatch [:e2e/create-admin-and-member-community])}
     {:icon                :i/communities
      :accessibility-label :create-snt-admin-community
      :label               "Create SNT Admin community (only for testing)"
      :on-press            #(rf/dispatch [:e2e/create-snt-admin-community])}
     {:icon                :i/communities
      :accessibility-label :create-token-gated-community
      :label               "Create Token Gated community (only for testing)"
      :on-press            #(rf/dispatch [:e2e/create-token-gated-community])}]]])
