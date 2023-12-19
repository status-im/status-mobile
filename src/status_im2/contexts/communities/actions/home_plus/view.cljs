(ns status-im2.contexts.communities.actions.home-plus.view
  (:require
    [quo.core :as quo]
    [utils.re-frame :as rf]))

(defn view
  []
  [quo/action-drawer
   [[{:icon                :i/communities
      :accessibility-label :create-closed-community
      :label               "Create closed community (only for testing)"
      :on-press            #(rf/dispatch [:fast-create-community/create-closed-community])}
     {:icon                :i/communities
      :accessibility-label :create-open-community
      :label               "Create open community (only for testing)"
      :on-press            #(rf/dispatch [:fast-create-community/create-open-community])}
     {:icon                :i/communities
      :accessibility-label :create-token-gated-community
      :label               "Create token-gated community (only for testing)"
      :on-press            #(rf/dispatch
                             [:fast-create-community/create-token-gated-community])}]]])
