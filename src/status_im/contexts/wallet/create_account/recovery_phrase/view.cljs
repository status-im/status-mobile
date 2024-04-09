(ns status-im.contexts.wallet.create-account.recovery-phrase.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [[words-count set-words-count] (rn/use-state 0)]
  [rn/view
   [quo/page-nav
    {:icon-name           :i/close
     :on-press            #(rf/dispatch [:navigate-back])
     :accessibility-label :top-bar}]
   [quo/page-top
    {
     ;:container-style   style/header-container
     :title             (i18n/label :t/use-recovery-phrase)
     :title-right       :counter
     :title-right-props {:text (i18n/label-pluralize words-count :t/words-n)}}]]))
