(ns status-im.contexts.keycard.create.view
  (:require [quo.context]
            [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.constants :as constants]
            [status-im.contexts.keycard.common.view :as common.view]
            [status-im.contexts.keycard.factory-reset.view :as factory-reset]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- ready-to-add-documentation
  []
  [quo/documentation-drawers
   {:shell? true}
   [quo/text {:size :paragraph-2}
    (i18n/label :t/ready-add-keypair-keycard-documentation)]])

(defn ready-to-add
  []
  (let [{:keys [on-continue]} (quo.context/use-screen-params)]
    [:<>
     [quo/page-nav
      {:icon-name  :i/close
       :on-press   events-helper/navigate-back
       :right-side [{:icon-name :i/info
                     :on-press  #(rf/dispatch [:show-bottom-sheet
                                               {:content ready-to-add-documentation
                                                :theme   :dark
                                                :shell?  true}])}]}]
     [quo/page-top
      {:title (i18n/label :t/ready-add-keypair-keycard)}]
     [rn/image
      {:resize-mode :contain
       :style       {:flex 1 :align-self :center :margin-vertical 37}
       :source      (resources/get-image :add-key-to-keycard)}]
     [common.view/tips]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-label (i18n/label :t/scan-keycard)
       :button-one-props {:on-press on-continue}}]]))

(defn ready-to-generate
  []
  (let [{:keys [on-continue]} (quo.context/use-screen-params)]
    [:<>
     [quo/page-nav
      {:icon-name :i/close
       :on-press  events-helper/navigate-back}]
     [quo/page-top
      {:title (i18n/label :t/ready-generate-keypair)}]
     [rn/image
      {:resize-mode :contain
       :style       {:flex 1 :align-self :center :margin-vertical 37}
       :source      (resources/get-image :add-key-to-keycard)}]
     [common.view/tips]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-label (i18n/label :t/scan-keycard)
       :button-one-props {:on-press on-continue}}]]))

(defn not-empty-view
  []
  (let [{:keys [on-login]} (quo.context/use-screen-params)]
    [:<>
     [quo/page-nav
      {:icon-name :i/close
       :on-press  events-helper/navigate-back}]
     [quo/page-top
      {:title            (i18n/label :t/keycard-not-empty)
       :description      :text
       :description-text (i18n/label :t/cant-store-new-keys)}]
     [quo/keycard]
     [quo/category
      {:list-type :settings
       :label (i18n/label :t/what-you-can-do)
       :blur? true
       :data
       [{:title             (i18n/label :t/login-with-keycard)
         :image             :icon
         :image-props       :i/profile
         :action            :arrow
         :description       :text
         :description-props {:text (i18n/label :t/use-keypair-keycard)}
         :on-press          on-login}
        {:title             (i18n/label :t/factory-reset)
         :image             :icon
         :image-props       :i/revert
         :action            :arrow
         :description       :text
         :description-props {:text (i18n/label :t/remove-keycard-content)}
         :on-press          (fn []
                              (rf/dispatch [:show-bottom-sheet
                                            {:theme   :dark
                                             :shell?  true
                                             :content factory-reset/sheet}]))}]}]]))

(defn empty-view
  []
  (let [{:keys [on-create on-import]} (quo.context/use-screen-params)]
    [:<>
     [quo/page-nav
      {:icon-name :i/close
       :on-press  events-helper/navigate-back}]
     [quo/page-top
      {:title            (i18n/label :t/keycard-empty)
       :description      :text
       :description-text (i18n/label :t/what-to-do)}]
     [quo/small-option-card
      {:variant             :main
       :title               (i18n/label :t/create-new-profile)
       :subtitle            (i18n/label :t/new-key-pair-keycard)
       :button-label        (i18n/label :t/lets-go)
       :accessibility-label :create-new-profile-keycard
       :container-style     {:margin-horizontal 20 :margin-top 8}
       :image               (resources/get-image :keycard-buy)
       :button-props        {:type :primary}
       :on-press            on-create}]
     [quo/small-option-card
      {:variant             :icon
       :title               (i18n/label :t/import-recovery-phrase-to-keycard)
       :subtitle            (i18n/label :t/store-key-pair-on-keycard)
       :accessibility-label :import-recovery-phrase-to-keycard
       :container-style     {:margin 20}
       :image               (resources/get-image :use-keycard)
       :on-press            on-import}]]))

(defn view
  []
  [:<>
   [quo/page-nav
    {:key        :header
     :background :blur
     :icon-name  :i/arrow-left
     :on-press   events-helper/navigate-back}]
   [quo/page-top
    {:title (i18n/label :t/create-profile-keycard)}]
   [rn/view {:style {:padding-horizontal 20 :padding-top 20}}
    [quo/small-option-card
     {:variant             :main
      :title               (i18n/label :t/check-keycard)
      :subtitle            (i18n/label :t/see-keycard-ready)
      :button-label        (i18n/label :t/scan-keycard)
      :button-props        {:type :primary :icon-left :i/keycard}
      :accessibility-label :get-keycard
      :image               (resources/get-image :check-your-keycard)
      :on-press            #(rf/dispatch [:keycard/create.check-empty-card])}]
    [rn/view {:style {:height 12}}]
    [quo/small-option-card
     {:variant             :icon
      :title               (i18n/label :t/learn-more-keycard)
      :subtitle            (i18n/label :t/secure-wallet-card)
      :accessibility-label :setup-keycard
      :image               (resources/get-image :use-keycard)
      :on-press            #(rf/dispatch [:browser.ui/open-url constants/get-keycard-url])}]]
   [rn/view {:style {:flex 1}}]
   [common.view/tips]])
