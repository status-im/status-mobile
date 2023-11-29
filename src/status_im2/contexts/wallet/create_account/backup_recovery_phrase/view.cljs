(ns status-im2.contexts.wallet.create-account.backup-recovery-phrase.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [status-im2.contexts.wallet.create-account.backup-recovery-phrase.style :as style]))


(defn view
  []
  [rn/view {:style {:flex 1}}
   [quo/page-nav
    {:icon-name           :i/close
     :on-press            #(rf/dispatch [:navigate-back])
     :accessibility-label :top-bar}]
   [quo/text-combinations
    {:container-style     style/header-container
     :title               (i18n/label :t/backup-recovery-phrase)
     :description         (i18n/label :t/backup-recovery-phrase-description)}]])
