(ns status-im.contexts.communities.actions.see-rules.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.communities.actions.community-rules-list.view :as community-rules]
    [status-im.contexts.communities.actions.generic-menu.view :as generic-menu]
    [utils.i18n :as i18n]))

(defn view
  [id intro-message]
  [generic-menu/view
   {:id    id
    :title (i18n/label :t/community-rules)}
   [rn/view {:style {:padding-top 8}}
    (if (empty? intro-message)
      [community-rules/view community-rules/standard-rules]
      [quo/text
       {:accessibility-label :communities-rules
        :weight              :regular
        :size                :paragraph-2}
       intro-message])]])
