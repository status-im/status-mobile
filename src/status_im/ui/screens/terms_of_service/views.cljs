(ns status-im.ui.screens.terms-of-service.views
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.design-system.spacing :as spacing]
            [quo.design-system.typography :as typography]
            [re-frame.core :as re-frame]
            [status-im2.constants :refer [docs-link]]
            [utils.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn principles-item
  []
  [react/nested-text {}
   (i18n/label :t/wc-new-tos-based-on-principles-prefix)
   [{:style    (merge {:color colors/blue}
                      typography/font-medium)
     :on-press #(re-frame/dispatch [:open-modal :principles])}
    " "
    (i18n/label :t/principles)]])

(def changes
  [[principles-item]
   :wc-how-to-use-status-app
   :wc-brand-guide
   :wc-disclaimer
   :wc-dispute])

(defn change-list-item
  [label]
  [react/view
   {:flex-direction    :row
    :align-items       :center
    :margin-horizontal (:base spacing/spacing)
    :margin-vertical   (:tiny spacing/spacing)}
   [icons/icon :main-icons/checkmark-circle
    {:color           colors/blue
     :container-style {:margin-top   1.2
                       :margin-right (:tiny spacing/spacing)}}]
   [react/view {:style {:padding-right (:xx-large spacing/spacing)}}
    (if (keyword? label)
      [react/text (i18n/label label)]
      label)]])

(defview force-accept-tos
  []
  (letsubs [next-root [:tos-accept-next-root]]
    [react/scroll-view
     [react/view
      {:style (merge {:align-items :center}
                     (:x-large spacing/padding-horizontal))}
      [react/image
       {:source (resources/get-image :status-logo)
        :style  {:margin-vertical (:base spacing/spacing)
                 :width           32
                 :height          32}}]
      [quo/text
       {:size   :x-large
        :align  :center
        :weight :bold
        :style  {:margin-bottom (:base spacing/spacing)}}
       (i18n/label :t/updates-to-tos)]
      [quo/text
       {:color :secondary
        :align :center}
       (i18n/label :t/updates-to-tos-desc)]]

     [quo/separator {:style {:margin-top (:base spacing/spacing)}}]
     [quo/list-item
      {:title               [quo/text
                             {:color  :link
                              :weight :medium}
                             (i18n/label :t/terms-of-service)]
       :accessibility-label :tos
       :chevron             true
       :on-press            #(re-frame/dispatch [:open-modal :terms-of-service])}]
     [quo/separator]

     [quo/list-header (i18n/label :t/what-changed)]
     (for [c changes]
       ^{:key c}
       [change-list-item c])

     [quo/separator {:style {:margin-vertical (:base spacing/spacing)}}]

     [react/view {:style (:base spacing/padding-horizontal)}
      [quo/text {:weight :medium} (i18n/label :t/status-is-open-source)]
      [quo/text {:color :secondary} (i18n/label :t/build-yourself)]
      [quo/text
       {:color    :link
        :weight   :medium
        :on-press #(.openURL ^js react/linking docs-link)}
       docs-link]]

     [quo/separator {:style {:margin-vertical (:base spacing/spacing)}}]

     [toolbar/toolbar
      {:size   :large
       :center
       [react/view {:padding-horizontal 8}
        [quo/button
         {:type     :primary
          :on-press #(do
                       (re-frame/dispatch [:hide-terms-of-services-opt-in-screen])
                       (re-frame/dispatch [:init-root next-root]))}
         (i18n/label :t/accept-and-continue)]]}]]))

(comment
  (re-frame/dispatch [:navigate-to :force-accept-tos]))
