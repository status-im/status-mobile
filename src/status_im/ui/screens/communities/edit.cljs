(ns status-im.ui.screens.communities.edit
  (:require [quo.core :as quo]
            [status-im.communities.core :as communities]
            [i18n.i18n :as i18n]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.screens.communities.create :as community.create]
            [utils.re-frame :as rf]))

(defn edit
  []
  (let [{:keys [name description]} (rf/sub [:communities/create])]
    [:<>
     [community.create/form]
     [toolbar/toolbar
      {:show-border? true
       :center
       [quo/button
        {:disabled (not (community.create/valid? name description))
         :type     :secondary
         :on-press #(rf/dispatch [::communities/edit-confirmation-pressed])}
        (i18n/label :t/save)]}]]))
