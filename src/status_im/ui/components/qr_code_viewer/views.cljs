(ns status-im.ui.components.qr-code-viewer.views
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.qr-code-viewer.styles :as styles]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.common.styles :as common.styles]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [reagent.core :as r])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn qr-code [props]
  (r/create-element
    rn-dependencies/qr-code
    (clj->js (merge {:inverted true} props))))

(defn qr-viewer-toolbar [name qr-value]
  [react/view styles/account-toolbar
   [react/view styles/toolbar-contents
    [react/view styles/toolbar-action-container
     [common/icon-or-label {:on-press #(re-frame/dispatch [:navigate-back])}
      :t/done styles/toolbar-done-text-ios :icons/close {:color :black}]]
    [react/view styles/name-container
     [react/text {:style           styles/name-text
                  :number-of-lines 1} name]]
    [react/view styles/toolbar-action-container
     [react/touchable-highlight {:on-press #(list-selection/open-share {:message qr-value})}
      [react/view styles/toolbar-action-icon-container
       [vector-icons/icon :icons/share {:color :black}]]]]]])

(defn qr-viewer-body [qr-value dimensions]
  [react/view {:style     styles/qr-code
               :on-layout #(let [layout (.. % -nativeEvent -layout)]
                             (re-frame/dispatch [:set-in [:qr-modal :dimensions] {:width  (* 0.7 (.-width layout))
                                                                                  :height (.-height layout)}]))}
   [react/text {:style styles/qr-code-hint} (i18n/label :t/qr-code-public-key-hint)]
   (when (:width dimensions)
     [react/view {:style (styles/qr-code-container dimensions)}
      [qr-code {:value qr-value
                :size  (- (min (:width dimensions)
                               (:height dimensions))
                          (* 2 styles/qr-code-padding))}]])])

(defn qr-viewer-footer [qr-value]
  [react/view styles/footer
   [react/view styles/wallet-info
    [react/text {:style styles/hash-value-text} qr-value]]])

(defview qr-viewer []
  (letsubs [{:keys [qr-value dimensions contact]} [:get :qr-modal]]
    [react/view styles/wallet-qr-code
     [status-bar/status-bar {:type :modal}]
     [qr-viewer-toolbar (:name contact) qr-value]
     [qr-viewer-body qr-value dimensions]
     [qr-viewer-footer qr-value]]))
