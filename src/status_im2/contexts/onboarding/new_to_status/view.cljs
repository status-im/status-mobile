(ns status-im2.contexts.onboarding.new-to-status.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.keycard.recovery :as keycard]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.onboarding.new-to-status.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [status-im2.config :as config]
    re-frame.db))

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
     [rn/view
      [quo/small-option-card
       {:variant  :icon
        :title    (i18n/label :t/use-recovery-phrase)
        :subtitle (i18n/label :t/use-recovery-phrase-subtitle)
        :image    (resources/get-image :ethereum-address)
        :on-press #(rf/dispatch [:navigate-to-within-stack [:enter-seed-phrase :new-to-status]])}]
      [rn/view {:style style/space-between-suboptions}]
      [quo/small-option-card
       {:variant  :icon
        :title    (i18n/label :t/use-keycard)
        :subtitle (i18n/label :t/use-keycard-subtitle)
        :image    (resources/get-image :use-keycard)
        :on-press #(rf/dispatch [::keycard/recover-with-keycard-pressed])}]]]))

(defn getting-started-doc
  []
  [quo/documentation-drawers
   {:title  (i18n/label :t/getting-started-with-status)
    :shell? true}
   [:<>
    [quo/text
     {:size  :paragraph-2
      :style style/doc-main-content}
     (i18n/label :t/getting-started-description)]
    [quo/text
     {:size   :paragraph-1
      :style  style/doc-subtitle
      :weight :semi-bold}
     (i18n/label :t/generate-keys)]
    [quo/text
     {:size  :paragraph-2
      :style style/doc-content}
     (i18n/label :t/getting-started-generate-keys-description)]
    [quo/text
     {:size   :paragraph-1
      :style  style/doc-subtitle
      :weight :semi-bold}
     (i18n/label :t/getting-started-generate-keys-from-recovery-phrase)]
    [quo/text
     {:size  :paragraph-2
      :style style/doc-content}
     (i18n/label :t/getting-started-generate-keys-from-recovery-phrase-description)]
    [quo/text
     {:size   :paragraph-1
      :style  style/doc-subtitle
      :weight :semi-bold}
     (i18n/label :t/getting-started-generate-keys-on-keycard)]
    [quo/text
     {:size  :paragraph-2
      :style style/doc-content}
     (i18n/label :t/getting-started-generate-keys-on-keycard-description)]]])

(defn new-to-status
  []
  (let [{:keys [top]} (safe-area/get-insets)]
    [rn/view {:style style/content-container}
     [quo/page-nav
      {:margin-top top
       :type       :no-title
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   #(do
                      (rf/dispatch [:onboarding/overlay-dismiss])
                      (rf/dispatch [:navigate-back]))
       :right-side [{:icon-name :i/info
                     :on-press  #(rf/dispatch [:show-bottom-sheet
                                               {:content getting-started-doc
                                                :shell?  true}])}
                    (when config/quo-preview-enabled?
                      {:icon-name :i/reveal-whitelist
                       :on-press  #(rf/dispatch [:navigate-to :quo2-preview])})]}]
     [sign-in-options]]))
