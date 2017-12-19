(ns status-im.ui.screens.discover.dapp-details.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.discover.components.views :as components]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.discover.styles :as styles]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]))

(defn section [title content]
  [react/view styles/dapp-details-section-container
   [react/view styles/dapp-details-section-title-container
    [react/text {:font  :small
                 :style styles/dapp-details-section-title-text} title]]
   [react/view styles/dapp-details-section-body-container
    [react/text {:font :medium
                 :style styles/dapp-details-section-content-text} content]]])

(defview dapp-details []
  (letsubs [{:keys [photo-path name dapp-url] :as dapp} [:get :discover-current-dapp]]
    [react/view styles/dapp-details-container
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/dapp-profile)]]
     [react/view styles/dapp-details-inner-container
      [react/view styles/dapp-details-header
       [react/view styles/dapp-details-icon
        [chat-icon.screen/chat-icon photo-path {:size 56}]]
       [react/view styles/dapp-details-name-container
        [react/text {:font  :medium
                     :style styles/dapp-details-name-text} name]]]
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:open-chat-with-contact dapp])}
       [react/view styles/dapp-details-action-container
        [react/view styles/dapp-details-action-icon-container
         [vector-icons/icon :icons/open {:color           :active
                                         :style           styles/dapp-details-open-icon
                                         :container-style styles/dapp-details-open-icon-background}]]
        [react/view styles/dapp-details-action-name-container
         [react/view
          [react/text {:font  :medium
                       :style styles/dapp-details-action-name-text} (i18n/label :t/open)]]]]]
      [section (i18n/label :t/description) (:description dapp "Description goes here")]
      [common/separator]
      [section (i18n/label :t/url) dapp-url]]]))
