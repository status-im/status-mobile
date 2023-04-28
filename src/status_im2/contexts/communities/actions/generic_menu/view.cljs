(ns status-im2.contexts.communities.actions.generic-menu.view
  (:require [quo2.core :as quo]
            [status-im2.contexts.communities.actions.generic-menu.style :as style]
            [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [id title]} children]
  (let [{:keys [name images]} (rf/sub [:communities/community id])]
    [rn/view {:style style/container}
     [rn/view {:style style/inner-container}
      [quo/text
       {:accessibility-label :communities-join-community
        :weight              :semi-bold
        :size                :heading-1}
       title]]
     [quo/context-tag
      {:style style/context-tag}
      (:thumbnail images) name]
     children]))
