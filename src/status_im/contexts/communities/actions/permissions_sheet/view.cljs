(ns status-im.contexts.communities.actions.permissions-sheet.view
  (:require
    [quo.core :as quo]
    [react-native.gesture :as gesture]
    [utils.re-frame :as rf]))

(defn view
  [id]
  (let [permissions (rf/sub [:community/token-permissions id])]
    [gesture/scroll-view
     [quo/community-detail-token-gating {:permissions permissions}]]))
