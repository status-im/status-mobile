(ns status-im.ui.screens.usage-data.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.screens.usage-data.styles :as styles]))

(views/defview usage-data []
  (views/letsubs [next [:get-screen-params]]
    [react/view {:style styles/usage-data-view}
     [status-bar/status-bar {:flat? true}]
     [react/view {:style styles/logo-container}
      [components.common/logo styles/logo]
      [react/image {:source (:analytics-image resources/ui)
                    :style  styles/usage-data-image}]]
     [react/text {:style styles/help-improve-text}
      (i18n/label :t/help-improve)]
     [react/view
      [react/text {:style styles/help-improve-text-description}
       (i18n/label :t/help-improve-description)]]
     [react/view styles/buttons-container
      [components.common/button {:button-style {:flex-direction :row}
                                 :on-press     #(re-frame/dispatch [:help-improve-handler true next])
                                 :label        (i18n/label :t/share-usage-data)}]
      [react/view styles/bottom-button-container
       [components.common/button {:on-press    #(re-frame/dispatch [:help-improve-handler false next])
                                  :label       (i18n/label :t/dont-want-to-share)
                                  :background? false}]]]]))
