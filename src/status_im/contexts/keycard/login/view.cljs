(ns status-im.contexts.keycard.login.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn empty-view
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title            (i18n/label :t/keycard-empty)
     :description      :text
     :description-text (i18n/label :t/no-key-pair-keycard)}]
   [quo/keycard]
   [quo/category
    {:list-type :settings
     :label (i18n/label :t/what-you-can-do)
     :blur? true
     :data
     [{:title             (i18n/label :t/create-new-profile)
       :image             :icon
       :image-props       :i/profile
       :action            :arrow
       :description       :text
       :description-props {:text (i18n/label :t/new-key-pair-keycard)}
       :on-press          (fn []
                            (rf/dispatch [:navigate-back])
                            (rf/dispatch [:keycard/create.open-empty]))}]}]])

(defn already-added-view
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title            (i18n/label :t/profile-already-added)
     :description      :text
     :description-text (i18n/label :t/profile-linked-keycard)}]
   [rn/image
    {:resize-mode :contain
     :style       {:flex 1 :width (:width (rn/get-window))}
     :source      (resources/get-image :keycard-profile-already-added)}]
   [quo/bottom-actions
    {:actions          :one-action
     :button-one-label (i18n/label :t/select-profile)
     :button-one-props {:on-press #(rf/dispatch [:pop-to-root :screen/profile.profiles])}}]])
