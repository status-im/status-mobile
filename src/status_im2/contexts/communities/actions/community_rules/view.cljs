(ns status-im2.contexts.communities.actions.community-rules.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.communities.actions.community-rules.style :as style]
    [utils.re-frame :as rf]))

(defn view
  [id]
  (let [rules (rf/sub [:communities/rules id])]
    [rn/view {:style style/community-rule}
     [quo/text
      {:style  style/community-rule-text
       :weight :regular
       :size   :paragraph-2}
      rules]]))
