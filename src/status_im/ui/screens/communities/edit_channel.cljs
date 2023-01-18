(ns status-im.ui.screens.communities.edit-channel
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [status-im.communities.core :as communities]
            [utils.i18n :as i18n]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.screens.communities.create-channel :as create-channel]
            [utils.re-frame :as rf]
            [utils.debounce :as debounce]))

(defn valid?
  [community-name]
  (not (string/blank? community-name)))

(defn view
  []
  (let [{:keys [name]} (rf/sub [:communities/create-channel])]
    (fn []
      [:<>
       [create-channel/form]
       [toolbar/toolbar
        {:show-border? true
         :center
         [quo/button
          {:disabled (not (valid? name))
           :type     :secondary
           :on-press #(debounce/dispatch-and-chill
                       [::communities/edit-channel-confirmation-pressed]
                       3000)}
          (i18n/label :t/save)]}]])))
