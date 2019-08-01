(ns status-im.ui.screens.intro.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.react-native.resources :as resources]
            [status-im.privacy-policy.core :as privacy-policy]
            [status-im.multiaccounts.create.core :refer [step-kw-to-num]]
            [status-im.ui.components.colors :as colors]
            [reagent.core :as r]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.screens.intro.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.status-bar.view :as status-bar]))

(defn dots-selector [{:keys [on-press n selected color]}]
  [react/view {:style (styles/dot-selector n)}
   (doall
    (for [i (range n)]
      ^{:key i}
      [react/view {:style (styles/dot color (selected i))}]))])

(defn intro-viewer [slides window-width]
  (let [margin 24
        view-width  (- window-width (* 2 margin))
        scroll-x (r/atom 0)
        scroll-view-ref (atom nil)]
    (fn []
      [react/view {:style {:margin-horizontal 32
                           :align-items :center
                           :justify-content :flex-end}}
       [react/scroll-view {:horizontal true
                           :paging-enabled true
                           :ref #(reset! scroll-view-ref %)
                           :shows-vertical-scroll-indicator false
                           :shows-horizontal-scroll-indicator false
                           :pinch-gesture-enabled false
                           :on-scroll #(let [x (.-nativeEvent.contentOffset.x %)]
                                         (reset! scroll-x x))
                           :style {:width view-width
                                   :margin-vertical 32}}
        (for [s slides]
          ^{:key (:title s)}
          [react/view {:style {:width view-width
                               :padding-horizontal 16}}
           [react/view {:style styles/intro-logo-container}
            [components.common/image-contain
             {:container-style {}}
             {:image (:image s) :width view-width  :height view-width}]]
           [react/i18n-text {:style styles/wizard-title :key (:title s)}]
           [react/i18n-text {:style styles/wizard-text
                             :key   (:text s)}]])]
       (let [selected (hash-set (/ @scroll-x view-width))]
         [dots-selector {:selected selected :n (count slides)
                         :color colors/blue}])])))

(defview intro []
  (let [window-width 300]
    [react/view {:style styles/intro-view}
     [status-bar/status-bar {:flat? true}]
     [intro-viewer [{:image (:intro1 resources/ui)
                     :title :intro-title1
                     :text :intro-text1}
                    {:image (:intro2 resources/ui)
                     :title :intro-title2
                     :text :intro-text2}
                    {:image (:intro3 resources/ui)
                     :title :intro-title3
                     :text :intro-text3}] window-width]
     [react/view styles/buttons-container
      [components.common/button {:button-style (assoc styles/bottom-button :margin-bottom 16)
                                 :on-press     #(re-frame/dispatch [:multiaccounts.create.ui/intro-wizard true])
                                 :label        (i18n/label :t/get-started)}]
      [components.common/button {:button-style (assoc styles/bottom-button :margin-bottom 24)
                                 :on-press    #(re-frame/dispatch [:multiaccounts.recover.ui/recover-multiaccount-button-pressed])
                                 :label       (i18n/label :t/access-key)
                                 :background? false}]
      [react/nested-text
       {:style styles/welcome-text-bottom-note}
       (i18n/label :t/intro-privacy-policy-note1)
       [{:style (assoc styles/welcome-text-bottom-note :color colors/blue)
         :on-press privacy-policy/open-privacy-policy-link!}
        (i18n/label :t/intro-privacy-policy-note2)]]]]))