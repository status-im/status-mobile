(ns status-im.ui.screens.usage-data.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.screens.usage-data.styles :as styles]
            [status-im.utils.utils :as utils]))

(views/defview usage-data []
  (views/letsubs [next [:get-screen-params]
                  {:keys [width height]} [:dimensions/window]]
    [react/view {:style styles/usage-data-view}
     [status-bar/status-bar {:flat? true}]
     [react/view
      [react/view {:style (styles/image-container height)}
       [react/image {:source (:analytics-image resources/ui)
                     :style  (styles/usage-data-image height)}]]
      [react/i18n-text {:style (styles/help-improve-text height)
                        :key   :help-improve}]
      [react/view
       [react/i18n-text {:style (styles/help-improve-text-description height)
                         :key   :help-improve-description}]]
      [react/text {:style    styles/learn-what-we-collect-link
                   :on-press #(.openURL react/linking "https://wiki.status.im/Help_Improve_Status#Help_Improve_Status")}
       (i18n/label :t/learn-what-we-collect-link)]]
     [react/view (styles/bottom-button-container height)
      [components.common/button {:button-style (styles/share-button width)
                                 :uppercase?   false
                                 :on-press     #(utils/show-confirmation {:ios-confirm-style "default"}
                                                                         (i18n/label :t/confirmation-title)
                                                                         (i18n/label :t/confirmation-text)
                                                                         (i18n/label :t/confirmation-action)
                                                                         (fn [] (re-frame/dispatch [:help-improve-handler true next]))
                                                                         nil)
                                 :label        (i18n/label :t/share-usage-data)}]
      [components.common/button {:button-style (styles/dont-share-button width)
                                 :uppercase?   false
                                 :on-press     #(re-frame/dispatch [:help-improve-handler false next])
                                 :label        (i18n/label :t/dont-want-to-share)}]]]))

