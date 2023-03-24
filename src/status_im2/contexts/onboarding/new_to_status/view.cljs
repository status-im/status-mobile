(ns status-im2.contexts.onboarding.new-to-status.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im.keycard.recovery :as keycard]
            [status-im.multiaccounts.recover.core :as multiaccounts.recover]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.onboarding.new-to-status.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn background-image
  []
  [rn/image
   {:style       style/image-background
    :blur-radius 13
    :source      (resources/get-image :onboarding-blur-bg)}])

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav
    {:align-mid?            true
     :mid-section           {:type :text-only :main-text ""}
     :left-section          {:type                :blur-bg
                             :icon                :i/arrow-left
                             :icon-override-theme :dark
                             :on-press            #(rf/dispatch [:navigate-back])}
     :right-section-buttons [{:type                :blur-bg
                              :icon                :i/info
                              :icon-override-theme :dark
                              :on-press            #(js/alert "Pending")}]}]])

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
       {:variant             :icon
        :title               "Temporary (old) generate keys flow"
        :subtitle            "generate keys"
        :image               (resources/get-image :use-keycard)
        :accessibility-label :generate-old-key
        :on-press            #(rf/dispatch [:generate-and-derive-addresses])}]
      [rn/view {:style style/space-between-suboptions}]
      [quo/small-option-card
       {:variant  :icon
        :title    "Temporary (old) recover phrase flow"
        :subtitle (i18n/label :t/use-recovery-phrase-subtitle)
        :image    (resources/get-image :ethereum-address)
        :on-press #(rf/dispatch [::multiaccounts.recover/enter-phrase-pressed])}]
      [rn/view {:style style/space-between-suboptions}]
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
  [rn/view {:style style/full-screen}
   [background-image]
   [rn/view {:style style/layer-background}
    [navigation-bar]
    [sign-in-options]]])
