(ns status-im2.contexts.chat.bottom-sheet-composer.edit.view
  (:require
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.bottom-sheet-composer.constants :as constants]
    [utils.i18n :as i18n]
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [re-frame.core :as rf]
    [react-native.core :as rn]
    [status-im2.contexts.chat.bottom-sheet-composer.edit.style :as style]))

(defn edit-message
  [cancel]
  [rn/view
   {:style               style/container
    :accessibility-label :edit-message}
   [rn/view {:style style/content-container}
    [quo/icon
     :i/connector-dotted
     {:size            16
      :color           (colors/theme-colors colors/neutral-40 colors/neutral-60)
      :container-style style/icon-container}]
    [rn/view {:style style/text-container}
     [quo/text
      {:weight :medium
       :size   :paragraph-2}
      (i18n/label :t/editing-message)]]]
   [quo/button
    {:width               24
     :size                24
     :accessibility-label :edit-cancel-button
     :on-press            (fn []
                            (cancel)
                            (rf/dispatch [:chat.ui/cancel-message-edit]))
     :type                :outline}
    [quo/icon :i/close
     {:size  16
      :color (colors/theme-colors colors/neutral-100 colors/neutral-40)}]]])

(defn- f-view
  [edit cancel]
  (let [height (reanimated/use-shared-value (if edit constants/edit-container-height 0))]
    (rn/use-effect #(reanimated/animate height (if edit constants/edit-container-height 0)) [edit])
    [reanimated/view {:style (reanimated/apply-animations-to-style {:height height} {})}
     (when edit [edit-message cancel])]))

(defn view
  [edit cancel]
  [:f> f-view edit cancel])
