(ns status-im.ui.screens.browser.bookmarks.views
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.topbar :as topbar]))

(defn screen
  [{url :url name :name new-arg :new}]
  (let [input-name (reagent/atom name)]
    (fn []
      (let [edit? (not new-arg)]
        [kb-presentation/keyboard-avoiding-view
         {:style         {:flex 1}
          :ignore-offset true}
         [topbar/topbar
          {:modal?        true
           :border-bottom true
           :title         (if edit? (i18n/label :t/edit-favourite) (i18n/label :t/new-favourite))}]
         [react/view {:flex 1}
          [quo/text-input
           {:container-style     {:margin 16 :margin-top 10}
            :accessibility-label :bookmark-input
            :max-length          100
            :auto-focus          true
            :show-cancel         false
            :label               (i18n/label :t/name)
            :default-value       @input-name
            :on-change-text      #(reset! input-name %)}]
          [react/text {:style {:margin 16 :color colors/gray}}
           url]]
         [toolbar/toolbar
          {:show-border? true
           :center
           [quo/button
            {:accessibility-label :save-bookmark
             :type                :secondary
             :disabled            (string/blank? @input-name)
             :on-press            #(do (if edit?
                                         (re-frame/dispatch [:browser/update-bookmark
                                                             {:url url :name @input-name}])
                                         (re-frame/dispatch [:browser/store-bookmark
                                                             {:url url :name @input-name}]))
                                       (re-frame/dispatch [:navigate-back]))}
            (if edit? (i18n/label :t/save) (i18n/label :t/add-favourite))]}]]))))

(defn new-bookmark
  []
  [screen @(re-frame/subscribe [:get-screen-params])])
