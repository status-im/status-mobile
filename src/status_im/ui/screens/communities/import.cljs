(ns status-im.ui.screens.communities.import
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo.core :as quo]
            [status-im.i18n :as i18n]
            [status-im.utils.handlers :refer [>evt]]
            [status-im.communities.core :as communities]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.toolbar :as toolbar]))

(defn view []
  (let [community-key (reagent/atom "")]
    (fn []
      [rn/view {:style {:flex 1}}
       [topbar/topbar {:title (i18n/label :t/import-community-title)}]
       [rn/scroll-view {:style                   {:flex 1}
                        :content-container-style {:padding 16}}
        [rn/view {:style {:padding-bottom 16
                          :padding-top    10}}
         [quo/text-input
          {:label          (i18n/label :t/community-key)
           :placeholder    (i18n/label :t/community-key-placeholder)
           :on-change-text #(reset! community-key %)
           :default-value  @community-key
           :auto-focus     true}]]]

       [toolbar/toolbar
        {:show-border? true
         :center       [quo/button {:disabled (= @community-key "")
                                    :type     :secondary
                                    :on-press #(>evt [::communities/import @community-key])}
                        (i18n/label :t/import)]}]])))
