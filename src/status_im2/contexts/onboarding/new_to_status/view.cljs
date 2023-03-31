(ns status-im2.contexts.onboarding.new-to-status.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im.keycard.recovery :as keycard]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.onboarding.new-to-status.style :as style]
    [status-im2.contexts.onboarding.common.navigation-bar.view :as navigation-bar]
    [status-im2.contexts.onboarding.common.background.view :as background]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn sign-in-options
  []
  (let [window (rf/sub [:dimensions/window])]
    [rn/view {:style style/options-container}
     [quo/text
      {:style  style/title
       :size   :heading-1
       :weight :semi-bold}
      (i18n/label :t/new-to-status)]

     [quo/small-option-card
      {:variant    :main
       :title      (i18n/label :t/generate-keys)
       :subtitle   (i18n/label :t/generate-keys-subtitle)
       :image      (resources/get-image :generate-keys)
       :max-height (- (:height window)
                      (* 2 56) ;; two other list items
                      (* 2 16) ;; spacing between items
                      220)     ;; extra spacing (top bar)
       :on-press   #(rf/dispatch [:onboarding-2/navigate-to-create-profile])}]

     [rn/view {:style style/subtitle-container}
      [quo/text
       {:style  style/subtitle
        :size   :paragraph-2
        :weight :medium}
       (i18n/label :t/experienced-web3)]]

     [rn/view {:style style/suboptions}
      [quo/small-option-card
       {:variant  :icon
        :title    (i18n/label :t/use-recovery-phrase)
        :subtitle (i18n/label :t/use-recovery-phrase-subtitle)
        :image    (resources/get-image :ethereum-address)
        :on-press #(rf/dispatch [:navigate-to :enter-seed-phrase])}]
      [rn/view {:style style/space-between-suboptions}]
      [quo/small-option-card
       {:variant  :icon
        :title    (i18n/label :t/use-keycard)
        :subtitle (i18n/label :t/use-keycard-subtitle)
        :image    (resources/get-image :use-keycard)
        :on-press #(rf/dispatch [::keycard/recover-with-keycard-pressed])}]]]))

(defn new-to-status
  []
  [:<>
   [background/view true]
   [rn/view {:style style/content-container}
    [navigation-bar/navigation-bar {:on-press-info #(js/alert "Info pressed")}]
    [sign-in-options]]])
