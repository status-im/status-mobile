(ns status-im.contexts.communities.actions.permissions-sheet.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.contexts.communities.actions.permissions-sheet.style :as style]
    [status-im.contexts.communities.utils :as communities.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- role-text
  [role]
  (i18n/label
   (communities.utils/role->translation-key role
                                            :t/token-master)))

(defn- permission-view
  [{:keys [tokens role satisfied?]}]
  (when (seq tokens)
    ^{:key role}
    [rn/view {:style {:margin-bottom 20}}
     [quo/text {:weight :medium}
      (if satisfied?
        (i18n/label :t/you-eligible-to-join-as {:role (role-text role)})
        (i18n/label :t/you-not-eligible-to-join-as {:role (role-text role)}))]
     [quo/text {:size :paragraph-2 :style {:padding-bottom 8}}
      (if satisfied?
        (i18n/label :t/you-hodl)
        (i18n/label :t/you-must-hold))]
     [quo/token-requirement-list
      {:tokens          tokens
       :container-style {:padding-horizontal 20}}]]))

(defn view
  [id]
  (let [permissions (rf/sub [:community/token-permissions id])]
    [gesture/scroll-view {:style style/container}
     (map permission-view permissions)]))
