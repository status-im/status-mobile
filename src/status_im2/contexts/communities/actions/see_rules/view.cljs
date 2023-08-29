(ns status-im2.contexts.communities.actions.see-rules.view
  (:require [status-im2.contexts.communities.actions.generic-menu.view :as generic-menu]
            [status-im2.contexts.communities.actions.community-rules-list.view :as community-rules]
            [utils.i18n :as i18n]))

(defn view
  [id]
  [generic-menu/view
   {:id    id
    :title (i18n/label :t/community-rules)}

   [community-rules/view community-rules/standard-rules]])
