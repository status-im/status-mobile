(ns status-im.contexts.communities.actions.community-rules.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.communities.actions.community-rules.style :as style]
    [utils.re-frame :as rf]))

(defn view
  [id]
  (let [rules (rf/sub [:communities/rules id])]
    [rn/view {:style style/community-rule}
     [quo/text
      {:weight :regular
       :size   :paragraph-2}
      rules]]))
