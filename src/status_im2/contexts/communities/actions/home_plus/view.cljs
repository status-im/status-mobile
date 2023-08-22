(ns status-im2.contexts.communities.actions.home-plus.view
  (:require [quo2.core :as quo]
            [utils.re-frame :as rf]
            [status-im2.config :as config]))

(defn view
  []
  [quo/action-drawer
   [(concat [{:icon                :i/download
              :accessibility-label :import-community
              :label               "Import community"
              :on-press            #(rf/dispatch [:navigate-to :community-import])}
             {:icon                :i/communities
              :accessibility-label :create-community
              :label               "Create community (only for e2e)"
              :on-press            #(rf/dispatch [:legacy-only-for-e2e/open-create-community])}]
            (when config/fast-create-community-enabled?
              [{:icon                :i/communities
                :accessibility-label :create-closed-community
                :label               "Create closed community"
                :on-press            #(rf/dispatch [:fast-create-community/create-closed-community])}
               {:icon                :i/communities
                :accessibility-label :create-open-community
                :label               "Create open community"
                :on-press            #(rf/dispatch [:fast-create-community/create-open-community])}
               {:icon                :i/communities
                :accessibility-label :create-token-gated-community
                :label               "Create token-gated community"
                :on-press            #(rf/dispatch
                                       [:fast-create-community/create-token-gated-community])}]))]])
