(ns status-im.ui.components.qr-code-viewer.views
  (:require [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.ui.components.qr-code-viewer.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.tribute-to-talk.views :as tr-to-talk])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn qr-code [props]
  (reagent/create-element
   (rn-dependencies/qr-code)
   (clj->js (merge {:inverted true} props))))

(defview qr-code-viewer-component [{:keys [style hint-style footer-style footer-button value hint legend]}]
  (letsubs  [{:keys [width]} [:dimensions/window]
             {:keys [disabled?]} [:tribute-to-talk/ui]]
    [(react/scroll-view) {:content-container-style {:align-items     :center
                                                    :margin-top      16
                                                    :justify-content :center}
                          :style                   (merge {:flex 1} style)}
     (when-not disabled?
       [react/view {:style {:margin-horizontal 16}}
        [tr-to-talk/enabled-note]])
     (when width
       (let [size (int (min width styles/qr-code-max-width))]
         [react/view {:style               (styles/qr-code-container size)
                      :accessibility-label :qr-code-image}
          [qr-code {:value value
                    :size  size}]]))
     [react/text {:style (merge styles/qr-code-hint hint-style)}
      hint]
     [react/view styles/footer
      [react/view styles/wallet-info
       [react/text {:style               (merge styles/hash-value-text footer-style)
                    :accessibility-label :address-text
                    :selectable          true}
        legend]]]
     (when footer-button
       [react/view {:style {:align-self    :stretch
                            :margin-bottom 16}}
        [footer-button value]])]))

(defn qr-code-viewer [{:keys [style hint-style footer-style footer-button value hint legend]
                       :as params}]
  (if value
    [qr-code-viewer-component params]
    [react/view [react/text "no value"]]))
