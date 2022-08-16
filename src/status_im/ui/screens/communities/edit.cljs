(ns status-im.ui.screens.communities.edit
  (:require [quo.core :as quo]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.screens.communities.create :as community.create]
            [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.communities.core :as communities]
            [status-im.ui.components.toolbar :as toolbar]))

(defn edit []
  (let [{:keys [name description]} (<sub [:communities/create])]
    [:<>
     [community.create/form]
     [toolbar/toolbar
      {:show-border? true
       :center
       [quo/button {:disabled (not (community.create/valid? name description))
                    :type     :secondary
                    :on-press #(>evt [::communities/edit-confirmation-pressed])}
        (i18n/label :t/save)]}]]))
