(ns legacy.status-im.ui.screens.communities.import
  (:require
    [legacy.status-im.communities.core :as communities]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [community-key (reagent/atom "")]
    (fn []
      [:<>
       [rn/scroll-view
        {:style                   {:flex 1}
         :content-container-style {:padding 16}}
        [rn/view
         {:style {:padding-bottom 16
                  :padding-top    10}}
         [quo/text-input
          {:label          (i18n/label :t/community-key)
           :placeholder    (i18n/label :t/community-key-placeholder)
           :on-change-text #(reset! community-key %)
           :default-value  @community-key
           :auto-focus     true}]]]

       [toolbar/toolbar
        {:show-border? true
         :center       [quo/button
                        {:disabled (= @community-key "")
                         :type     :secondary
                         :on-press #(rf/dispatch [::communities/import @community-key])}
                        (i18n/label :t/import)]}]])))
