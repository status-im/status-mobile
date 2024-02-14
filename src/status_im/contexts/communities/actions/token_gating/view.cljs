(ns status-im.contexts.communities.actions.token-gating.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn token-requirements
  []
  (fn [id]
    (let [{:keys [can-request-access? tokens]} (rf/sub [:community/token-gated-overview id])]
      [rn/view {:style {:padding-horizontal 12}}
       [quo/text {:weight :medium}
        (if can-request-access?
          (i18n/label :t/you-eligible-to-join)
          (i18n/label :t/you-not-eligible-to-join))]
       [quo/text {:style {:padding-bottom 18} :size :paragraph-2}
        (if can-request-access?
          (i18n/label :t/you-hodl)
          (i18n/label :t/you-must-hold))]
       [quo/token-requirement-list {:tokens tokens}]])))
