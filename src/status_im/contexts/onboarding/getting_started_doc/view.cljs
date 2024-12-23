(ns status-im.contexts.onboarding.getting-started-doc.view
  (:require
    [quo.core :as quo]
    [status-im.contexts.onboarding.getting-started-doc.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn getting-started-doc
  []
  [quo/documentation-drawers
   {:title  (i18n/label :t/getting-started-with-status)
    :shell? true}
   [:<>
    [quo/text
     {:size  :paragraph-2
      :style style/main-content}
     (i18n/label :t/getting-started-description)]
    [quo/text
     {:size   :paragraph-1
      :style  style/subtitle
      :weight :semi-bold}
     (i18n/label :t/generate-keys)]
    [quo/text
     {:size  :paragraph-2
      :style style/content}
     (i18n/label :t/getting-started-generate-keys-description)]
    [quo/text
     {:size   :paragraph-1
      :style  style/subtitle
      :weight :semi-bold}
     (i18n/label :t/getting-started-generate-keys-from-recovery-phrase)]
    [quo/text
     {:size  :paragraph-2
      :style style/content}
     (i18n/label :t/getting-started-generate-keys-from-recovery-phrase-description)]
    [quo/text
     {:size   :paragraph-1
      :style  style/subtitle
      :weight :semi-bold}
     (i18n/label :t/getting-started-generate-keys-on-keycard)]
    [quo/text
     {:size  :paragraph-2
      :style style/content}
     (i18n/label :t/getting-started-generate-keys-on-keycard-description)]]])

(defn show-as-bottom-sheet
  []
  (rf/dispatch [:show-bottom-sheet
                {:content getting-started-doc
                 :shell?  true}]))
